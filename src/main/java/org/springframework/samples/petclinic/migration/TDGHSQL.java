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
        String sql = "SELECT * FROM OWNERS;";
        Statement stmt;
        ResultSet rs = null;
        try {
            stmt = hsqldb.createStatement();
            rs    = stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

}
