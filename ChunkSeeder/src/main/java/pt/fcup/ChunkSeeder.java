package pt.fcup;

import java.io.*;
import java.net.*;

class ChunkSeeder extends Thread {
    private final int port;
    private final String filepath;
    private final String filename;
    private final int numChunks;
    private final Socket socket;

    private int chunkID;

    public ChunkSeeder(int port, int numChunks, String filepath, String filename, Socket socket) {
        this.socket = socket;
        this.port = port;
        this.numChunks = numChunks;
        this.filepath = filepath;
        this.filename = filename;

    }

    public void run() {
        transferChunk();
        System.out.println("File sent succesfully!");

    }

    private void transferChunk() {

        try{
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            chunkID = Integer.parseInt(in.readLine());
            File file = new File(filepath + "-" + chunkID);
            FileInputStream fis = new FileInputStream(file);

            BufferedInputStream bis = new BufferedInputStream(fis);

            OutputStream os = socket.getOutputStream();

            System.out.println(String.format("User requested chunk id #%s for file: %s", chunkID, filename));

            System.out.println("Sending back number of chunks: " + numChunks);
            out.println(numChunks);

            long fileLength = file.length();

            byte[] contents = new byte[(int)fileLength]; // 1MB
            int bytesRead = 0;

            while((bytesRead = bis.read(contents)) > 0){
                os.write(contents, 0, bytesRead);

                // update every 20%
                if((bytesRead*100)/fileLength % 20 == 0) {
                    System.out.print("Sending file ... " + (bytesRead * 100) / fileLength + "% complete!");
                }
            }

            os.flush();

        } catch (IOException e) {
            // Do nothing, client can request again.
            e.printStackTrace();

        }
    }
}
