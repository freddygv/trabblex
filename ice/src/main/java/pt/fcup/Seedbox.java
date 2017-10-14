package pt.fcup;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Seedbox {
    // TODO: Put in real URL
    private final String BUCKET_URL = "fake.google.com/cloud/storage/url/bucket";

    public static void main(String[] args) {
        Seedbox sb = new Seedbox();
        sb.run();
    }

    private void run() {
        List<String> videoURLS = getVideoListFromBucketURL();
        HashMap<String, Seeder> seederMap = createSeeders(videoURLS);

    }

    /**
     * TODO: Upload videos to google cloud storage and implement function to get the list of files
     * @return list of video URLs in Google Storage Bucket
     */
    private List<String> getVideoListFromBucketURL() {
        List<String> videos = new ArrayList<>(Arrays.asList("video1-url.mp4", "video2-url.mp4"));

        return videos;
    }

    /**
     * Creating and returning a dictionary of file-hashes to Seeders
     *
     * @param urls String list of URLs with video files
     */
    private HashMap<String, Seeder> createSeeders(List<String> urls) {
        HashMap<String, Seeder> seeders = new HashMap<>();
        HashMap<String, String> fileMetadata;

        Seeder current;

        for (int i = 0; i < urls.size(); i++) {
            fileMetadata = getMetadata(i, urls.get(i));

            current = new Seeder(fileMetadata);

            // TODO: Do I need to give each seeder a unique port to listen on?
            current.setHost();

            boolean regSuccess = current.registerSeeder();

            if (regSuccess) {
                seeders.put(current.getFileHash(), current);
            } else {
                System.out.println("Seeder registration failed for: " + current.getFileHash());

            }

        }

        return seeders;
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
     * TODO: Remove dummy data and index param once metadata fetcher is implemented
     * Getting relevant file metadata to instantiate Seeders
     *
     * @param videoUrl URL for the file
     */
    private HashMap<String, String> getMetadata(int index, String videoUrl) {
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
