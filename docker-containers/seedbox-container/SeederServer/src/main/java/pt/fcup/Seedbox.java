package pt.fcup;

import org.json.JSONException;
import org.json.JSONObject;
import pt.fcup.exception.*;
import pt.fcup.generated.RegistrableIPrx;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

public class Seedbox {

    private final String METADATA_LOCATION = "file-metadata.json";
    private JSONObject fileMetadata;

    private String iceHost;

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
        String portalAddress = InetAddress.getByName("portal").getHostAddress();
        iceHost = String.format("%s -p 8081", portalAddress);

        // Parsing metadata for each video from a local JSON file
        parseMetadata();
        writeVideosToDB();

        // Set up ICE adapter to accept incoming messages
        IceServer rpc = new IceServer();
        Thread iceThread = new Thread(rpc, "RPC Thread");
        iceThread.start();

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

        Seeder newSeeder = null;

        try{
            newSeeder = new Seeder(filename, BASE_PORT, iceHost, fileMetadata.getJSONObject(filename), CHUNK_SIZE);
        }
        catch(JSONException e)
        {
            System.err.println("Couldn't retrieve " + filename + " from local json storage");
            System.out.println(fileMetadata);
            throw e;
        }

        // Hash file, chunk file, and hash chunks
        boolean videoProcSuccess = newSeeder.processVideo();

        // Register with the portal
        boolean regSuccess = newSeeder.registerSeeder();

        if (regSuccess && videoProcSuccess) {
            System.out.println("Seeder registration success for: " + newSeeder.getVideoName());

        } else {
            System.out.println("Seeder registration UNSUCCESSFUL for: " + newSeeder.getVideoName());
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

    private void writeVideosToDB() {
        String fileHash;
        String filepath;
        int fileSize;
        int videoSizeX;
        int videoSizeY;
        int bitrate;

        JSONObject currentItem;
        Iterator<?> keys = fileMetadata.keys();
        String currentKey;

        System.out.println("Initializing videos table...");

        boolean regResult = false;
        while(keys.hasNext()) {

            currentKey = (String)keys.next();
            currentItem = fileMetadata.getJSONObject(currentKey);

            fileHash = currentItem.getString("fileHash");
            filepath = currentItem.getString("filepath");
            fileSize = currentItem.getInt("fileSize");
            videoSizeX = currentItem.getInt("videoSizeX");
            videoSizeY = currentItem.getInt("videoSizeY");
            bitrate = currentItem.getInt("bitrate");

            System.out.println(String.format("Current video: '%s' '%s'", currentKey, fileHash));

            try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize()) {
                RegistrableIPrx deregister = RegistrableIPrx.checkedCast(communicator.stringToProxy("SeederRegistration:default -h " + iceHost));

                regResult = deregister.initializeDB(fileHash, filepath, fileSize, videoSizeX, videoSizeY, bitrate);
            }

            if (regResult == false) {
                System.err.println("Video db initialization failed for: " + currentKey);
                fileMetadata.remove(currentKey);

            }

        }

        // TODO: Remove
        queryTables();

    }

    /**
     * For debugging only, queries entire seeders and chunk_owners tables to check if all files were added.
     */
    private void queryTables() {
        try {
            DBManager testDB = new DBManager(true);
            System.out.println("Querying videos table:");
            System.out.println(testDB.queryTable("SELECT file_hash, file_name FROM videos;").toString());

            System.out.println("Querying chunk_owners table:");
            System.out.println(testDB.queryTable("SELECT file_hash, chunk_hash, owner_port FROM chunk_owners;").toString());

        } catch (SQLException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }



}
