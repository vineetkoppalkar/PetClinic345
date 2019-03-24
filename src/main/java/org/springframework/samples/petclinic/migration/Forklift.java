package org.springframework.samples.petclinic.migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class Forklift implements Runnable {
    public void run() {
        System.out.println("Forklift running");
        try {
            constructDatabase("jdbc:sqlite:memory", "sqlite");
            forkliftDatabase();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        System.out.println("Forklift has finished.");

    }

    public static void constructDatabase(String root, String dbName) throws SQLException, IOException {
        // create a connection to the database
        Connection c = DriverManager.getConnection(root);
        FileReader fr = new FileReader(new File("src/main/resources/db/" + dbName + "/schema.sql"));
        BufferedReader br = new BufferedReader(fr);
        executeSQL(c, br);
    }

    public static void fakeData(String root) throws SQLException, IOException {
        // create a connection to the database
        Connection c = DriverManager.getConnection(root);
        FileReader fr = new FileReader(new File("src/main/resources/db/hsqldb/data.sql"));
        BufferedReader br = new BufferedReader(fr);
        executeSQL(c, br);
    }

    public static int forkliftDatabase() {
        ResultSet owners = TDGHSQL.forkliftAllOwners();
        ResultSet types = TDGHSQL.forkliftAllTypes();
        ResultSet pets = TDGHSQL.forkliftAllPets();
        ResultSet specialties = TDGHSQL.forkliftAllSpecialties();
        ResultSet vets = TDGHSQL.forkliftAllVets();
        ResultSet vetSpecialties = TDGHSQL.forkliftAllVetSpecialties();
        ResultSet visits = TDGHSQL.forkliftAllVisits();
        Integer forkliftCounter = 0; // Keep track of tables that are forklifted

        try {
            while (owners != null && owners.next()) {
                Integer id = owners.getInt("id");
                String firstName = owners.getString("first_name");
                String lastName = owners.getString("last_name");
                String address = owners.getString("address");
                String city = owners.getString("city");
                String telephone = owners.getString("telephone");
                TDGSQLite.addOwner(firstName, lastName, address, city, telephone);
            }
            // owners forklifted
            forkliftCounter++;
            while (types != null && types.next()) {
                Integer id = types.getInt("id");
                String name = types.getString("name");
                TDGSQLite.addPetType(name);
            }
            // types forklifted
            forkliftCounter++;
            while(pets != null && pets.next()) {
                Integer id = pets.getInt("id");
                String name = pets.getString("name");
                Date birthDate = pets.getDate("birth_date");
                Integer typeId = pets.getInt("type_id");
                Integer ownerId = pets.getInt("owner_id");
                TDGSQLite.addPet(name, birthDate, typeId, ownerId);
            }
            //pets forklifted
            forkliftCounter++;
            while(specialties != null && specialties.next()) {
                Integer id = specialties.getInt("id");
                String name = specialties.getString("name");
                TDGSQLite.addSpecialty(name);
            }
            //specialties forklifted
            forkliftCounter++;
            while(vets != null && vets.next()) {
                Integer id = vets.getInt("id");
                String firstName = vets.getString("first_name");
                String lastName = vets.getString("last_name");
                TDGSQLite.addVet(firstName, lastName);
            }
            //vets forklifted
            forkliftCounter++;
            while(vetSpecialties != null && vetSpecialties.next()) {
                Integer vetId = vetSpecialties.getInt("vet_id");
                Integer specialtyId = vetSpecialties.getInt("specialty_id");
                TDGSQLite.addVetSpecialty(vetId, specialtyId);
            }
            //vet specialties forklifted
            forkliftCounter++;
            while(visits != null && visits.next()) {
                Integer id = visits.getInt("id");
                Integer petId = visits.getInt("pet_id");
                Date visitDate = visits.getDate("visit_date");
                String description = visits.getString("description");
                TDGSQLite.addVisit(id, petId, visitDate, description);
            }
            //visits forklifted
            forkliftCounter++;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return forkliftCounter;
    }

    private static void executeSQL(Connection c, BufferedReader br) throws IOException, SQLException {
        StringBuilder sb = new StringBuilder();
        String command;
        while ((command = br.readLine()) != null) {
            sb.append(command);
        }
        br.close();
        String[] commandList = sb.toString().split(";");
        Statement st = c.createStatement();

        for (String cmd : commandList) {
            if (!cmd.trim().equals("")) {
                st.executeUpdate(cmd);
            }
        }
    }
}


