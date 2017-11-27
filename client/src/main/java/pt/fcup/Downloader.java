package pt.fcup;

import java.io.FileOutputStream;
import java.net.Socket;
import java.io.*;

class Downloader implements Runnable
{
    private final String ip;
    private final String file;
    private final int port;
    private final int chunkNumber;

    private int nbChunks;


    public Downloader(String file, int chunkNumber, String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.file = file;
        this.chunkNumber = chunkNumber;

    }

    @Override
    public void run() {
        // debug
        System.out.println(String.format("Requesting chunk %s of '%s' from: %s", chunkNumber, file, ip));

        // Make local directory for downloads if it doesn't exist
        File downloadDirectory = new File("downloads");
        if (!downloadDirectory.exists()) { downloadDirectory.mkdir(); }

        try (Socket clientSocket = new Socket(ip, port);

             PrintWriter out   = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(clientSocket.getInputStream()));

             DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
             FileOutputStream fos = new FileOutputStream(downloadDirectory + "/" + file + "-" + chunkNumber)) {

            clientSocket.setSoTimeout(10000);

            serverHandshake(in, out);
            download(dis, fos);

            // debug
            System.out.println(String.format("Downloaded chunk %s of '%s' successfully", chunkNumber, file));

        } catch(IOException  e) {
            System.out.println("Couldn't connect to " + ip + ":" + port);
            e.printStackTrace();

        }

    }

    /**
     * Download chunk to file
     */
    private void download(DataInputStream dis, FileOutputStream fos) throws IOException {
        byte[] contents = new byte[1024*1024];
        int bytesRead;

        while ((bytesRead = dis.read(contents)) > 0) {
            fos.write(contents, 0, bytesRead);

        }
    }

    /**
     * Basic handshake with the server
     */
    private void serverHandshake(BufferedReader in, PrintWriter out) throws IOException {
        // Chunk request to server
        out.println(chunkNumber);
        out.println(file);

        // Number of chunks from server
        nbChunks = Integer.parseInt(in.readLine());

    }

    public int getNbChunks() {
        return nbChunks;

    }
}