package org.springframework.samples.petclinic.migration;

import java.io.IOException;
import java.sql.*;
import org.springframework.samples.petclinic.migration.Forklift;
import org.springframework.samples.petclinic.vet.Vet;

public class TDGSQLite {

    private static String root = "jdbc:sqlite:memory";
    private static Connection sqlite = init();

    public TDGSQLite(String root) {
        TDGSQLite.root = root;
        TDGSQLite.sqlite = init();
        TDGSQLite.populate();
    }

    private static Connection init() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(root);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return conn;
    }

    public static ResultSet selectQuery(String s) {

        Statement stmt;
        ResultSet rs = null;
        try {
            stmt = sqlite.createStatement();
            rs    = stmt.executeQuery(s);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    private static void populate() {
        try {
            Forklift.constructDatabase("jdbc:sqlite:test");
            Forklift.forkliftDatabase("jdbc:sqlite:test");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

    }
    
    public static void addVet(String firstName, String lastName) {
    	selectQuery("INSERT INTO vets id, first_name, last_name VALUES (NULL, " + firstName + ", " + lastName + ");");
    }
    
    public static Vet getVet(int id) {
    	ResultSet rs = selectQuery("SELECT * FROM vets WHERE id=" + String.valueOf(id) + ";");
    	if(rs != null) {
    		Vet vet = new Vet();
    		vet.setId(rs.getInt('id'));
    		vet.setFirstName(rs.getString('first_name'));
    		vet.setLastName(rs.getString('last_name'));
    		rs = selectQuery("SELECT specialty_id FROM vet_specialties WHERE vet_id=" + String.valueOf(id) + ";");
    		if(rs != null) {
    			ResultSet specialty = selectQuery("SELECT name FROM specialties WHERE id=" + String.valueOf(rs.getInt('specialty_id')) +";");
    			if(specialty != null) {
    				vet.addSpecialty(specialty.getString('name'));
    			}
    		}		
    		return vet;
    	}
    	return null;
    }
    
    public static void updateVet(int id, String firstName, String lastName) {
    	selectQuery("UPDATE vets SET first_name = " + firstName + ", last_name = " + lastName + " WHERE id = " + String.valueOf(id) + ";");
    }
    
    public static void deleteVet(int id) {
    	selectQuery("DELETE FROM vets WHERE id=" + String.valueOf(id) + ";");
    }
    
    public static void addSpecialty(String specialty) {
    	selectQuery("INSERT INTO specialties id, name VALUES (NULL, " + specialty + ");");
    }
    
    public static void addVetSpecialty(int vet_id, int specialty_id) {
    	selectQuery("INSERT INTO vet_specialties vet_id, specialty_id VALUES ("+ String.valueOf(vet_id) + ", "+ String.valueOf(specialty_id) + ");");
    }
    
    
    
    
    
    
    
    
}
