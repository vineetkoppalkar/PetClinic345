package org.springframework.samples.petclinic.migration;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.visit.Visit;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ConsistencyChecker implements Runnable {

    private static final String OWNER_TABLE_NAME = "owners";
    private static final String PET_TABLE_NAME = "pets";
    private static final String VISIT_TABLE_NAME = "visits";

    private static int nbOfOwnerInconsistencies;
    private static int nbOfPetInconsistencies;
    private static int nbOfVisitInconsistencies;

    @Override
    public void run() {
        System.out.println("Starting consistency checkers");
        resetInconsistencyCounters();

        System.out.println("\nConsistency checker RUNNING for table: " + OWNER_TABLE_NAME);
        ownerCheckConsistency();
        System.out.println("Consistency checker COMPLETE for table: " + OWNER_TABLE_NAME);

        System.out.println("\nConsistency checker RUNNING for table: " + PET_TABLE_NAME);
        petCheckConsistency();
        System.out.println("Consistency checker COMPLETE for table: " + PET_TABLE_NAME);

        System.out.println("\nConsistency checker RUNNING for table: " + VISIT_TABLE_NAME);
        visitCheckConsistency();
        System.out.println("Consistency checker COMPLETE for table: " + VISIT_TABLE_NAME);
    }

	public void ownerCheckConsistency() {
        List<Owner> oldDatastoreOwners = TDGHSQL.getAllOwners();
        List<Owner> newDatastoreOwners = TDGSQLite.getAllOwners();

        for (int i = 0; i < oldDatastoreOwners.size(); i++) {
            Owner expected = oldDatastoreOwners.get(i);
            Owner actual;
            try {
                actual = newDatastoreOwners.get(i);
            } catch (IndexOutOfBoundsException e) {
                // New data was added since the forklift
                printViolation(OWNER_TABLE_NAME, "null", expected.toString());
                nbOfOwnerInconsistencies++;

                insertNewOwnerIntoSQLite(expected);
                newDatastoreOwners.add(i, expected);

                continue;
            }

            if (actual != null && !actual.equals(expected)) {
                // Inconsistency for a specific row between new and old datastores
                printViolation(OWNER_TABLE_NAME, actual.toString(), expected.toString());
                nbOfOwnerInconsistencies++;

                fixInconsistencyInOwners(actual.getId(), expected);
                newDatastoreOwners.set(i, expected);
            }
        }
	}

    public void petCheckConsistency() {
        List<Pet> oldDatastorePets = TDGHSQL.getAllPets();
        List<Pet> newDatastorePets = TDGSQLite.getAllPets();

        for (int i = 0; i < oldDatastorePets.size(); i++) {
            Pet expected = oldDatastorePets.get(i);
            Pet actual;
            try {
                actual = newDatastorePets.get(i);
            } catch (IndexOutOfBoundsException e) {
                // New data was added since the forklift
                printViolation(PET_TABLE_NAME, "null", expected.toString());
                nbOfPetInconsistencies++;

                insertNewPetIntoSQLite(expected);
                newDatastorePets.add(i, expected);

                continue;
            }

            if (actual != null && !actual.equals(expected)) {
                // Inconsistency for a specific row between new and old datastores
                printViolation(PET_TABLE_NAME, actual.toString(), expected.toString());
                nbOfPetInconsistencies++;

                fixInconsistencyInPets(actual.getId(), expected);
                newDatastorePets.set(i, expected);
            }
        }
    }

    public void visitCheckConsistency() {
        List<Visit> oldDatastoreVisits = TDGHSQL.getAllVisits();
        List<Visit> newDatastoreVisits = TDGSQLite.getAllVisits();

        for (int i = 0; i < oldDatastoreVisits.size(); i++) {
            Visit expected = oldDatastoreVisits.get(i);
            Visit actual;
            try {
                actual = newDatastoreVisits.get(i);
            } catch (IndexOutOfBoundsException e) {
                // New data was added since the forklift
                printViolation(VISIT_TABLE_NAME, "null", expected.toString());
                nbOfVisitInconsistencies++;

                insertNewVisitIntoSQLite(expected);
                newDatastoreVisits.add(i, expected);

                continue;
            }

            if (actual != null && !actual.equals(expected)) {
                // Inconsistency for a specific row between new and old datastores
                printViolation(VISIT_TABLE_NAME, actual.toString(), expected.toString());
                nbOfVisitInconsistencies++;

                fixInconsistencyInVisits(actual.getId(), expected);
                newDatastoreVisits.set(i, expected);
            }
        }
    }

	private static void printViolation(String tableName, String actual, String expected) {
        System.out.println("\nInconsistency detected for table " + tableName + ": ");
        System.out.println("[Actual]: " + actual);
        System.out.println("[Expected]: " + expected);
    }

	private static void insertNewOwnerIntoSQLite(Owner expected) {
        System.out.println("<SQLite> Inserting new owner in table: " + OWNER_TABLE_NAME);
        TDGSQLite.addOwner(
            expected.getFirstName(),
            expected.getLastName(),
            expected.getAddress(),
            expected.getCity(),
            expected.getTelephone()
        );
    }

    private static void fixInconsistencyInOwners(int id, Owner expected) {
        System.out.println("<SQLite> Updating owner in table: " + OWNER_TABLE_NAME);
        TDGSQLite.updateOwner(
            id,
            expected.getFirstName(),
            expected.getLastName(),
            expected.getAddress(),
            expected.getCity(),
            expected.getTelephone()
        );
    }

    private static void insertNewPetIntoSQLite(Pet expected) {
        System.out.println("<SQLite> Inserting new pet in table: " + PET_TABLE_NAME);
        TDGSQLite.addPet(
            expected.getName(),
            Date.valueOf(expected.getBirthDate()),
            expected.getId(),
            expected.getOwner().getId()
        );
    }

    private static void fixInconsistencyInPets(int id, Pet expected) {
        System.out.println("<SQLite> Updating pet in table: " + PET_TABLE_NAME);
        TDGSQLite.updatePet(
            id,
            expected.getName(),
            Date.valueOf(expected.getBirthDate()),
            expected.getId(),
            expected.getOwner().getId()
        );
    }

    private static void insertNewVisitIntoSQLite(Visit expected) {
        System.out.println("<SQLite> Inserting new visit in table: " + VISIT_TABLE_NAME);
        TDGSQLite.addVisit(
            expected.getId(),
            expected.getPetId(),
            Date.valueOf(expected.getDate()),
            expected.getDescription()
        );
    }

    private static void fixInconsistencyInVisits(int id, Visit expected) {
        System.out.println("<SQLite> Updating visit in table: " + VISIT_TABLE_NAME);
        TDGSQLite.updateVisit(
            id,
            expected.getPetId(),
            Date.valueOf(expected.getDate()),
            expected.getDescription()
        );
    }

    private static void resetInconsistencyCounters() {
        nbOfOwnerInconsistencies = 0;
        nbOfPetInconsistencies = 0;
        nbOfVisitInconsistencies = 0;
    }

    public static int getNbOfInconcistencies() {
        return nbOfOwnerInconsistencies +
               nbOfPetInconsistencies +
               nbOfVisitInconsistencies;
    }

    public static int getNbOfOwnerInconsistencies() {
        return nbOfOwnerInconsistencies;
    }

    public static int getNbOfPetInconsistencies() {
        return nbOfPetInconsistencies;
    }

    public static int getNbOfVisitInconsistencies() {
        return nbOfVisitInconsistencies;
    }

    public static boolean shadowWritesAndReadsConsistencyCheckerOwner(Owner oldDatastoreOwner, Owner newDatastoreOwner) throws SQLException{

        if(!oldDatastoreOwner.equals(newDatastoreOwner)) {
            System.out.println("Inconsistency detected for owner: ");
            System.out.println("[Actual]: " + newDatastoreOwner.toString());
            System.out.println("[Expected]: " + oldDatastoreOwner.toString());

//            nbOfOwnerInconsistencies++;

            TDGSQLite.updateOwner(oldDatastoreOwner.getId(), oldDatastoreOwner.getFirstName(), oldDatastoreOwner.getLastName(),
                oldDatastoreOwner.getAddress(), oldDatastoreOwner.getCity(), oldDatastoreOwner.getTelephone());

            return false;
        }

        return true;
    }

    public static boolean shadowWritesAndReadsConsistencyCheckerPet(Pet oldDatastorePet, Pet newDatastorePet) throws SQLException{

        if(!oldDatastorePet.equals(newDatastorePet)) {
            System.out.println("Inconsistency detected for pet: ");
            System.out.println("[Actual]: " + newDatastorePet.toString());
            System.out.println("[Expected]: " + oldDatastorePet.toString());

//            nbOfOwnerInconsistencies++;

            TDGSQLite.updatePet(oldDatastorePet.getId(), oldDatastorePet.getName(), Date.valueOf(oldDatastorePet.getBirthDate()),
                oldDatastorePet.getType().getId(), oldDatastorePet.getOwner().getId());
            return false;
        }
        return true;
    }

    public static boolean shadowWritesAndReadsConsistencyCheckerVisit(Visit oldDatastoreVisit, Visit newDatastoreVisit) throws SQLException{

        if(!oldDatastoreVisit.equals(newDatastoreVisit)) {
            System.out.println("Inconsistency detected for visit: ");
            System.out.println("[Actual]: " + newDatastoreVisit.toString());
            System.out.println("[Expected]: " + oldDatastoreVisit.toString());

//            nbOfOwnerInconsistencies++;

            TDGSQLite.updateVisit(oldDatastoreVisit.getId(), oldDatastoreVisit.getPetId(), Date.valueOf(oldDatastoreVisit.getDate()),
                oldDatastoreVisit.getDescription());
            return false;
        }
        return true;

    }

    public static boolean shadowWritesAndReadsConsistencyCheckerVet(Vet oldDatastoreVet, Vet newDatastoreVet) throws SQLException {

        if(!oldDatastoreVet.equals(newDatastoreVet)) {
            System.out.println("Inconsistency detected for vet: ");
            System.out.println("[Actual]: " + newDatastoreVet.toString());
            System.out.println("[Expected]: " + oldDatastoreVet.toString());

//            nbOfOwnerInconsistencies++;

            TDGSQLite.updateVet(oldDatastoreVet.getId(), oldDatastoreVet.getFirstName(), oldDatastoreVet.getLastName());

            return false;
        }
        return true;
    }

    public static boolean shadowWritesAndReadsConsistencyCheckerOwners(Collection<Owner> oldDatastoreOwners) {
        int countInconsistencies = 0;

        for (Owner owner: oldDatastoreOwners){
            Owner actual;
            try{
                actual = TDGSQLite.getOwner(owner.getId());

                if(actual != null){
                    if(!owner.equals(actual)){
                        System.out.println("Inconsistency detected for owner: ");
                        System.out.println("[Actual]: " + actual.toString());
                        System.out.println("[Expected]: " + owner.toString());

                        //            nbOfOwnerInconsistencies++;

                        TDGSQLite.updateOwner(owner.getId(), owner.getFirstName(), owner.getLastName(),
                            owner.getAddress(), owner.getCity(), owner.getTelephone());

                        countInconsistencies++;
                    }
                }
            }
            catch(IndexOutOfBoundsException e){
                TDGSQLite.addOwner(owner.getFirstName(), owner.getLastName(),
                    owner.getAddress(), owner.getCity(), owner.getTelephone());
            }
        }

        if(countInconsistencies > 0){
            return false;
        }

        return true;
    }

}
