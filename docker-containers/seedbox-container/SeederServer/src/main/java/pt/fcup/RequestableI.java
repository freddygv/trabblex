package pt.fcup;

import org.json.JSONObject;
import pt.fcup.exception.FileHashException;
import java.io.IOException;
import java.sql.SQLException;

public class RequestableI implements pt.fcup.generated.RequestableI {
    private final int BASE_PORT = 29200;

    /**
     * Implementation of RPC requests for Seeders from the ClientManager.
     * If a client requests a seeder, provide number of chunks, else create and register one.
     *
     * @param fileName of video
     * @param current Ice object
     * @return number of chunks that the video was split into
     */
    public boolean requestSeeder(String fileName, com.zeroc.Ice.Current current) {
        boolean success = false;

        try {

            // Try to get an existing instance of a Seeder for a given file
            Seedbox sb = Seedbox.getSeedbox();
            Seeder newSeed = sb.seederHashMap.get(fileName);

            // If there is no instance, create one
            // Files aren't chunked and hashed up front. Only once requested for the first time.
            if(newSeed == null) {
                System.out.println("File hasn't been processed, doing so now.");
                newSeed = createSingleSeeder(sb.fileMetadata.getJSONObject(fileName), fileName);
                sb.seederHashMap.put(fileName, newSeed);

            }

            success = true;

        } catch (IOException | FileHashException e) {
            System.err.println("Error generating seeder.");
            // TODO: Remove from videos


        } finally {
            // If false is returned, ClientManager handles failure
            return success;

        }

    }

    /**
     * Processes the video requested and registers the chunks in the portal.
     * @param filename name of the file requested
     */
    private Seeder createSingleSeeder(JSONObject videoMetadata, String filename) throws IOException, FileHashException, ClassNotFoundException, SQLException {

        Seeder newSeeder;

        newSeeder = new Seeder(filename, BASE_PORT, videoMetadata);

        // Hash file, chunk file, hash chunks, and register in DB, if either fails an exception is thrown
        newSeeder.processVideo();
        newSeeder.registerSeeder();

        return newSeeder;
    }
}
