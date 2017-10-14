package pt.fcup;

import java.io.IOException;
import java.sql.SQLException;

public class Ice {
    public static void main(String[] args) {
        Ice driver = new Ice();
        driver.run();

    }

    private void run() {
        queryDB();
    }

    private void queryDB() {
        System.out.println("Sending query to DB...");

        try {
            DBManager db = new DBManager();
            String sampleQuery = "SELECT pg.tablename FROM pg_catalog.pg_tables pg WHERE pg.tablename = 'seeders';";
            System.out.println(db.queryTable(sampleQuery).toString());

        } catch (ClassNotFoundException | IOException | SQLException ec) {
            ec.printStackTrace();

        }
    }
}
