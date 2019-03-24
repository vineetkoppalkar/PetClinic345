package org.springframework.samples.petclinic.migration;

import org.springframework.samples.petclinic.PetClinicApplication;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.vet.Specialty;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.visit.Visit;

import java.sql.*;
import java.util.List;

public class ConsistencyChecker implements Runnable {

    private static final String OWNER_TABLE_NAME = "owners";
    private static final String PET_TABLE_NAME = "pets";
    private static final String VISIT_TABLE_NAME = "visits";
    private static final String VET_TABLE_NAME = "vets";
    private static final String SPECIALITIES_TABLE_NAME = "specialties";
    private static final String TYPES_TABLE_NAME = "types";

    private static int nbOfOwnerInconsistencies;
    private static int nbOfPetInconsistencies;
    private static int nbOfVisitInconsistencies;
    private static int nbOfVetInconsistencies;
    private static int nbOfSpecialtiesInconsistencies;
    private static int nbOfTypeInconsistencies;

    @Override
    public void run() {
        System.out.println("Starting consistency checkers");
        resetInconsistencyCounters();

        if (PetClinicApplication.consistencyCheckerOwner) {
            System.out.println("\nConsistency checker RUNNING for table: " + OWNER_TABLE_NAME);
            ownerCheckConsistency();
            System.out.println("Consistency checker COMPLETE for table: " + OWNER_TABLE_NAME);
        }

        if (PetClinicApplication.consistencyCheckerPet) {
            System.out.println("\nConsistency checker RUNNING for table: " + PET_TABLE_NAME);
            petCheckConsistency();
            System.out.println("Consistency checker COMPLETE for table: " + PET_TABLE_NAME);
        }

        if (PetClinicApplication.consistencyCheckerVisit) {
            System.out.println("\nConsistency checker RUNNING for table: " + VISIT_TABLE_NAME);
            visitCheckConsistency();
            System.out.println("Consistency checker COMPLETE for table: " + VISIT_TABLE_NAME);
        }

        if (PetClinicApplication.consistencyCheckerVet) {
            System.out.println("\nConsistency checker RUNNING for table: " + VET_TABLE_NAME);
            vetCheckConsistency();
            System.out.println("Consistency checker COMPLETE for table: " + VET_TABLE_NAME);
        }

        if (PetClinicApplication.consistencyCheckerSpecialty) {
            System.out.println("\nConsistency checker RUNNING for table: " + SPECIALITIES_TABLE_NAME);
            specialtiesCheckConsistency();
            System.out.println("Consistency checker COMPLETE for table: " + SPECIALITIES_TABLE_NAME);
        }

        if (PetClinicApplication.consistencyCheckerType) {
            System.out.println("\nConsistency checker RUNNING for table: " + TYPES_TABLE_NAME);
            typesCheckConsistency();
            System.out.println("Consistency checker COMPLETE for table: " + TYPES_TABLE_NAME);
        }
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
                printViolation(OWNER_TABLE_NAME, "null", expected.displayInfo());
                nbOfOwnerInconsistencies++;

                insertNewOwnerIntoSQLite(expected);
                newDatastoreOwners.add(i, expected);

                continue;
            }

            if (actual != null && !actual.equals(expected)) {
                // Inconsistency for a specific row between new and old datastores
                printViolation(OWNER_TABLE_NAME, actual.displayInfo(), expected.displayInfo());
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
                printViolation(PET_TABLE_NAME, "null", expected.displayInfo());
                nbOfPetInconsistencies++;

                insertNewPetIntoSQLite(expected);
                newDatastorePets.add(i, expected);

                continue;
            }

            if (actual != null && !actual.equals(expected)) {
                // Inconsistency for a specific row between new and old datastores
                printViolation(PET_TABLE_NAME, actual.displayInfo(), expected.displayInfo());
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
                printViolation(VISIT_TABLE_NAME, "null", expected.displayInfo());
                nbOfVisitInconsistencies++;

                insertNewVisitIntoSQLite(expected);
                newDatastoreVisits.add(i, expected);

                continue;
            }

