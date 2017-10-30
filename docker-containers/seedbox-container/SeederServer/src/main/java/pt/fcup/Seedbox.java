package pt.fcup;

import org.json.JSONException;
import org.json.JSONObject;
import pt.fcup.exception.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Seedbox {

    private final String METADATA_LOCATION = "file-metadata.json";
    private JSONObject fileMetadata;

    private final int BASE_PORT = 29200;
    public HashMap<String, Seeder> seederHashMap = new HashMap<>();

    private final int CHUNK_SIZE = 10 * 1024 * 1024; // 10 MB

    private static Seedbox sb;

    public static void main(String[] args) {

        sb = new Seedbox();

        try {
            sb.run();

        } catch (JSONParsingException | IOException e) {
            System.err.println("Fatal exception, exiting.");
            e.printStackTrace();
            System.exit(1);

        }

    }

    /**
     * Provides RequestableI interface with access to the Seedbox object
     */
    public static Seedbox getSeedbox() {
        return sb;

    }

    private void run() throws JSONParsingException, IOException {
        // Parsing metadata for each video from a local JSON file
        parseMetadata();

        // Set up ICE adapter to accept incoming messages
        IceServer rpc = new IceServer();
        Thread iceThread = new Thread(rpc, "RPC Thread");
        iceThread.start();

        try {
            createSingleSeeder("tl_512kb.mp4");
        } catch (FileHashException e) {
            e.printStackTrace();
        } catch (PortGenerationException e) {
            e.printStackTrace();
        }

        UploadServer us = new UploadServer();
        Thread usThread = new Thread(us, "Upload Thread");
        usThread.start();

    }

    /**
     * Instantiates a seeder to provide file requested
     *
     * @param filename name of the file requested
     */
    public Seeder createSingleSeeder(String filename) throws IOException, FileHashException, PortGenerationException {

        Seeder newSeeder = new Seeder(filename, BASE_PORT, fileMetadata.getJSONObject(filename), CHUNK_SIZE);

        // Hash file, chunk file, and hash chunks
        boolean videoProcSuccess = newSeeder.processVideo();

        // Register with the portal
        boolean regSuccess = newSeeder.registerSeeder();

        if (regSuccess && videoProcSuccess) {
            System.out.println("Seeder registration success for: " + newSeeder.getVideoName());

        }

        System.out.println();

        // Storing seeders in a HashMap to allow access by filename
        seederHashMap.put(filename, newSeeder);

        return newSeeder;
    }

    /**
     * Reads metadata for all videos from a local JSON
     *
     * @return JSONObject keyed by filename
     */
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
