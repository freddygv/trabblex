package pt.fcup;

import java.io.IOException;
import java.sql.SQLException;
import java.lang.ClassNotFoundException;

public class RegistrableI implements pt.fcup.generated.RegistrableI {
    private DBManager db;

    public boolean registerSeeder(String fileHash, String fileName, int fileSize, String protocol, int port,
                                  int videoSizeX, int videoSizeY, int bitrate, com.zeroc.Ice.Current current) {
        try {
            String baseUpdateQuery = "INSERT INTO seeders(file_hash, file_name, file_size, protocol, " +
                    "port, video_size_x, video_size_y, bitrate) ";

            String updateQuery;

            updateQuery = baseUpdateQuery + String.format("VALUES('%s', '%s', '%d', '%s', '%d', '%d', '%d', '%d');",
                    fileHash, fileName, fileSize, protocol, port, videoSizeX, videoSizeY, bitrate);

            System.out.println(updateQuery);
            dbUpdate(updateQuery);

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

    public boolean sendHashes(String[] chunkHashes, String fileHash, String seederIP, int seederPort, com.zeroc.Ice.Current current) {
        String baseUpdateQuery = "INSERT INTO chunk_owners(file_hash, chunk_hash, owner_ip, owner_port, is_seeder) ";
        String updateQuery;
        try {
            for (int i = 0; i < chunkHashes.length; i++) {
                updateQuery = baseUpdateQuery + String.format("VALUES('%s', '%s', '%s', '%d', 't');",
                        fileHash, chunkHashes[i], seederIP, seederPort);

                dbUpdate(updateQuery);

            }

            return true;

        } catch (ClassNotFoundException | IOException | SQLException ec) {
            System.err.println("Neighborhood update: DB insert failed.");
            return false;

        }
    }

    public void dbUpdate(String query) throws ClassNotFoundException, IOException, SQLException {
        if(db == null) {
            db = new DBManager();
        }

        db.singleUpdate(query);

    }

}