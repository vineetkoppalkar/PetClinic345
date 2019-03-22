package org.springframework.samples.petclinic.migration;

import java.sql.*;

public class TDGHSQL {

    private static String root = "jdbc:hsqldb:mem:petclinic;readonly=true";
    private static Connection hsqldb = init();

    private static Connection init() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(root);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return conn;
    }

    public static ResultSet getAllOwners() {
        String sql = "SELECT * FROM owners;";
        return getRecords(sql);
    }

    public static ResultSet getAllTypes() {
        String sql = "SELECT * FROM types;";
        return getRecords(sql);
    }

    public static ResultSet getAllPets() {
        String sql = "SELECT * FROM pets;";
        return getRecords(sql);
    }

    public static ResultSet getAllSpecialties() {
        String sql = "SELECT * FROM specialties;";
        return getRecords(sql);
    }

    public static ResultSet getAllVets() {
        String sql = "SELECT * FROM vets;";
        return getRecords(sql);
    }

    public static ResultSet getAllVetSpecialties() {
        String sql = "SELECT * FROM vet_specialties;";
        return getRecords(sql);
    }

    public static ResultSet getAllVisits() {
        String sql = "SELECT * FROM visits;";
        return getRecords(sql);
    }

    public static ResultSet getRecords(String sql) {
        Statement stmt;
        try {
            stmt = hsqldb.createStatement();
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }



}
