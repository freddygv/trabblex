package pt.fcup;

import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.io.*;


class DummyDownloader {
    String file = "tl_512kb.mp4";
    int nbChunks = 3;
    String ip = "localhost";
    String protocol = "TCP";
    String hash = "972C57B6BBADB9146778DFA6C503C19D9B4DC056AD50CD41B38FCC65E0D21162";
    int port = 30740;
    int chunkSize;
    String chunkNumber = "1";

    public static void main(String[] args) {
        new DummyDownloader().run();
    }

    public void run() {

        if (protocol != "TCP") {
            System.out.println("Sorry, " + protocol + " is not yet supported!");
            Thread.currentThread().interrupt();
        }

        try (
                Socket clientSocket = new Socket(InetAddress.getByName(ip), port);

                PrintWriter out =
                        new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(clientSocket.getInputStream()));

                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());

                FileOutputStream fos = new FileOutputStream("downloads/" + file + "-" + chunkNumber);
        ) {

            System.out.println("Running for chunk number " + chunkNumber);
            out.println(chunkNumber);

            nbChunks = Integer.parseInt(in.readLine());
            System.out.println("Number of chunks is: " + nbChunks);

            byte[] contents = new byte[1024*1024];
            int bytesRead = 0;
            while ((bytesRead = dis.read(contents)) > 0) {
                fos.write(contents, 0, bytesRead);

            }

            fos.flush();
            System.out.println("Downloaded chunk " + hash + " successfully");

        } catch (IOException e) {
            e.printStackTrace();

        }

    }

}