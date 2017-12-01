package pt.fcup;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Executes handshake with user requesting chunk and transfers it via TCP socket
 * TODO: Pull out into jar and use as local dependency
 */
class ChunkSeeder implements Runnable {
    private final Socket socket;

    private int chunkID;
    private String filename;
    private String directory;

    public ChunkSeeder(Socket socket) {
        this.socket = socket;

    }

    public void run() {
        try(BufferedReader in = new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));

            OutputStream os = socket.getOutputStream();
            PrintWriter out = new PrintWriter(os, true)) {

            clientHandshake(in, out);
            sendFile(os, filename);

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    /**
     * Very basic handshake between client and seeder:
     *   1. Client sends chunk id and filename requested
     *   2. Seeder replies with the number of chunks in the file
     */
    private void clientHandshake(BufferedReader in, PrintWriter out) throws IOException {
        // In from client
        chunkID = Integer.parseInt(in.readLine());
        filename = in.readLine();

        setDirectory(filename);

        // Out to client
        int numChunks = getNumChunks();
        out.println(numChunks);
    }

    private void setDirectory(String filename) {
        // First check if there is an extension in the filename
        // (would have been stripped when creating chunk dir)
        directory = (filename.indexOf(".") > 0) ? filename.substring(0, filename.lastIndexOf("."))
                                                : filename;
    }

    /**
     * Counts number of files in the chunk directory for the requested video
     */
    private int getNumChunks() throws IOException {
        return (int)Files.list(Paths.get("chunks/" + directory)).count();
    }

    /**
     * Write from file over socket to client
     *
     * Using FileChannel for thread safety
     */
    private void sendFile(OutputStream os, String filename) {
        String filepath = "chunks/" + directory + "/" + filename + "-" + chunkID;
        File outgoingFile = new File(filepath);

        try(FileInputStream fis = new FileInputStream(outgoingFile);
            FileChannel ch = fis.getChannel()) {

            long fileLength = outgoingFile.length();

            int bytesRead;
            ByteBuffer buffer = ByteBuffer.allocate((int)fileLength);

            while ((bytesRead = ch.read(buffer)) > 0){
                os.write(buffer.array(), 0, bytesRead);

            }

            os.flush();

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
