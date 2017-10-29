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
            System.out.println("> ");
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
    * Via a TCP connection
    * @return a json of the specific seeders
    * TODO implement handshake
    **/
    private boolean downloadFile(String name)
    {
        // the hash of the file we wish to download
        String hashToGet = null;

        // the owners of the chunk of the file (seeder + other clients)
        JSONArray remoteChunkOwners = null;

        // the protocol that will be used - atm, fixed
        String protocol = "TCP";

        // storing the file
        ArrayList<Byte> file = new ArrayList<Byte>();
        
        /*
            (1) Fetch all the chunk owners related to the client
        */

        hashToGet = getHashFromName(name);

        if(hashToGet == null)
        {
            System.err.println("Couldn't solve hash from filename!");
            return false;
        }

        String chunkOwners = client.query("getowners", hashToGet);

        if(chunkOwners == null)
        {
            System.err.println("Couldn't get the chunk owners of the file!");
            return false;
        }

        // if no seeders available, request a new one
        if(chunkOwners == "[]")
        {
            String newSeeder = client.query("createseeder", name);
            if(newSeeder == null)
            {
                System.err.println("Error requesting the creation of a new seeder");
                return false;
            }
            
            // restart, only this time with all the seeders needed...
            return downloadFile(name);   
        }

        remoteChunkOwners = new JSONArray(chunkOwners);

        /*
            (2) Starting phase - download the first chunk from the first owner available
            In order to get metadata on the file
        */


        JSONObject obj = remoteChunkOwners.getJSONObject(0);
        int chunkNumber = obj.getInt("chunk_id"); // TODO modify based on database modifs by Freddy

        ChunkManager chm = new ChunkManager(remoteChunkOwners);

        Downloader firstdwl = new Downloader(
            name,
            chunkNumber,
            obj.getString("owner_ip"), 
            Integer.parseInt(obj.getString("owner_port")),
            protocol
        );

        // the thread will automatically save the file locally
        firstdwl.start();

        try{
            firstdwl.join();   
            // store chunk info in local
            //localChunks.put(obj);
            chm.markChunkDownloaded(chunkNumber);
            // TODO update database
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }


        /*
            (4) Assess if all chunks are available 
            If no, create a seeder, and start all over again
        */
        int nbChunksInFile = firstdwl.getNbChunks();
        if(chm.getNbChunksAvailable() != nbChunksInFile)
        {
            String newSeeder = client.query("createseeder", name);
            if(newSeeder == null)
            {
                System.err.println("Error requesting the creation of a new seeder");
                return false;
            }
            
            // restart, only this time with all the seeders needed...
            return downloadFile(name);
        }

        /*
            At this point,
            we normally have all the chunk owners we need
        */

        /* 
            (5) Download chunks one by one
            TODO later pool of downloaders
            TODO priority management (later...)
                -- could be done by using a treemap instead of hashmap for remoteChunkOwners
        */
        while(chm.numberOfChunksNotDownloaded() > 0)
        {
            // determine next chunk to download
            Chunk nextChunkToDownload = chm.getRarestChunk();

            if(nextChunkToDownload == null)
            {
                System.err.println("Error couldn't get a source for the next chunk !");
                System.err.println("Note: this shouldn't be happening");
            }

            // get a source for this chunk
            Owner chunkSource = nextChunkToDownload.getSource();

            // start downloader
            Downloader dwl = new Downloader(
                name,
                nextChunkToDownload.chunkNumber,
                chunkSource.ip,
                chunkSource.port,
                chunkSource.protocol
            );

            // the thread will automatically save the file locally
            dwl.start();

            // wait for it to finish
            try{
                dwl.join();   
            }
            catch(Exception e)
            {
                e.printStackTrace();
                return false;
            }
            // TODO add custom exception: source unjoinable, then inform chunkmanager
            // that it needs to remove that source

            // TODO manage local seeder

            // TODO check file hash

            // mark chunk as downloaded
            chm.markChunkDownloaded(nextChunkToDownload.chunkNumber);

            // TODO update database

        }

        // terminate all local seeders
        // update database

        // assemble file
        assembleFile(name, nbChunksInFile);

        return false;
    }

    private boolean assembleFile(String name, int nbChunks)
    {
        try (FileOutputStream fos = new FileOutputStream("downloads/" + name);
            BufferedOutputStream mergingStream = new BufferedOutputStream(fos)) {
            for (int i = 0; i < nbChunks; i++) 
            {
                java.nio.file.Path localFile = Paths.get("downloads/" + name + "-" + i);
                Files.copy(localFile, mergingStream);
            }
        }   
        catch(IOException e)
        {
            System.err.println("Error re-assembling the file\n");
            e.printStackTrace();
            return false;
        }
        return true;
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
