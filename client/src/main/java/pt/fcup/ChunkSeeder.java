package pt.fcup;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Executes handshake with user requesting chunk and transfers it via TCP socket
 * TODO: Pull this out into a local dependency jar
 */
class ChunkSeeder implements Runnable {
    private final Socket socket;

    private int chunkID;
    private String filepath;

    public ChunkSeeder(Socket socket) {
        this.socket = socket;

    }

    public void run() {
        try(BufferedReader in = new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));

            OutputStream os = socket.getOutputStream();
            PrintWriter out = new PrintWriter(os, true)) {

            clientHandshake(in, out);
            sendFile(os);

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
        // In from peer
        chunkID = Integer.parseInt(in.readLine());
        filepath = in.readLine();

        // Out to peer
        out.println(SimpleClient.chunkCounts.get(filepath));
    }

    /**
     * Write from file over socket to client
     *
     * Using FileChannel for thread safety
     */
    private void sendFile(OutputStream os) {
        File outgoingFile = new File("sources/" + filepath + "-" + chunkID);

        try(FileInputStream fis = new FileInputStream(outgoingFile);
            FileChannel ch = fis.getChannel()) {

            long fileLength = outgoingFile.length();

            int bytesRead;
            ByteBuffer buffer = ByteBuffer.allocate((int)fileLength);

            while ((bytesRead = ch.read(buffer)) > 0){
                os.write(buffer.array(), 0, bytesRead);

            }

            os.flush();

        } catch (IOException e) {
            e.printStackTrace();
            // TODO: Handle failure, deregister this user

        }
    }
}