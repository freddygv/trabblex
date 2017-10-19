package pt.fcup;

import java.io.IOException;
import java.sql.SQLException;
import java.lang.ClassNotFoundException;


public class RegistrableI implements pt.fcup.generated.RegistrableI {
    public void registerSeeder(String regMessage, com.zeroc.Ice.Current current) {
        try {
            dbUpdate(regMessage);

        } catch (ClassNotFoundException | IOException | SQLException ec) {
            System.err.println("Seeder registration: DB insert failed.");

        }

    }

    public void deregisterSeeder(String deregMessage, com.zeroc.Ice.Current current){
        try {
            dbUpdate(deregMessage);

        } catch (ClassNotFoundException | IOException | SQLException ec) {
            System.err.println("Seeder de-registration: DB delete failed.");

        }
    }

    private void dbUpdate(String query) throws ClassNotFoundException, IOException, SQLException {
        DBManager db = new DBManager();
        db.singleUpdate(query);

    }

}