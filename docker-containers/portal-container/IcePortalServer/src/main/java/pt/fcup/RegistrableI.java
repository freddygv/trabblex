package pt.fcup;

import com.zeroc.Ice.Current;

import java.io.IOException;
import java.sql.SQLException;
import java.lang.ClassNotFoundException;

public class RegistrableI implements pt.fcup.generated.RegistrableI {
    private DBManager db;

    /**
     * Writes to videos table, adding host and file information
     * @return true if database successfully updated with new file
     */
    public boolean registerSeeder(String fileHash, com.zeroc.Ice.Current current) {
        try {
            System.out.println(String.format("Seeding Seeder for: %s as active", fileHash));
            String updateQuery = "UPDATE videos SET seeder_is_active = 't' WHERE file_hash = '%s';";

            dbUpdate(String.format(updateQuery, fileHash));
            return true;

        } catch (ClassNotFoundException | IOException | SQLException ec) {
            System.err.println("Seeder registration: DB update failed.");
            ec.printStackTrace();
            return false;

        }

    }

    /**
     * Deletes from videos table, removing de-registered seeder
     * @return true if records successfully deleted
     */
    public boolean deregisterSeeder(String fileHash, com.zeroc.Ice.Current current){
        String seederDeletionQuery = "UPDATE videos SET seeder_is_active = 'f' WHERE file_hash = '%s';";
        String neighborDeletionQuery = "DELETE FROM chunk_owners WHERE file_hash = '%s';";

        try {
            dbUpdate(String.format(seederDeletionQuery, fileHash));
            dbUpdate(String.format(neighborDeletionQuery, fileHash));
            return true;

        } catch (ClassNotFoundException | IOException | SQLException ec) {
            System.err.println("Seeder de-registration: DB delete failed.");
            ec.printStackTrace();
            return false;

        }
    }

    /**
     * Writes to chunk_owners table, adding a row per chunk_hash
     * @return true if database successfully updated with all chunks for a new file
     */
    public boolean sendHashes(String[] chunkHashes, String[] chunkIDs, String fileHash, String seederIP, int seederPort,
                              com.zeroc.Ice.Current current) {

        String baseUpdateQuery = "INSERT INTO chunk_owners(file_hash, chunk_hash, chunk_id, owner_ip, owner_port, is_seeder) ";
        String updateQuery;
        try {
            for (int i = 0; i < chunkHashes.length; i++) {
                updateQuery = baseUpdateQuery + String.format("VALUES('%s', '%s', '%s', '%s', '%d', 't');",
                        fileHash, chunkHashes[i], chunkIDs[i], seederIP, seederPort);

                System.out.println(String.format("Seeding chunkHash #%s for: %s as active", chunkHashes[i], chunkIDs[i]));
                dbUpdate(updateQuery);

            }

            return true;

        } catch (ClassNotFoundException | IOException | SQLException ec) {
            System.err.println("Neighborhood update: DB insert failed.");
            return false;

        }
    }

    /**
     * Creates a new DBManager if there isn't one, then executes single table update
     * @return true if database successfully updated with new file
     */
    public void dbUpdate(String query) throws ClassNotFoundException, IOException, SQLException {
        if(db == null) {
            db = new DBManager(true);
        }

        db.singleUpdate(query);

    }

}