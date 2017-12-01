package pt.fcup;

import org.json.JSONArray;

import java.util.*;
import java.io.*;
import java.nio.file.Files;

/**
 * Handles download for a specific video
 */
public class FileDownloader implements Runnable {
    private final String name;
    private final String fileHash;
    private final JerseyClient client;

    private ChunkManager chm;

    public FileDownloader(String name, String hash) {
        this.name = name;
        this.fileHash = hash;

        client = new JerseyClient();
    }

    /**
    * Starts the download of a file
    * Via a TCP connection
    **/
    @Override
    public void run() {
        if (fileHash == null) { return; }

        try {
            downloadFile();

        } catch(IOException e) {
            e.printStackTrace();

        }
    }

    /**
     * TODO: Break up
     */
    private void downloadFile() throws IOException {
        // Fetch swarm for the file
        JSONArray remoteChunkOwners = new JSONArray(getChunkOwners());
        chm = new ChunkManager(remoteChunkOwners);

        Set<Integer> activeDownloads = new HashSet<>();

        // Continue while there are chunks remaining to download
        while(chm.numberOfChunksNotDownloaded() > 0) {
            // Check if all chunk are actively being downloaded, if so, wait
            if (activeDownloads.size() == SimpleClient.chunkCounts.get(name)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // determine next chunk to download and get a source for it
            Chunk nextChunkToDownload = getNextChunk(activeDownloads);
            if (nextChunkToDownload == null) { return; } // Bail, no seeder is available

            Owner chunkSource = nextChunkToDownload.getSource();

            // construct downloader and start thread
            ChunkDownloader dwl = new ChunkDownloader(name,
                                                      chm,
                                                      nextChunkToDownload,
                                                      chunkSource,
                                                      fileHash,
                                                      nextChunkToDownload.getChunkNumber(),
                                                      chunkSource.ip,
                                                      chunkSource.port);

            activeDownloads.add(nextChunkToDownload.getChunkNumber());

            Thread dwlThread = new Thread(dwl);
            dwlThread.start();

        }

        assembleFile(name);
    }

    /**
     * If there are sources available, get the rarest chunk
     * Else, request a seeder server
     */
    private Chunk getNextChunk(Set<Integer> activeDownloads) {
        Chunk rarest = chm.getRarestChunk(activeDownloads);

        // Wait for the seeder to come online
        if (rarest == null) {
            // If rarest is null, request a new seeder
            String newSeeder = client.query("createseeder", name);
            if (newSeeder == null) {
                System.err.println("File no longer available. Please refresh the list of videos.");
                return null;
            }

            rarest = chm.getRarestChunk(activeDownloads);
        }

        return rarest;

    }

    /**
     * Query the ClientManager to get the swarm for this file
     */
    private String getChunkOwners() {
        String chunkOwners = client.query("getowners", fileHash);

        if (chunkOwners == null) {
            System.err.println("Couldn't get the chunk owners of the file!");
            Thread.currentThread().interrupt();

        }

        return chunkOwners;
    }

    /**
     * Combine all chunks for the file in the sources directory and validate merged file
     */
    private void assembleFile(String name) {
        try (FileOutputStream fos = new FileOutputStream("downloads/" + name);
            BufferedOutputStream mergingStream = new BufferedOutputStream(fos)) {

            File[] directoryFiles = new File("sources/").listFiles();
            Arrays.sort(directoryFiles);

            for (File videoChunk : directoryFiles) {
                if (videoChunk.getName().startsWith(name)) {
                    Files.copy(videoChunk.toPath(), mergingStream);

                }
            }

            FileHasher hasher = new FileHasher();
            hasher.checkHash("downloads/" + name, fileHash);

        } catch(IOException e) {
            System.err.println("Error re-assembling the file, please try again.");


        } finally {
            cleanUpAllChunks();

        }
    }

    /**
     * Removes all chunks downloaded for when a re-download is needed
     */
    private void cleanUpAllChunks() {
        File directory = new File("downloads/");

        for (File videoChunk : directory.listFiles()) {
            if (videoChunk.getName().startsWith(name)) {
                videoChunk.delete();

            }
        }

    }
}