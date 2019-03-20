package org.springframework.samples.petclinic.migration;

import java.sql.*;

public class TDGSQLite {

    private static final String ROOT = "jdbc:sqlite:memory";
    private static final Connection sqlite = init();

    private static Connection init() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(ROOT);
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
}
