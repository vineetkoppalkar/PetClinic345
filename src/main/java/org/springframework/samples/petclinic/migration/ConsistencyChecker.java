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

        try {
            // TODO loop over the owner table
            ownerCheckConsistency();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

	public void ownerCheckConsistency() throws SQLException {

        List<Owner> oldDatastoreOwners = TDGHSQL.getAllOwners();
        List<Owner> newDatastoreOwners = TDGSQLite.getAllOwners();

        for (int i = 0; i < oldDatastoreOwners.size(); i++) {
            Owner expected = oldDatastoreOwners.get(i);
            Owner actual = newDatastoreOwners.get(i);

            if(!actual.equals(expected)) {
                System.out.println("Inconsistency detected for owner: ");
                System.out.println("[Actual]: " + actual.toString());
                System.out.println("[Expected]: " + expected.toString());

                nbOfOwnerInconsistencies++;

                fixInconsistencyInOwners(actual.getId(), expected);
                oldDatastoreOwners.set(i, expected);
            }
        }
	}

    private void fixInconsistencyInOwners(int id, Owner expected) {
        TDGSQLite.updateOwner(id,
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
