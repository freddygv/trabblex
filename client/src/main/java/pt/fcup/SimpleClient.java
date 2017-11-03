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

    // could be used later for multi-server management
    //private final String seeder_identifier = "file_hash";

    ArrayList<FileDownloader> files = new ArrayList<FileDownloader>();


    public SimpleClient(String[] args)
    {
        client = new JerseyClient("http://35.195.218.215:8080", "/trabblex/clientmanager/");
    }

    private void run()
    {
        String input;
        Scanner sc = new Scanner(System.in);

        System.out.println("Client started");
        displayHelp();

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
                            if(parts.length < 3)
                                displayHelp();
                            else{
                                searchFromKeyword(parts[2]);
                            }
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
                    listFiles();
                    break;

                case "info":
                    if(parts.length < 2)
                        displayHelp();
                    else
                    {
                        // if file has spaces...
                        String filename = parts[1];
                        for(int i = 2; i < parts.length; i++)
                            filename = filename + " " + parts[i];
                        fileInfo(filename);
                    }
                    break;

                case "play":
                if(parts.length < 2)
                        displayHelp();
                    else
                    {
                        // if file has spaces...
                        String filename = parts[1];
                        for(int i = 2; i < parts.length; i++)
                            filename = filename + " " + parts[i];
                        playFile(filename);
                    }
                    break;

                case "verbose":
                    verbose = !verbose;
                    System.out.println("Verbose is now set to " + verbose);
                    break;

                default:
                    displayHelp();
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
        System.out.println("===========================");
    }

   
    private void searchFromKeyword(String keyword)
    {
        String result = client.query("listfromkeyword", keyword);

        if(result == null)
        {
            System.err.println("Error querying the server");
        }
        else
        {         
            JSONArray localSeederInfo = new JSONArray(result);

            /*
                Display result info nicely
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
        }

    }

    /**
    *   @return a list of the seeder on the remote server
    **/
    private void listSeeders()
    {
        String result = client.query("list", null);

        if(result == null)
        {
            System.err.println("Error querying the server for the seeds");
        }
        else{

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
        }

    }

    private void playFile(String file)
    {
        try{
            Runtime.getRuntime().exec("ffplay downloads/" + file);
        }
        catch(Exception e){
            System.err.println("Error could not read file!");
        }
    }


    /**
    * Get file info
    * if completely downloaded, full path, size; if
    * being downloaded: file size and neighbor list
    **/
    public void fileInfo(String fileName)
    {
        // get number of file
        for(int i = 0; i < files.size(); i++)
        {
            FileDownloader f = files.get(i);
            if(f.getFileName().equals(fileName)){
                System.out.println("File info " + f.getFileName());

                if(f.getnbChunksNotDownloaded() <= 1)
                    System.out.println("Downloading Finished");
                else 
                    System.out.println("Downloading " + f.getnbChunksNotDownloaded() + " chunks");

                System.out.println("Full path = " + "downloads/" + fileName);
                File fa =new File("downloads/" + fileName);
                System.out.println("File size = " + fa.length()/(1024*1024) + "MB");

                // neighbor list

            }
        }
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
        FileDownloader f = new FileDownloader(name, getHashFromName(name), client);
        f.start();
        files.add(f);
    }

    /**
    * Get info from all local files
    **/
    private void listFiles()
    {
        for(int i = 0; i < files.size(); i++)
        {
            FileDownloader f = files.get(i);
            System.out.println("=================");
            System.out.println(f.getFileName());
            // TODO why stuck at one ? Resolve...
            if(f.getnbChunksNotDownloaded() <= 1)
                System.out.println("Downloading Finished");
            else 
                System.out.println("Downloading " + f.getnbChunksNotDownloaded() + " chunks");


        }
    }

    public static void main(String[] args)
    {
        SimpleClient sc = new SimpleClient(args);
        sc.run();
    }

    

}
