package pt.fcup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.json.JSONObject;
import org.json.JSONArray;


public class SimpleClient {
    private final String MODE = "remote";

    private Map<String, String> hashes = new HashMap<>();
    private JerseyClient client;
    static UploadServer uploader;

    public SimpleClient() {
        client = (MODE == "local") ? new JerseyClient("http://127.0.0.1:8080",
                "/trabblex/clientmanager/")
                : new JerseyClient("http://35.195.218.215:8080",
                "/trabblex/clientmanager/");

    }

    public static void main(String[] args) {
        SimpleClient sc = new SimpleClient();
        sc.run();

    }

    private void run() {
        String input;
        Scanner sc = new Scanner(System.in);

        displayHelp();

        do{
            System.out.print("> ");
            input = sc.nextLine();
            String[] parts = input.split(" ");

            switch(parts[0]) {
                case "download":
                    if (parts.length == 2) {
                        String filename = parts[1];
                        downloadFile(filename);

                    } else { System.out.println("Usage: download [filename]"); }
                    break;

                case "list":
                    if (parts.length == 2 && parts[1].equals("files")) {
                        listSeeders();
                        break;

                    } else if (parts.length == 2 && parts[1].equals("videos")) {
                        listFiles();
                        break;

                    } else { System.out.println("Usage: 'list files'"); }
                    break;

                case "play":
                    if (parts.length == 2) {
                        String filename = parts[1];
                        playFile(filename);

                    } else { System.out.println("Usage: play [filename]"); }
                    break;

                default:
                    displayHelp();
                    break;

            }

        } while (input != "quit");
    }

    private void displayHelp() {
        System.out.println("=========================================");
        System.out.println("Simple client coded by Freddy and Quentin");
        System.out.println("=========== Available commands===========");
        System.out.println("video list");
        System.out.println("download [filename]");
        System.out.println("list files");
        System.out.println("play [filename]");
        System.out.println("quit");
        System.out.println("=========================================");
    }

    /**
     *   @return a list of the seeder on the remote server
     **/
    private void listSeeders() {
        String result = client.query("list", null);

        if (result == null) {
            System.err.println("Error querying the server for the seeds");

        } else {
            printNicely(result);

        }
    }

    /**
     * Prints out video metadata
     */
    private void printNicely(String result) {
        JSONArray localSeederInfo = new JSONArray(result);

        for (int i = 0 ; i < localSeederInfo.length(); i++) {
            JSONObject obj = localSeederInfo.getJSONObject(i);
            System.out.println(obj.getString("file_name")
                    + ": " + obj.getString("file_size") + "Kb"
                    + " (" + obj.getString("video_size_x") + "x"
                    + obj.getString("video_size_y") + " @ "
                    + obj.getString("bitrate") + "b/s" + ")");

            hashes.put(obj.getString("file_name"), obj.getString("file_hash"));
        }
    }

    /**
     * Play downloaded video
     */
    private void playFile(String file) {
        try {
            Runtime.getRuntime().exec("ffplay downloads/" + file);

        } catch(IOException e) {
            System.err.println("Error: could not read file!");

        }
    }


    /**
    * Starts the download of a file
    **/
    private void downloadFile(String name) {
        FileDownloader f = new FileDownloader(name, hashes.get(name), client);
        Thread fdThread = new Thread(f);
        fdThread.start();

    }

    /**
    * Get info from all local files
    **/
    private void listFiles() {
        File directory = new File("downloads/");

        for (File video : directory.listFiles()) {
            System.out.println("> " + video.getName() + " (" + video.length()/(1024*1024) + " MB)");

        }
    }
}
