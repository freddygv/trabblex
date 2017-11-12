package pt.fcup;

import java.io.*;
import java.net.*;

class ChunkSeeder extends Thread {
    private final int port;
    private final int numChunks;
    private Socket socket;

    private int chunkID;

    public ChunkSeeder(int port, int numChunks, Socket socket) {
        this.socket = socket;
        this.port = port;
        this.numChunks = numChunks;
    }

    public void run() {
        System.out.println("Starting local chunk seeder");
        System.out.println("Path%" + System.getProperty("user.dir"));
        transferChunk();

    }

    private void transferChunk() {

        try{

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            chunkID = Integer.parseInt(in.readLine());
            String filepath = in.readLine();

            //System.out.println("Local seeder seeding file " + filepath);
            File file = new File("sources/" + filepath + "-" + chunkID);

            System.out.println(String.format("User requested chunk id #%s for file: %s", chunkID, filepath));

            OutputStream os = socket.getOutputStream();
            PrintWriter out = new PrintWriter(os, true);
            out.println(numChunks);

            if(!file.exists())
            {
                System.out.println("Error opening file " + "sources/" + filepath + "-" + chunkID);
            }
            else
            {
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);


                long fileLength = file.length();

                byte[] contents = new byte[(int)fileLength]; // 1MB
                int bytesRead = 0;

                while((bytesRead = bis.read(contents)) > 0){
                    os.write(contents, 0, bytesRead);
                    System.out.println("Writing bytes " + contents);
                }
            }

            os.flush();
            os.close();

        } catch (IOException e) {
            // Do nothing, client can request again.
            e.printStackTrace();

        }
    }
}
