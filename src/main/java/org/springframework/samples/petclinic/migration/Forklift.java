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
            constructDatabase("jdbc:sqlite:memory");
            forkliftDatabase();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    static void constructDatabase(String root) throws SQLException, IOException {
        // create a connection to the database
        Connection c = DriverManager.getConnection(root);
        FileReader fr = new FileReader(new File("src/main/resources/db/sqlite/schema.sql"));
        BufferedReader br = new BufferedReader(fr);
        executeSQL(c, br);
    }

    static void fakeData(String root) throws SQLException, IOException {
        // create a connection to the database
        Connection c = DriverManager.getConnection(root);
        FileReader fr = new FileReader(new File("src/main/resources/db/hsqldb/data.sql"));
        BufferedReader br = new BufferedReader(fr);
        executeSQL(c, br);
    }

    static void forkliftDatabase() {
        ResultSet owners = TDGHSQL.getAllOwners();
        ResultSet types = TDGHSQL.getAllTypes();
        ResultSet pets = TDGHSQL.getAllPets();
        ResultSet specialties = TDGHSQL.getAllSpecialties();
        ResultSet vets = TDGHSQL.getAllVets();
        ResultSet vetSpecialties = TDGHSQL.getAllVetSpecialties();
        ResultSet visits = TDGHSQL.getAllVisits();

        try {
            while (owners.next()) {
                Integer id = owners.getInt("id");
                String firstName = owners.getString("first_name");
                String lastName = owners.getString("last_name");
                String address = owners.getString("address");
                String city = owners.getString("city");
                String telephone = owners.getString("telephone");
                TDGSQLite.addOwner(firstName, lastName, address, city, telephone);
            }
            while (types.next()) {
                Integer id = types.getInt("id");
                String name = types.getString("name");
                TDGSQLite.addPetType(name);
            }
            while(pets.next()) {
                Integer id = pets.getInt("id");
                String name = pets.getString("name");
                Date birthDate = pets.getDate("birth_date");
                Integer typeId = pets.getInt("type_id");
                Integer ownerId = pets.getInt("owner_id");
                TDGSQLite.addPet(name, birthDate, typeId, ownerId);
            }
            while(specialties.next()) {
                Integer id = specialties.getInt("id");
                String name = specialties.getString("name");
                TDGSQLite.addSpecialty(name);
            }
            while(vets.next()) {
                Integer id = vets.getInt("id");
                String firstName = vets.getString("first_name");
                String lastName = vets.getString("last_name");
                TDGSQLite.addVet(firstName, lastName);
            }
            while(vetSpecialties.next()) {
                Integer vetId = vetSpecialties.getInt("vet_id");
                Integer specialtyId = vetSpecialties.getInt("specialty_id");
                TDGSQLite.addVetSpecialty(vetId, specialtyId);
            }
            while(visits.next()) {
                Integer id = visits.getInt("id");
                Integer petId = visits.getInt("pet_id");
                Date visitDate = visits.getDate("visit_date");
                String description = visits.getString("description");
                TDGSQLite.addVisit(id, petId, visitDate, description);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                System.out.println(">>" + cmd);
            }
        }
    }
}


