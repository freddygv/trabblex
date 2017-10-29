package pt.fcup;

import java.util.Properties;
import java.util.ArrayList;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;


/*import org.glassfish.jersey.client.*;*/
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Scanner;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;


public class SimpleClient {

    private boolean verbose = false;
    private final int chunkSize = 1024*10; // bytes

    JSONArray localSeederInfo= new JSONArray();
    JerseyClient client;

    //JSONArray localChunks = new JSONArray();

    // max number of concurrent downloads
    private int maxDownloads = 3;

    // could be used later for multi-server management
    //private final String seeder_identifier = "file_hash";


    public SimpleClient(String[] args)
    {
        client = new JerseyClient("http://127.0.0.1:8080", "/trabblex/clientmanager/");
    }

    private void run()
    {
        String input;
        Scanner sc = new Scanner(System.in);

        System.out.println("Client started");

        do{
            System.out.print("> ");
            input = sc.nextLine();
            String[] parts = input.split(" ");

            switch(parts[0]){
                case "seeder":
                    if(parts.length < 2)
                        displayHelp();
                    else{
                        switch(parts[1]){
                            case "list":
                                listSeeders();
                                break;

                            case "search":
                                break;
                        }
                    }
                    break;

                case "download":
                    if(parts.length < 2)
                        displayHelp();
                    else
                    {
                        // if file has spaces...
                        String filename = parts[1];
                        for(int i = 2; i < parts.length; i++)
                            filename = filename + " " + parts[i];
                        downloadFile(filename);
                    }
                    break;

                case "list":
                    break;

                case "info":
                    break;

                case "play":
                    break;

                case "verbose":
                    verbose = !verbose;
                    System.out.println("Verbose is now set to " + verbose);
                    break;

                default:
                    displayHelp();
                    break;

                case "setmaxdownloads":
                    if(parts.length < 2)
                        displayHelp();
                    else
                        maxDownloads = Integer.parseInt(parts[1]);
                    break;

            }
        } while(input != "quit");
    }

    private void displayHelp()
    {
        System.out.println("===========================");
        System.out.println("Simple client coded by Freddy and Quentin");
        System.out.println("=== Available commands===");
        System.out.println("seeder list");
        System.out.println("seeder search keywords");
        System.out.println("download file");
        System.out.println("list files");
        System.out.println("info file");
        System.out.println("play name");
        System.out.println("setmaxdownloads x");
        System.out.println("===========================");
    }

   

    /**
    *   @return a list of the seeder on the remote server
    **/
    private JSONArray listSeeders()
    {
        String result = client.query("list", null);

        if(result == null)
        {
            System.err.println("Error querying the server for the seeds");
        }

        localSeederInfo = new JSONArray(result);


        /*
            Display seeder info nicely
        */
        for (int i = 0 ; i < localSeederInfo.length(); i++) 
        {
            JSONObject obj = localSeederInfo.getJSONObject(i);
            System.out.println(obj.getString("file_name")
                + ": " + obj.getString("file_size") + "Kb"
                + " (" + obj.getString("video_size_x") + "x"
                + obj.getString("video_size_y") + " @ "
                + obj.getString("bitrate") + "b/s" + ")"
                );

            if(verbose)
            {
                System.out.println(">> file_hash: " + obj.getString("file_hash"));
                System.out.println(">> protocol: " + obj.getString("protocol"));
                System.out.println(">> port: " + obj.getString("port"));
            }
        }

        return null;
    }


    /**
    * Get file info
    * if completely downloaded, full path, size; if
    * being downloaded: file size and neighbor list
    * Since the client can only download one file at a time, 
    *   only display info on that
    * @return file info in a json array
    **/
    public JSONArray fileInfo(String fileName)
    {
        return null;
    }


    /*
        Get file hash from local stored data
        This could be done server-side, but by doing it client-side
        we reduce the number of server queries
        Also, ensures consistency if file name on server changes meanwhile
        TODO later create a seeder class to alleviate work ?
    */
    private String getHashFromName( String name )
    {
      
        String hashToGet = null;

        if(verbose)
            System.out.println("Searching for " + name);

        for (int i = 0; i < localSeederInfo.length(); i++)
        {
            if (localSeederInfo.getJSONObject(i).getString("file_name").equals(name))
            {
                hashToGet = localSeederInfo.getJSONObject(i).getString("file_hash");

            }
        }

        if(hashToGet == null)
        {
            System.out.println("File not found: " + name);

        }
        else
        {
            if(verbose)
                System.out.println("Found, hash= " + hashToGet);
        }

        return hashToGet;
    }


    /**
    * Starts the download of a file
    * TODO manage join ?
    **/
    private void downloadFile(String name)
    {
        (new FileDownloader(name, getHashFromName(name), client)).start();
    }

    /**
    * Get info from all local files
    * @return file info in a json array
    **/
    private JSONArray listFiles(String hash)
    {
        return null;
    }

    private boolean informClientUnjoinable(String ip, int port)
    {
        // Inform client manager that a client is not joinable
        // eg disconnected from the network
        return false;
    }

    public static void main(String[] args)
    {
        SimpleClient sc = new SimpleClient(args);
        sc.run();
    }

    

}
