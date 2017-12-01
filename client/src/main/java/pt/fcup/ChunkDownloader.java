package pt.fcup;

import java.io.FileOutputStream;
import java.net.Socket;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Executes handshake with a chunk source and downloads the chunk
 */
class ChunkDownloader implements Runnable {
    private final String ip;
    private final ChunkManager manager;
    private final Chunk chunkRequested;
    private final Owner sources;
    private final String file;
    private final int port;
    private final int chunkNumber;
    private final String fileHash;

    public ChunkDownloader(String file, ChunkManager manager, Chunk chunkRequested, Owner sources,
                           String fileHash, int chunkNumber, String ip, int port) {
        this.ip = ip;
        this.chunkRequested = chunkRequested;
        this.sources = sources;
        this.port = port;
        this.file = file;
        this.manager = manager;
        this.chunkNumber = chunkNumber;
        this.fileHash = fileHash;

    }

    @Override
    public void run() {
        downloadChunk();
        validateChunk(chunkRequested, sources);
        SimpleClient.startUploadServer();

    }

    private void downloadChunk() {
        // Make local directory for downloads if it doesn't exist
        File downloadDirectory = new File("downloads");
        if (!downloadDirectory.exists()) { downloadDirectory.mkdir(); }

        try (Socket clientSocket = new Socket(ip, port);

             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(clientSocket.getInputStream()));

             DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
             FileOutputStream fos = new FileOutputStream(downloadDirectory + "/" + file + "-" + chunkNumber)) {

            clientSocket.setSoTimeout(10000);

            serverHandshake(in, out);
            saveChunk(dis, fos);

        } catch(IOException  e) {
            System.out.println("Couldn't connect to " + ip + ":" + port);
            e.printStackTrace();

        }
    }

    /**
     * Save chunk to file
     */
    private void saveChunk(DataInputStream dis, FileOutputStream fos) throws IOException {
        byte[] contents = new byte[1024*1024]; // 1MB
        int bytesRead;

        while ((bytesRead = dis.read(contents)) > 0) {
            fos.write(contents, 0, bytesRead);

        }
    }

    /**
     * Validate the download, if not valid clean up the bad file
     */
    private void validateChunk(Chunk nextChunkToDownload, Owner chunkSource) {
        int chunkID = nextChunkToDownload.getChunkNumber();

        try {
            FileHasher hasher = new FileHasher();
            hasher.checkHash("downloads/" + file + "-" + chunkID, chunkSource.hash);

            manager.markChunkDownloaded(chunkID);

            // move file to sources
            File f = new File("downloads/" + file + "-" + chunkID);
            File f2 = new File("sources/" + file + "-" + chunkID);
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

        System.out.println("Bad chunk hash downloads/" + file + "-" + chunkID
                + "Will now try another source");

        // delete local chunk, client will try again
        Path localFile = Paths.get("downloads/" + file + "-" + chunkID);
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
        queryParams.put("ip", SimpleClient.localIP);
        queryParams.put("port", Integer.toString(SimpleClient.LOCAL_PORT));

        JerseyClient client = new JerseyClient();
        client.query("registerclientseeder", null, queryParams);

    }

    /**
     * Basic handshake with the server
     */
    private void serverHandshake(BufferedReader in, PrintWriter out) throws IOException {
        // Chunk request to server
        out.println(chunkNumber);
        out.println(file);

        // Number of chunks from server
        int numChunks = Integer.parseInt(in.readLine());
        SimpleClient.chunkCounts.put(file, numChunks);

    }
}