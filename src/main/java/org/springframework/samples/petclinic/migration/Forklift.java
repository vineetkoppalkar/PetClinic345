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
            constructDatabase();
            forkliftDatabase();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void constructDatabase() throws SQLException, IOException {
        String url = "jdbc:sqlite:memory";
        // create a connection to the database
        Connection c = DriverManager.getConnection(url);
        FileReader fr = new FileReader(new File("src\\main\\resources\\db\\sqlite\\schema.sql"));
        BufferedReader br = new BufferedReader(fr);
        executeSQL(c, br);
    }

    private static void forkliftDatabase() throws SQLException, IOException {
        String url = "jdbc:sqlite:memory";
        // create a connection to the database
        Connection c = DriverManager.getConnection(url);
        FileReader fr = new FileReader(new File("src\\main\\resources\\db\\hsqldb\\data.sql"));
        BufferedReader br = new BufferedReader(fr);
        executeSQL(c, br);
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


