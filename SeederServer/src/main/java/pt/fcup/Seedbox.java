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

    public static void main(String[] args) {

        Seedbox sb = new Seedbox();

        try {
            sb.run();

        } catch (JSONParsingException | SeederGenerationException e) {
            System.err.println("Fatal exception, exiting.");
            e.printStackTrace();
            System.exit(1);

        }

    }

    private void run() throws JSONParsingException, SeederGenerationException {
        // TODO: Remove at the end, just used to flush the system
        truncateTables();

        List<String> videos = new ArrayList<>(Arrays.asList("The Letter",
                                                            "The Vagabond",
                                                            "Popeye the Sailor"));

        try {
            fileMetadata = parseMetadata();

        } catch (IOException | JSONException e) {
            throw new JSONParsingException("Error reading file metadata.", e);

        }

        try {
            createSingleSeeder(videos.get(2));

        } catch (IOException | FileHashException e) {
            throw new SeederGenerationException("Error generating seeder.", e);
            // TODO: Seeder creation failures need to be handled somewhere

        }

        // TODO: Remove at the end, just used to flush the system
        queryTables();
    }

    /**
     * Instantiates a seeder to provide file requested
     * @param filename name of the file requested
     */
    private Seeder createSingleSeeder(String filename) throws IOException, FileHashException {

        Seeder newSeeder;
        newSeeder = new Seeder(filename, fileMetadata.getJSONObject(filename));

        int seederPort = generatePort();
        newSeeder.setHost(seederPort);

        boolean regSuccess = newSeeder.registerSeeder();

        if (regSuccess) {
            System.out.println("Seeder registration success for: " + newSeeder.getFileName());

        } else {
            System.out.println("Seeder registration failed for: " + newSeeder.getFileName());


        }

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

    private JSONObject parseMetadata() throws IOException, JSONException {
        String metadata = new String(Files.readAllBytes(Paths.get(METADATA_LOCATION)));
        return new JSONObject(metadata);

    }

}
