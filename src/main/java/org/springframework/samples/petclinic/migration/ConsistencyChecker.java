package org.springframework.samples.petclinic.migration;

import org.springframework.samples.petclinic.owner.Owner;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConsistencyChecker implements Runnable {

    private int nbOfOwnerInconsistencies;

    @Override
    public void run() {
        System.out.println("Consistency checker running");
        resetInconsistencyCounters();
        ownerCheckConsistency();
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
                printViolation("owners", "null", expected.toString());
                nbOfOwnerInconsistencies++;

                insertNewOwnerIntoSQLite(expected);
                newDatastoreOwners.add(i, expected);

                continue;
            }

            if (actual != null && !actual.equals(expected)) {
                // Inconsistency for a specific owner between new and old datastores
                printViolation("owners", actual.toString(), expected.toString());
                nbOfOwnerInconsistencies++;

                fixInconsistencyInOwners(actual.getId(), expected);
                newDatastoreOwners.set(i, expected);
            }
        }
	}

	private void printViolation(String tableName, String actual, String expected) {
        System.out.println("\nInconsistency detected for table " + tableName + ": ");
        System.out.println("[Actual]: " + actual);
        System.out.println("[Expected]: " + expected);
    }

	private void insertNewOwnerIntoSQLite(Owner expected) {
        TDGSQLite.addOwner(
            expected.getFirstName(),
            expected.getLastName(),
            expected.getAddress(),
            expected.getCity(),
            expected.getTelephone()
        );
    }

    private void fixInconsistencyInOwners(int id, Owner expected) {
        TDGSQLite.updateOwner(
            id,
            expected.getFirstName(),
            expected.getLastName(),
            expected.getAddress(),
            expected.getCity(),
            expected.getTelephone()
        );
    }

    private void resetInconsistencyCounters() {
        nbOfOwnerInconsistencies = 0;
    }

    public int getNbOfInconcistencies() {
        return nbOfOwnerInconsistencies;
    }

    public int getNbOfOwnerInconsistencies() {
        return nbOfOwnerInconsistencies;
    }

}
