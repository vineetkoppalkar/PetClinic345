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

    private static void populate() {
        try {
            Forklift.constructDatabase("jdbc:sqlite:test");
            Forklift.fakeData("jdbc:sqlite:test");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

    }
}
