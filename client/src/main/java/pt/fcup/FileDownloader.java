package pt.fcup;

import java.nio.file.Path;

import org.json.JSONArray;

import java.util.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class FileDownloader implements Runnable {
    private final String name;
    private final String fileHash;
    private final JerseyClient client;

    private final int localPort = 26000;
    private String localIP;

    private ChunkManager chm;
    private int numChunks;

    private final String HASHING_ALGORITHM = "SHA-256";

    public FileDownloader(String name, String hash, JerseyClient client) {
        this.name = name;
        this.fileHash = hash;
        this.client = client;

        try{
            localIP = InetAddress.getLocalHost().getHostAddress();

        } catch(UnknownHostException e) {
            System.err.println("Couldn't get local IP address!");
            // TODO: Handle failure

        }
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

    private void downloadFile() throws IOException {
        // Fetch swarm for the file
        JSONArray remoteChunkOwners = new JSONArray(getChunkOwners());
        chm = new ChunkManager(remoteChunkOwners);

        while(chm.numberOfChunksNotDownloaded() > 0) {
            System.out.println("Still has to download " + chm.numberOfChunksNotDownloaded() + " chunks");

            // determine next chunk to download and get a source for it
            Chunk nextChunkToDownload = getNextChunk();
            Owner chunkSource = nextChunkToDownload.getSource();

            // construct downloader and start thread
            ChunkDownloader dwl = new ChunkDownloader(name,
                                                      nextChunkToDownload.getChunkNumber(),
                                                      chunkSource.ip,
                                                      chunkSource.port);

            Thread dwlThread = new Thread(dwl);
            dwlThread.start();

            try {
                dwlThread.join();
                numChunks = dwl.getNbChunks();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

            }

            checkDownload(nextChunkToDownload, chunkSource);
            startUploading();

        }

        assembleFile(name);

    }

    /**
     * If there are sources available, get the rarest chunk
     * Else, request a seeder server
     */
    private Chunk getNextChunk() {
        Chunk rarest = chm.getRarestChunk();

        // Wait for the seeder to come online
        if (rarest == null) {
            // If rarest is null, request a new seeder
            String newSeeder = client.query("createseeder", name);
            if (newSeeder == null) {
                System.err.println("File no longer available. Please refresh the list of videos.");
                Thread.currentThread().interrupt();
            }

            rarest = chm.getRarestChunk();
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
     * Validate the download, if not valid clean up the bad file
     */
    private void checkDownload(Chunk nextChunkToDownload, Owner chunkSource) {
        int chunkID = nextChunkToDownload.getChunkNumber();

        try {
            checkHash("downloads/" + name + "-" + chunkID, chunkSource.hash);
            chm.markChunkDownloaded(chunkID);

            // move file to sources
            File f = new File("downloads/" + name + "-" + chunkID);
            File f2 = new File("sources/" + name + "-" + chunkID);
            f.renameTo(f2);

            // register with portal so other can download from this user
            registerAsSeeder(chunkSource.hash, chunkID);

        } catch (IOException e) {
            try {
                cleanUpBadChunk(nextChunkToDownload, chunkSource, chunkID);

            } catch (IOException ex) {
                System.err.println("Unable to cleanup bad chunk.");
                ex.printStackTrace();

            }
        }
    }

    /**
     * If the chunk validation fails, delete bad chunk and remove owner
     * TODO: Blacklist owner instead of just removing
     * that way you can requery and still keep the owner out
     */
    private void cleanUpBadChunk(Chunk nextChunkToDownload, Owner chunkSource, int chunkID) throws IOException {
        nextChunkToDownload.removeOwner(chunkSource.ip, chunkSource.port);

        System.out.println("Bad chunk hash downloads/" + name + "-" + chunkID
                         + "Will now try another source");

        // delete local chunk, client will try again
        Path localFile = Paths.get("downloads/" + name + "-" + chunkID);
        Files.delete(localFile);

    }

    /**
     * If the download is valid, register this client as a chunk owner in the DB
     */
    private void registerAsSeeder(String chunkHash, int id) {
        Map<String,String> queryParams = new HashMap<>();
        queryParams.put("file_hash", fileHash);
        queryParams.put("chunk_hash", chunkHash);
        queryParams.put("chunk_id", Integer.toString(id));
        queryParams.put("ip", localIP);
        queryParams.put("port", Integer.toString(localPort));

        client.query("registerclientseeder", null, queryParams);
    }

    /**
     * TODO: Update
     */
    private void startUploading() {
        if (SimpleClient.uploader == null) {
            SimpleClient.uploader = new UploadServer(localPort, numChunks);

            Thread upServer = new Thread(SimpleClient.uploader);
            upServer.start();

        } else {
            // Already have an upload server running, do nothing

        }
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

            checkHash("downloads/" + name, fileHash);

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

    /**
    *   Makes sure a file or chunk hash is correct
    */
    public boolean checkHash(String file, String hash) throws IOException {
        String realHash = hashFile(file);
        return realHash.equals(hash);

    }

    /**
     * Read file to byte array with buffer, hash, then convert to hex string
     * http://www.codejava.net/coding/how-to-calculate-md5-and-sha-hash-values-in-java
     * @param file
     * @return hex string hash
     * TODO: Make local jar, duplicating work
     */
    private String hashFile(String file) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance(HASHING_ALGORITHM);

            byte[] bytesBuffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(bytesBuffer)) > 0) {
                digest.update(bytesBuffer, 0, bytesRead);
            }

            byte[] hashedBytes = digest.digest();

            return bytesToHex(hashedBytes);

        } catch (NoSuchAlgorithmException e) {
            // Unreachable unless SHA-256 is removed
            return null;

        } catch (IOException e) {
            throw e;

        }
    }

    /**
     * Converts byte array to hex string
     * https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
     * TODO: Make local jar, duplicating work
    */
    private final char[] hexArray = "0123456789ABCDEF".toCharArray();
    public String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


}