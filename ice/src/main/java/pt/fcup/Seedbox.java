package pt.fcup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Seedbox {
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
        HashMap<String, Seeder> seeders = new HashMap<String, Seeder>();
        HashMap<String, String> fileMetadata;

        Seeder current;

        for (int i = 0; i < urls.size(); i++) {
            fileMetadata = getMetadata(i, urls.get(i));

            current = new Seeder(fileMetadata);
            current.registerInPortal();

            seeders.put(current.getFileHash(), current);

        }

        return seeders;
    }

    /**
     * TODO: Remove index param once metadata fetcher is implemented
     * Getting relevant file metadata to instantiate Seeders
     *
     * @param videoUrl URL for the file
     */
    private HashMap<String, String> getMetadata(int index, String videoUrl) {
        HashMap<String, String> metadata = new HashMap<String, String>();
        metadata.put("fileHash", "abcd" + Integer.toString(index));
        metadata.put("filename", "video-" + Integer.toString(index));
        metadata.put("video_size_x", "20");
        metadata.put("video_size_y", "24");

        return metadata;
    }

}
