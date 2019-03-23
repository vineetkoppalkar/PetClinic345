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
import java.util.List;

public class ConsistencyChecker implements Runnable {

    private final String OWNER_TABLE_NAME = "owners";
    private final String PET_TABLE_NAME = "pets";

    private int nbOfOwnerInconsistencies;
    private int nbOfPetInconsistencies;

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

	private void printViolation(String tableName, String actual, String expected) {
        System.out.println("\nInconsistency detected for table " + tableName + ": ");
        System.out.println("[Actual]: " + actual);
        System.out.println("[Expected]: " + expected);
    }

	private void insertNewOwnerIntoSQLite(Owner expected) {
        System.out.println("<SQLite> Inserting new owner in table: " + OWNER_TABLE_NAME);
        TDGSQLite.addOwner(
            expected.getFirstName(),
            expected.getLastName(),
            expected.getAddress(),
            expected.getCity(),
            expected.getTelephone()
        );
    }

    private void fixInconsistencyInOwners(int id, Owner expected) {
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

    private void insertNewPetIntoSQLite(Pet expected) {
        System.out.println("<SQLite> Inserting new pet in table: " + PET_TABLE_NAME);
        TDGSQLite.addPet(
            expected.getName(),
            Date.valueOf(expected.getBirthDate()),
            expected.getId(),
            expected.getOwner().getId()
        );
    }

    private void fixInconsistencyInPets(int id, Pet expected) {
        System.out.println("<SQLite> Updating pet in table: " + PET_TABLE_NAME);
        TDGSQLite.updatePet(
            id,
            expected.getName(),
            Date.valueOf(expected.getBirthDate()),
            expected.getId(),
            expected.getOwner().getId()
        );
    }

    private void resetInconsistencyCounters() {
        nbOfOwnerInconsistencies = 0;
        nbOfPetInconsistencies = 0;
    }

    public int getNbOfInconcistencies() {
        return nbOfOwnerInconsistencies +
               nbOfPetInconsistencies;
    }

    public int getNbOfOwnerInconsistencies() {
        return nbOfOwnerInconsistencies;
    }

    public int getNbOfPetInconsistencies() {
        return nbOfPetInconsistencies;
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

}
