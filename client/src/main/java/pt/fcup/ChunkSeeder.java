package pt.fcup;

import java.io.*;
import java.net.Socket;

/**
 * Executes handshake with user requesting chunk and transfers it via TCP socket
 * TODO: Pull out Server version into jar and use as local dependency
 */
class ChunkSeeder implements Runnable {
    private final int numChunks;
    private final Socket socket;

    private int chunkID;
    private String filepath;

    public ChunkSeeder(int numChunks, Socket socket) {
        this.socket = socket;
        this.numChunks = numChunks;

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
        out.println(numChunks);
    }

    /**
     * Write from file over socket to client
     */
    private void sendFile(OutputStream os) {
        File file = new File("sources/" + filepath + "-" + chunkID);

        System.out.println(String.format("Peer requested chunk id #%s for file: %s",
                                         chunkID,
                                         filepath));

        try(FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis)) {

            long fileLength = file.length();
            byte[] contents = new byte[(int)fileLength]; // 1MB

            int bytesRead;
            while ((bytesRead = bis.read(contents)) > 0) {
                os.write(contents, 0, bytesRead);
                System.out.println("Writing bytes " + contents);

            }

        } catch (FileNotFoundException e) {
            // TODO: Handle failure, deregister client

        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}