            if (actual != null && !actual.equals(expected)) {
                // Inconsistency for a specific row between new and old datastores
                printViolation(VISIT_TABLE_NAME, actual.displayInfo(), expected.displayInfo());
                nbOfVisitInconsistencies++;

                fixInconsistencyInVisits(actual.getId(), expected);
                newDatastoreVisits.set(i, expected);
            }
        }
    }

    public void vetCheckConsistency() {
        List<Vet> oldDatastoreVets = TDGHSQL.getAllVets();
        List<Vet> newDatastoreVets = TDGSQLite.getAllVets();

        for (int i = 0; i < oldDatastoreVets.size(); i++) {
            Vet expected = oldDatastoreVets.get(i);
            Vet actual;
            try {
                actual = newDatastoreVets.get(i);
            } catch (IndexOutOfBoundsException e) {
                // New data was added since the forklift
                printViolation(VET_TABLE_NAME, "null", expected.displayInfo());
                nbOfVetInconsistencies++;

                insertNewVetIntoSQLite(expected);
                newDatastoreVets.add(i, expected);

                continue;
            }

            if (actual != null && !actual.equals(expected)) {
                // Inconsistency for a specific row between new and old datastores
                printViolation(VET_TABLE_NAME, actual.displayInfo(), expected.displayInfo());
                nbOfVetInconsistencies++;

                fixInconsistencyInVets(actual.getId(), expected);
                newDatastoreVets.set(i, expected);
            }
        }
    }

    public void specialtiesCheckConsistency() {
        List<Specialty> oldDatastoreSpecialties = TDGHSQL.getAllSpecialties();
        List<Specialty> newDatastoreSpecialties = TDGSQLite.getAllSpecialties();

        for (int i = 0; i < oldDatastoreSpecialties.size(); i++) {
            Specialty expected = oldDatastoreSpecialties.get(i);
            Specialty actual;
            try {
                actual = newDatastoreSpecialties.get(i);
            } catch (IndexOutOfBoundsException e) {
                // New data was added since the forklift
                printViolation(SPECIALITIES_TABLE_NAME, "null", expected.displayInfo());
                nbOfSpecialtiesInconsistencies++;

                insertNewSpecialtyIntoSQLite(expected);
                newDatastoreSpecialties.add(i, expected);

                continue;
            }

            if (actual != null && !actual.equals(expected)) {
                // Inconsistency for a specific row between new and old datastores
                printViolation(SPECIALITIES_TABLE_NAME, actual.displayInfo(), expected.displayInfo());
                nbOfSpecialtiesInconsistencies++;

                fixInconsistencyInSpecialties(actual.getId(), expected);
                newDatastoreSpecialties.set(i, expected);
            }
        }
    }

    public void typesCheckConsistency() {
        List<PetType> oldDatastoreTypes = TDGHSQL.getAllTypes();
        List<PetType> newDatastoreTypes = TDGSQLite.getAllTypes();

        for (int i = 0; i < oldDatastoreTypes.size(); i++) {
            PetType expected = oldDatastoreTypes.get(i);
            PetType actual;
            try {
                actual = newDatastoreTypes.get(i);
            } catch (IndexOutOfBoundsException e) {
                // New data was added since the forklift
                printViolation(TYPES_TABLE_NAME, "null", expected.displayInfo());
                nbOfTypeInconsistencies++;

                insertNewTypeIntoSQLite(expected);
                newDatastoreTypes.add(i, expected);

                continue;
            }

            if (actual != null && !actual.equals(expected)) {
                // Inconsistency for a specific row between new and old datastores
                printViolation(TYPES_TABLE_NAME, actual.displayInfo(), expected.displayInfo());
                nbOfTypeInconsistencies++;

                fixInconsistencyInTypes(actual.getId(), expected);
                newDatastoreTypes.set(i, expected);
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

    private static void insertNewVetIntoSQLite(Vet expected) {
        System.out.println("<SQLite> Inserting new visit in table: " + VET_TABLE_NAME);
        TDGSQLite.addVet(
            expected.getFirstName(),
            expected.getLastName()
        );
    }

    private static void fixInconsistencyInVets(int id, Vet expected) {
        System.out.println("<SQLite> Updating visit in table: " + VET_TABLE_NAME);
        TDGSQLite.updateVet(
            id,
            expected.getFirstName(),
            expected.getLastName()
        );
    }

    private static void insertNewSpecialtyIntoSQLite(Specialty expected) {
        System.out.println("<SQLite> Inserting new visit in table: " + SPECIALITIES_TABLE_NAME);
        TDGSQLite.addSpecialty(
            expected.getName()
        );
    }

    private static void fixInconsistencyInSpecialties(int id, Specialty expected) {
        System.out.println("<SQLite> Updating visit in table: " + SPECIALITIES_TABLE_NAME);
        TDGSQLite.updatedSpecialty(
            id,
            expected.getName()
        );
    }

    private static void insertNewTypeIntoSQLite(PetType expected) {
        System.out.println("<SQLite> Inserting new visit in table: " + TYPES_TABLE_NAME);
        TDGSQLite.addPetType(
            expected.getName()
        );
    }

    private static void fixInconsistencyInTypes(int id, PetType expected) {
        System.out.println("<SQLite> Updating visit in table: " + TYPES_TABLE_NAME);
        TDGSQLite.updatePetType(
            id,
            expected.getName()
        );
    }

    private static void resetInconsistencyCounters() {
        nbOfOwnerInconsistencies = 0;
        nbOfPetInconsistencies = 0;
        nbOfVisitInconsistencies = 0;
        nbOfVetInconsistencies = 0;
        nbOfSpecialtiesInconsistencies = 0;
        nbOfTypeInconsistencies = 0;
    }

    public static int getNbOfInconcistencies() {
        return nbOfOwnerInconsistencies +
               nbOfPetInconsistencies +
               nbOfVisitInconsistencies +
               nbOfVetInconsistencies +
               nbOfSpecialtiesInconsistencies +
               nbOfTypeInconsistencies;
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

    public static int getNbOfVetInconsistencies() {
        return nbOfVetInconsistencies;
    }

    public static int getNbOfSpecialtiesInconsistencies() {
        return nbOfSpecialtiesInconsistencies;
    }

    public static int getNbOfTypeInconsistencies() {
        return nbOfTypeInconsistencies;
    }
}
