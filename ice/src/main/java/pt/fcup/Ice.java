package pt.fcup;

import java.io.IOException;
import java.sql.SQLException;

public class Ice {
    public static void main(String[] args) {
        Ice driver = new Ice();
        driver.run();

    }

    private void run() {
        testDB();

    }

    private void testDB() {
        System.out.println("Testing DB connection and basic query...");

        try {
            DBManager db = new DBManager();
            String sampleQuery = "SELECT pg.tablename FROM pg_catalog.pg_tables pg WHERE pg.tablename = 'seeders';";
            db.printQuery(sampleQuery);

        } catch (ClassNotFoundException | IOException | SQLException ec) {
            ec.printStackTrace();

        }
    }
}
