package org.springframework.samples.petclinic.migration;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.*;

@Component
public class ForkliftRunner {

    private static String root = "jdbc:sqlite:memory";

    @PostConstruct
    public void init() {
        Thread migration = new Thread(new Forklift());
        migration.setPriority(Thread.MIN_PRIORITY);
        migration.start();

//        String url = "jdbc:hsqldb:mem:petclinic;readonly=true";
//        Connection c;
//        try {
//            c = DriverManager.getConnection(url, "sa", "");
//            Statement st = c.createStatement();
//            String query = "select * from owners";
//            ResultSet rs = st.executeQuery(query);
//            while (rs.next()) {
//                String firstName = rs.getString("first_name");
//                System.out.println(firstName);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

        Thread consistency = new Thread(new ConsistencyChecker(new TDGHSQL(), new TDGSQLite(root)));
        consistency.setPriority(Thread.MIN_PRIORITY);
        consistency.start();

        try {
            migration.join();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
