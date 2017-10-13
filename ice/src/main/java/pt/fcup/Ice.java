package pt.fcup;

public class Ice {
    public static void main(String[] args) {
        Ice driver = new Ice();
        driver.run();

    }


    private void run() {
        DBManager db = new DBManager();
        String sampleQuery = "SELECT pg.tablename FROM pg_catalog.pg_tables pg WHERE pg.tablename = 'seeders';";
        db.printQuery(sampleQuery);

    }
}
