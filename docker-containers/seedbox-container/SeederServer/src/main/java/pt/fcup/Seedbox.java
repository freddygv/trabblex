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
    JSONObject fileMetadata;
    HashMap<String, Seeder> seederHashMap = new HashMap<>();

    static DBManager db;
    static Seedbox sb;

    public static void main(String[] args) {
        sb = new Seedbox();

        try {
            db = new DBManager();
            sb.run();


        } catch (JSONParsingException | IOException | ClassNotFoundException e) {
            System.err.println("Fatal exception. Exiting.");
            e.printStackTrace();
            System.exit(1);

        }

    }

    /**
     * Provides RequestableI interface with access to the Seedbox object
     */
    static Seedbox getSeedbox() {
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
