package pt.fcup;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Seedbox {
    // TODO: Put in real URL
    private final String BUCKET_URL = "fake.google.com/cloud/storage/url/bucket";

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
     * TODO: Return seeder, not void
     * @param filename
     */
    private void createSingleSeeder(String filename) {
        Random rand = new Random();
        int i = rand.nextInt(10);

        HashMap<String, String> fileMetadata =  getDummyMetadata(i, filename);
        Seeder newSeeder = new Seeder(fileMetadata);

        // TODO: Do I need to give each seeder a unique port to listen on?
        newSeeder.setHost();

        boolean regSuccess = newSeeder.registerSeeder();

        if (regSuccess) {
            System.out.println("Seeder registration success for: " + newSeeder.getFileName());
        } else {
            System.out.println("Seeder registration failed for: " + newSeeder.getFileName());

        }

//        return newSeeder;
    }

    /**
     * For debugging only, queries the DB to check if all files were added.
     * Call before 'return seeders;'
     */
    private void queryAndTruncateSeeders() {
        try {
            DBManager testDB = new DBManager();
            System.out.println("Querying seeders table:");
            System.out.println(testDB.queryTable("SELECT file_hash, file_name FROM seeders;").toString());

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
