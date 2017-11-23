package pt.fcup;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import pt.fcup.exception.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Seedbox {

    private final String METADATA_LOCATION = "file-metadata.json";
    private JSONObject fileMetadata;

    private final int BASE_PORT = 29200;
    public HashMap<String, Seeder> seederHashMap = new HashMap<>();

    private static Seedbox sb;

    public static void main(String[] args) {
        sb = new Seedbox();

        try {
            sb.run();

        } catch (JSONParsingException e) {
            System.err.println("Fatal exception, no file metadata available. Exiting.");
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

    private void run() throws JSONParsingException {
        // Parsing metadata for each video from a local JSON file
        parseMetadata();

        // Set up ICE adapter to accept incoming messages from client manager
        IceServer rpc = new IceServer();
        Thread iceThread = new Thread(rpc, "RPC Thread");
        iceThread.start();

        // Set up UploadServer to accept incoming file chunk requests from clients
        UploadServer us = new UploadServer();
        Thread usThread = new Thread(us, "Upload Thread");
        usThread.start();

    }

    /**
     * Instantiates a seeder to provide file requested
     * TODO: Handle unsuccessful registration
     * TODO: Pull out into separate class
     * @param filename name of the file requested
     */
    public Seeder createSingleSeeder(String filename) throws IOException, FileHashException {

        Seeder newSeeder;

        try{
            newSeeder = new Seeder(filename, BASE_PORT, fileMetadata.getJSONObject(filename));
        }
        catch(JSONException e)
        {
            System.err.println("Couldn't retrieve " + filename + " from local json storage");
            System.out.println(fileMetadata);
            throw e;
        }

        // Hash file, chunk file, hash chunks, and register in DB
        boolean videoProcSuccess = newSeeder.processVideo();
        boolean regSuccess = newSeeder.registerSeeder();

        // TODO: What if one fails
        if (videoProcSuccess && regSuccess) {
            System.out.println("Seeder registration success for: " + newSeeder.getVideoName());

        } else {
            System.out.println("Seeder registration UNSUCCESSFUL for: " + newSeeder.getVideoName());

        }

        seederHashMap.put(filename, newSeeder);

        return newSeeder;
    }

    /**
     * Reads metadata for all videos from a local JSON
     *
     * @return JSONObject keyed by filename
     */
    private void parseMetadata() throws JSONParsingException {
        try {
            String metadata = new String(Files.readAllBytes(Paths.get(METADATA_LOCATION)));
            fileMetadata = new JSONObject(metadata);

        } catch (IOException | JSONException e) {
            throw new JSONParsingException("Error reading file metadata.", e);

        }

    }
}
