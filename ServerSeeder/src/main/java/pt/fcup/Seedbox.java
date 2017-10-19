package pt.fcup;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.security.SecureRandom;

public class Seedbox {

    private final int BASE_PORT = 29200;
    private final int MAX_OFFSET = 100;
    private Set<Integer> portsTaken = new HashSet<>();

    public static void main(String[] args) {
        Seedbox sb = new Seedbox();
        sb.run();
    }

    private void run() {
        List<String> videos = new ArrayList<>(Arrays.asList("video1-url.mp4", "video2-url.mp4"));
        createSingleSeeder(videos.get(0));
        createSingleSeeder(videos.get(1));

        // TODO: Remove at the end, just used to flush the system
        queryAndTruncateSeeders();
    }

    /**
     * Instantiates a seeder to provide file requested
     * @param filename name of the file requested
     */
    private boolean createSingleSeeder(String filename) {
        // TODO: Remove block when there's real data
        Random rand = new Random();
        int i = rand.nextInt(100);
        HashMap<String, String> fileMetadata =  getDummyMetadata(i, filename);
        //

        Seeder newSeeder = new Seeder(fileMetadata);

        int seederPort = generatePort();
        newSeeder.setHost(seederPort);

        boolean regSuccess = newSeeder.registerSeeder();

        if (regSuccess) {
            System.out.println("Seeder registration success for: " + newSeeder.getFileName());

        } else {
            System.out.println("Seeder registration failed for: " + newSeeder.getFileName());


        }

        return regSuccess;
    }

    private int generatePort() {
        Random rand = new SecureRandom();

        int randomOffset;
        while (true) {
            randomOffset = rand.nextInt(MAX_OFFSET);

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
    private void queryAndTruncateSeeders() {
        try {
            DBManager testDB = new DBManager();
            System.out.println("Querying seeders table:");
            System.out.println(testDB.queryTable("SELECT file_hash, file_name, port FROM seeders;").toString());

            System.out.println("Truncating seeders table:");
            testDB.singleUpdate("DELETE FROM seeders WHERE file_hash IS NOT NULL;");
        } catch (SQLException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO: Remove, need to implement a separate method that creates metadata
     * @param index index to distinguish hash and filename
     * @param filename Name of the file
     */
    private HashMap<String, String> getDummyMetadata(int index, String filename) {
        HashMap<String, String> metadata = new HashMap<>();
        metadata.put("fileHash", "abcd" + Integer.toString(index));
        metadata.put("fileName", "video-" + Integer.toString(index));
        metadata.put("fileSize", "74");
        metadata.put("video_size_x", "20");
        metadata.put("video_size_y", "24");
        metadata.put("bitrate", "256");

        return metadata;
    }

}
