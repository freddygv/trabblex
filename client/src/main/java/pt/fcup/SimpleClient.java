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


public class SimpleClient {

    protected final String HOST = "http://127.0.0.1:8080";
    protected final String URL = HOST + "/trabblex/clientmanager/";

    private Client client;

    private boolean verbose = false;
    private final int chunkSize = 1024*10; // bytes

    JSONArray localSeederInfo;

    // max number of concurrent downloads
    private int maxDownloads = 3;

    // could be used later for multi-server management
    //private final String seeder_identifier = "file_hash";


    public SimpleClient(String[] args)
    {
        client = ClientBuilder.newClient();
        localSeederInfo = new JSONArray();
    }

    private void run()
    {
        String input;
        Scanner sc = new Scanner(System.in);

        System.out.println("Client started");

        do
        {
            input = sc.nextLine();
            String[] parts = input.split(" ");

            switch(parts[0])
            {
                case "seeder":
                    if(parts.length < 2)
                        displayHelp();
                    
                    else
                    {
                        switch(parts[1])
                        {
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
                        downloadFile(parts[1]);
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

        } 
        while(input != "quit");
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

    private queryClientManager(String path, String param)
    {
        String result = null;

        try
        {
            if(param != null)
            {
                path = path + "/";
            }

            // Query database
            result = client.target(URL)
                                 .path(path + param)
                                 .request(MediaType.TEXT_PLAIN)
                                 .get(String.class);
        }
        catch(javax.ws.rs.ProcessingException e)
        {
            System.err.println("Cannot connect to server " + HOST);

        }
        catch(Exception e )
        {
            e.printStackTrace();
        }  

        return result;
    }

    /**
    *   @return a list of the seeder on the remote server
    **/
    private JSONArray listSeeders()
    {
        String result = queryClientManager("list", null);

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
                + ": " + obj.getString("file_size") + "MB"
                + " (" + obj.getString("video_size_x") + "x"
                + obj.getString("video_size_y") + " @ "
                + obj.getString("bitrate") + "b/s" + ")"
                );

            if(verbose)
            {
                // Maybe remove seeder ip, not really necessary
                System.out.println(">> seeder_ip: " + obj.getString("seeder_ip"));
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
            System.out.println("File not found !");

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
        // TODO local chunk management
        int nbChunks = 0;
        int nbChunksAvailable = 0;
        
        /*
            (1) Get all the chunk owners related to the client
        */
        String hashToGet = getHashFromName(name);

        if(hashToGet == null)
        {
            System.err.println("Couldn't solve hash from filename!");
            return false;
        }

        String chunkOwners = queryClientManager("getowners", hashToGet);

        if(chunkOwners == null)
        {
            System.err.println("Couldn't get the chunk owners of the file!");
            return false;
        }

        JSONArray remoteChunkOwners = new JSONArray(chunkOwners);

        /*
            Count number of chunks available for download
        */

        /*
            NOTE: May not be possible... we don't know the
            hashes of certain files ! 
            We need all the hashes to dispatch the downloaders
            Not needed if we manage seeder request creation in 
            the downloader, if he can't find a source...
        */
       /* Hashtable chunks<String, String> = new Hashtable<String, String>();
        for (int i = 0 ; i < remoteChunkOwners.length(); i++) 
        {
            JSONObject obj = remoteChunkOwners.getJSONObject(i);
            String hash = obj.getString("chunk_hash");
            if(!chunks.contains(hash))
            {
                chunks.add(hash);
            }
            nbChunksAvailable ++;
        }*/

        /*
            Dummy test - take the first one that is active.
            If none is active, request creation of one (TODO)
        */

      //  JSONArray chunkOwnersJSON = new JSONArray(chunkOwners);
       
      //  JSONObject obj = chunkOwnersJSON.getJSONObject(0);
     //   System.out.println(obj.toString());

        /*
            (2) Fetch the number of chunks the file has,
            and compare it to the chunks available in the seedbox.
        */

        String nbChunksS = queryClientManager("getnumberofchunks", name);ksS

        if(nbChunkss == null)
        {
            System.err.println("Couldn't get number of file chunks !");
            return false;
        }

        nbChunks = Integer.parseInt(nbChunksS);

        if(verbose)
        {
            System.out.println("Chunks in the file: " + nbChunks);
            System.out.println("Chunks available for download: " + nbChunksAvailable);
        }


        /*
            (3) If some chunk owners are missing, ask the client manager to create a seeder
            that will provide those chunks
        */
        if(nbChunksAvailable != nbChunks)
        {
            if(requestCreateSeeder(name) == false)
                return false;

            // now, get (again) all the chunks
            // TODO what if a client disconnects during the process ? gotta
            // separate all that code...
            // TODO store chunk owners
            /*
                Maybe:
                - manage all that in the downloader
                - store chunk being downloaded 
                - start x-1 downloaders

                - Create a pool of nbChunks downloader processes and use a semaphore for them
                to wait for each other ?
                - In this case, how to manage chunk priorities ?
                https://docs.oracle.com/javase/tutorial/essential/concurrency/pools.html
                - Manage seeder request creation in the downloader, if he can't find a source...
            */
            String chunkOwners = queryClientManager("getowners", hashToGet);
        }

        /*
            (4) Start downloaders based on rarity
            If one fails, give him the next source for the chunk
            If no sources available, request the creation of a seeder
        */

        //JSONArray chunkOwnersJSONRequest = createSeeder(obj.getString("file_hash"));

        /*
            Starts a new seeder that downloads the file
        */
        byte[] chunkTest = new byte[chunkSize];

        /*Downloader dwl = new Downloader(
            obj.getString("file_name"),
            obj.getString("seeder_ip"), Integer.parseInt(obj.getString("port")),
            obj.getString("protocol"),
            chunkTest
        );*/
        Downloader dwl = new Downloader(
            "test-popeye.mp4",
            "localhost", 26000,
            "TCP",
            chunkTest
        );
        dwl.start();
        try{
            dwl.join();   
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


        // check chunk hash

        return false;
    }

    /**
    * Requests the creation of a seeder for a file
    * @return false if failure, true if success
    **/
    private boolean requestCreateSeeder(String fileName)
    {
       // TODO call client manager

        return false;

    }

    /**
    * Get info from all local files
    * @return file info in a json array
    **/
    private JSONArray listFiles(String hash)
    {
        return null;
    }

    /**
    * Creates a seeder for the designated file
    * @return all the seeders
    **/
    private JSONArray createSeeder(String fileHash)
    {
        // call client Manager - createSeeder
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
