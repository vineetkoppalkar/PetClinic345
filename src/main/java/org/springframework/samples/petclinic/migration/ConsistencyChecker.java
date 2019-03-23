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

    private int nbOfOwnerInconsistencies;

    private int sumOfInconsistencies = nbOfOwnerInconsistencies;

    private final String URL_HSQLDB = "jdbc:hsqldb:mem:petclinic;readonly=true";
    private final String USER = "sa";
    private final String PASSWORD = "";

    private final String URL_SQLite = "jdbc:sqlite:memory";
    private TDGSQLite tdg;

    @Override
    public void run() {
        System.out.println("Consistency checker running");
        tdg = new TDGSQLite(URL_SQLite);
        resetInconsistencyCounters();

        try {
            // TODO loop over the owner table
            ownerCheckConsistency();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

	private void ownerCheckConsistency() throws SQLException {

        List<Owner> oldDatastoreOwners;
        List<Owner> newDatastoreOwners;

        oldDatastoreOwners = getOwnersFromDatastore(URL_HSQLDB);
        newDatastoreOwners = getOwnersFromDatastore(URL_SQLite);

        for (int i = 0; i < newDatastoreOwners.size(); i++) {
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

	private List<Owner> getOwnersFromDatastore(String url) throws SQLException {

        List<Owner> results = new ArrayList<>();

        Connection c;
        if (url.equals(URL_HSQLDB)) {
            c =  DriverManager.getConnection(URL_HSQLDB, USER, PASSWORD);
        } else {
            c = DriverManager.getConnection(URL_SQLite);
        }

        Statement st = c.createStatement();
        String query = "select * from owners";
        ResultSet rs = st.executeQuery(query);
        while (rs.next()) {

            int id = rs.getInt("id");
            String first_name = rs.getString("first_name");
            String last_name = rs.getString("last_name");
            String address = rs.getString("address");
            String city = rs.getString("city");
            String telephone = rs.getString("telephone");

//            System.out.println("@@ " + id + " " + first_name + " " + last_name + " " + address + " " + city + " " + telephone);

            results.add(new Owner(id, first_name, last_name, address, city, telephone));
        }
        return results;
    }

    private void fixInconsistencyInOwners(int id, Owner expected) throws SQLException {
        Connection c = DriverManager.getConnection(URL_SQLite);
        Statement st = c.createStatement();
        String query = "update owners SET " +
                        "first_name = '" + expected.getFirstName() + "', " +
                        "last_name = '" + expected.getLastName() + "', " +
                        "address = '" + expected.getAddress() + "', " +
                        "city = '" + expected.getCity() + "', " +
                        "telephone = '" + expected.getTelephone() + "' " +
                        "WHERE id = " + id;

        st.executeQuery(query);
    }

    private void resetInconsistencyCounters() {
        nbOfOwnerInconsistencies = 0;
    }


    public static boolean shadowWritesAndReadsConsistencyCheckerOwner(Owner oldDatastoreOwner, Owner newDatastoreOwner) throws SQLException{

        if(!oldDatastoreOwner.equals(newDatastoreOwner)) {
            System.out.println("Inconsistency detected for owner: ");
            System.out.println("[Actual]: " + newDatastoreOwner.toString());
            System.out.println("[Expected]: " + oldDatastoreOwner.toString());

            //Cannot increment this, either make the whole thing static or everything dynamic
//            nbOfOwnerInconsistencies++;

            TDGSQLite.updateOwner(oldDatastoreOwner.getId(), oldDatastoreOwner.getFirstName(), oldDatastoreOwner.getLastName(),
                oldDatastoreOwner.getAddress(), oldDatastoreOwner.getCity(), oldDatastoreOwner.getTelephone());

            return false;
        }

        return true;
    }

    public static boolean shadowWritesAndReadsConsistencyCheckerPet(Pet oldDatastorePet, Pet newDatastorePet) throws SQLException{

        //TODO: Add a equals method in Pet class
        if(!oldDatastorePet.equals(newDatastorePet)) {
            System.out.println("Inconsistency detected for pet: ");
            System.out.println("[Actual]: " + newDatastorePet.toString());
            System.out.println("[Expected]: " + oldDatastorePet.toString());

            //Cannot increment this, either make the whole thing static or everything dynamic
//            nbOfOwnerInconsistencies++;

            TDGSQLite.updatePet(oldDatastorePet.getId(), oldDatastorePet.getName(), Date.valueOf(oldDatastorePet.getBirthDate()),
                oldDatastorePet.getType().getId(), oldDatastorePet.getOwner().getId());
            return false;
        }
        return true;
    }

    public static boolean shadowWritesAndReadsConsistencyCheckerVisit(Visit oldDatastoreVisit, Visit newDatastoreVisit) throws SQLException{

        //TODO: add an equals method for Visit
        if(!oldDatastoreVisit.equals(newDatastoreVisit)) {
            System.out.println("Inconsistency detected for visit: ");
            System.out.println("[Actual]: " + newDatastoreVisit.toString());
            System.out.println("[Expected]: " + oldDatastoreVisit.toString());

            //Cannot increment this, either make the whole thing static or everything dynamic
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

            //Cannot increment this, either make the whole thing static or everything dynamic
//            nbOfOwnerInconsistencies++;

            TDGSQLite.updateVet(oldDatastoreVet.getId(), oldDatastoreVet.getFirstName(), oldDatastoreVet.getLastName());

            return false;
        }
        return true;
    }

    public static boolean shadowWritesAndReadsConsistencyCheckerOwners(Collection<Owner> oldDatastoreOwners,Collection<Owner> newDatastoreOwners) throws SQLException {

        if(oldDatastoreOwners.size() == newDatastoreOwners.size()) {
            for (int i = 0; i < newDatastoreOwners.size(); i++) {
                Owner expected = (Owner)oldDatastoreOwners.toArray()[i];
                Owner actual = (Owner)newDatastoreOwners.toArray()[i];
                //TODO: add an equals method for Visit
                if(!actual.equals(expected)) {
                    System.out.println("Inconsistency detected for visit: ");
                    System.out.println("[Actual]: " + newDatastoreOwners.toString());
                    System.out.println("[Expected]: " + oldDatastoreOwners.toString());

                    //Cannot increment this, either make the whole thing static or everything dynamic
//            nbOfOwnerInconsistencies++;
                    TDGSQLite.updateOwner(expected.getId(), expected.getFirstName(), expected.getLastName(), expected.getAddress(),
                    expected.getCity(), expected.getTelephone());
                    return false;
                }
            }
            return true;
        }
        else {
            //If the size of the lists are not equal, wipe everything in the new datastore and replace with the ones from olddb
            for (Owner owner : newDatastoreOwners) {
                //            nbOfOwnerInconsistencies++;
                TDGSQLite.deleteOwner(owner.getId());
            }
            for(Owner owner : oldDatastoreOwners){
                TDGSQLite.addOwner(owner.getFirstName(), owner.getLastName(), owner.getAddress(), owner.getCity(),
                owner.getTelephone());
            }
            return false;
        }
    }

}
