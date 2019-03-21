package org.springframework.samples.petclinic.migration;

import java.io.IOException;
import java.sql.*;
import org.springframework.samples.petclinic.migration.Forklift;

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
}
