package pt.fcup;

import org.json.JSONException;
import org.json.JSONObject;
import pt.fcup.exception.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.security.SecureRandom;

public class Seedbox {

    private final String METADATA_LOCATION = "file-metadata.json";
    private JSONObject fileMetadata;

    private final int BASE_PORT = 29200;
    private final int MAX_OFFSET = 100;
    private Set<Integer> portsTaken = new HashSet<>();
    private final int CHUNK_SIZE = 10 * 1024 * 1024;

    private static Seedbox sb;
    public HashMap<String, Seeder> seederHashMap = new HashMap<>();

    public static void main(String[] args) {

        sb = new Seedbox();

        try {
            sb.run();

        } catch (JSONParsingException e) {
            System.err.println("Fatal exception, exiting.");
            e.printStackTrace();
            System.exit(1);

        }

    }

    public static Seedbox getSeedbox() {
        return sb;

    }

    private void run() throws JSONParsingException {
        // TODO: Remove at the end, just used to flush the system
        truncateTables();

        parseMetadata();
        startIceServer();

    }

    private void startIceServer() {
        int status = 0;
        com.zeroc.Ice.Communicator ic = null;

        try {
            ic = com.zeroc.Ice.Util.initialize();
            com.zeroc.Ice.ObjectAdapter adapter =
                    ic.createObjectAdapterWithEndpoints("SeederRequestAdapter", "default -p 8082");
            adapter.add(new RequestableI(), com.zeroc.Ice.Util.stringToIdentity("SeederRequest"));
            adapter.activate();
            ic.waitForShutdown();

        } catch (com.zeroc.Ice.LocalException e) {
            e.printStackTrace();
            status = 1;

        } catch (Exception e) {
            System.err.println(e.getMessage());
            status = 1;

        }
        if (ic != null) {
            try {
                ic.destroy();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                status = 1;
            }
        }
        System.exit(status);
    }

    /**
     * Instantiates a seeder to provide file requested
     * @param filename name of the file requested
     */
    public Seeder createSingleSeeder(String filename) throws IOException, FileHashException {

        Seeder newSeeder = new Seeder(filename, fileMetadata.getJSONObject(filename), CHUNK_SIZE);

        int seederPort = generatePort();
        newSeeder.setHost(seederPort);

        boolean videoProcSuccess = newSeeder.processVideo();
        boolean regSuccess = newSeeder.registerSeeder();

        if (regSuccess && videoProcSuccess) {
            System.out.println("Seeder registration success for: " + newSeeder.getFileName());

        }

        seederHashMap.put(filename, newSeeder);

        // manually start the seeder's tcp seed
        // later, will be done via RPC call from clientManagerResource
//        newSeeder.transferTCP();
//        System.in.read();

        return newSeeder;
    }

    private int generatePort() {
        Random rand = new SecureRandom();

        int randomOffset;
        while (true) {
            randomOffset = rand.nextInt(MAX_OFFSET) * 20;

            // If portsTaken already has the number, false is returned
            if (portsTaken.add(randomOffset)) {
                return BASE_PORT + randomOffset;

            }
        }
    }

    /**
     * For debugging only, queries the DB to check if all files were added.
     * Call before 'return seeders;'
     */
    private void truncateTables() {
        try {
            DBManager testDB = new DBManager();
            System.out.println("Truncating seeders table:");
            testDB.singleUpdate("DELETE FROM seeders WHERE file_hash IS NOT NULL;");

            System.out.println("Truncating chunk_owners table:");
            testDB.singleUpdate("DELETE FROM chunk_owners WHERE file_hash IS NOT NULL;");

        } catch (SQLException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * For debugging only, queries the DB to check if all files were added.
     * Call before 'return seeders;'
     */
    private void queryTables() {
        try {
            DBManager testDB = new DBManager();
            System.out.println("Querying seeders table:");
            System.out.println(testDB.queryTable("SELECT file_hash, file_name, port FROM seeders;").toString());

            System.out.println("Querying chunk_owners table:");
            System.out.println(testDB.queryTable("SELECT file_hash, chunk_hash, owner_port FROM chunk_owners;").toString());

        } catch (SQLException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    private JSONObject parseMetadata() throws JSONParsingException {
        try {
            String metadata = new String(Files.readAllBytes(Paths.get(METADATA_LOCATION)));
            fileMetadata = new JSONObject(metadata);

            return fileMetadata;

        } catch (IOException | JSONException e) {
            throw new JSONParsingException("Error reading file metadata.", e);

        }

    }

}
