package pt.fcup;

import java.io.IOException;
import java.sql.SQLException;
import java.lang.ClassNotFoundException;

public class RegistrableI implements pt.fcup.generated.RegistrableI {
    public boolean registerSeeder(String regMessage, com.zeroc.Ice.Current current) {
        try {
            dbUpdate(regMessage);
            return true;

        } catch (ClassNotFoundException | IOException | SQLException ec) {
            System.err.println("Seeder registration: DB insert failed.");
            ec.printStackTrace();
            return false;

        }

    }

    public boolean deregisterSeeder(String fileHash, com.zeroc.Ice.Current current){
        String seederDeletionQuery = "DELETE FROM seeders WHERE file_hash = '" + fileHash + "';";
        String neighborDeletionQuery = "DELETE FROM chunk_owners WHERE file_hash = '" + fileHash + "';";

        try {
            dbUpdate(seederDeletionQuery);
            dbUpdate(neighborDeletionQuery);
            return true;

        } catch (ClassNotFoundException | IOException | SQLException ec) {
            System.err.println("Seeder de-registration: DB delete failed.");
            ec.printStackTrace();
            return false;

        }
    }

    public boolean sendHashes(String[] chunkHashes, String fileHash, String seederIP, String seederPort, com.zeroc.Ice.Current current) {
        String baseUpdateQuery = "INSERT INTO chunk_owners(file_hash, chunk_hash, owner_ip, owner_port, is_seeder, is_active) ";
        String updateQuery;
        try {
            for (int i = 0; i < chunkHashes.length; i++) {
                updateQuery = baseUpdateQuery + String.format("VALUES('%s', '%s', '%s', '%s', 't', 't');",
                        fileHash, chunkHashes[i], seederIP, seederPort);

                dbUpdate(updateQuery);

            }

            return true;

        } catch (ClassNotFoundException | IOException | SQLException ec) {
            System.err.println("Neighborhood update: DB insert failed.");
            return false;

        }
    }

    private void dbUpdate(String query) throws ClassNotFoundException, IOException, SQLException {
        DBManager db = new DBManager();
        db.singleUpdate(query);

    }

}