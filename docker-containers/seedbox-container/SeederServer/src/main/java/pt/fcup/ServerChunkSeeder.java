package pt.fcup;

import java.io.*;
import java.net.Socket;

class ServerChunkSeeder implements Runnable {
    private final Socket socket;

    private String filepath;
    private int chunkID;

    public ServerChunkSeeder(Socket socket) {
        this.socket = socket;

    }

    public void run() {
        try(OutputStream os = socket.getOutputStream();
            PrintWriter out = new PrintWriter(os, true);

            BufferedReader in = new BufferedReader(
                                new InputStreamReader(socket.getInputStream()))
        )
        {
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
    void clientHandshake(BufferedReader in, PrintWriter out) throws IOException {
        // In from client
        chunkID = Integer.parseInt(in.readLine());
        String filename = in.readLine();

        Seedbox sb = Seedbox.getSeedbox();
        Seeder seederRequested = sb.seederHashMap.get(filename);
        filepath = seederRequested.getFullPath();

        // Out to client
        int numChunks = seederRequested.getNumberOfChunks();
        out.println(numChunks);
    }

    /**
     * Write from file over socket to client
     */
    void sendFile( OutputStream os) {
        File outgoingFile = new File(filepath + "-" + chunkID);

        try(FileInputStream fis = new FileInputStream(outgoingFile);
            BufferedInputStream bis = new BufferedInputStream(fis)) {

            long fileLength = outgoingFile.length();

            int bytesRead;
            byte[] contents = new byte[(int)fileLength]; // 1MB
            while((bytesRead = bis.read(contents)) > 0){
                os.write(contents, 0, bytesRead);
            }

            os.flush();

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
