package pt.fcup;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

class ChunkSeeder extends Thread {
    private final Socket socket;

    private String filename;
    private String filepath;
    private int numChunks;
    private int chunkID;
    private Seedbox sb;

    public ChunkSeeder(Socket socket) {
        this.socket = socket;

    }

    public void run() {
        sb = Seedbox.getSeedbox();

        try(
                OutputStream os = socket.getOutputStream();
                PrintWriter out = new PrintWriter(os, true);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()))
        )
        {
            filename = in.readLine();
            chunkID = Integer.parseInt(in.readLine());
            System.out.println(String.format("User requested chunk id #%s for file: %s", chunkID, filename));

            Seeder seederRequested = sb.seederHashMap.get(filename);
            filepath = seederRequested.getFullPath();
            numChunks = seederRequested.getNumberOfChunks();

            System.out.println("Sending back number of chunks: " + numChunks);
            out.println(numChunks);

            File file = new File(filepath + "-" + chunkID);

            try(FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis)) {

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
            }


        } catch (IOException e) {
            // Do nothing, client can request again.
            e.printStackTrace();

        }

    }
}
