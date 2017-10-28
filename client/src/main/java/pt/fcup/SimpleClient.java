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

    private boolean verbose = false;
    private final int chunkSize = 1024*10; // bytes

    JSONArray localSeederInfo;
    JerseyClient client;

    // max number of concurrent downloads
    private int maxDownloads = 3;

    // could be used later for multi-server management
    //private final String seeder_identifier = "file_hash";


    public SimpleClient(String[] args)
    {
        localSeederInfo = new JSONArray();
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
            System.out.println("File not found !");

        }
        else
        {
            if(verbose)
                System.out.println("Found, hash= " + hashToGet);
        }

        return hashToGet;
    }

    private Hashtable<String, Integer> sortAvailableChunks( int nbChunksAvailable, JSONArray remoteChunkOwners )
    {
        nbChunksAvailable = 0;
        Hashtable chunks<String, Integer> = new Hashtable<String, Integer>();

        for (int i = 0 ; i < remoteChunkOwners.length(); i++) 
        {
            JSONObject obj = remoteChunkOwners.getJSONObject(i);
            String hash = obj.getString("chunk_hash");
            if(!chunks.containsKey(hash))
            {
                chunks.put(hash, 1);
            }
            else
            {
                chunks.put(hash, chunks.get(hash) + 1);
            }
            nbChunksAvailable ++;
        }

        return chunks;
    }


    /**
    * Starts the download of a file
    * Via a TCP connection
    * @return a json of the specific seeders
    * TODO implement handshake
    **/
    private boolean downloadFile(String name)
    {
        // number of chunks in the file
        int nbChunks = 0;

        // the hash of the file we wish to download
        String hashToGet = null;

        // the owners of the chunk of the file (seeder + other clients)
        JSONArray remoteChunkOwners = null;

        // number of chunks available online (seeder + other clients)
        int nbChunksAvailable = 0;

        
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

        remoteChunkOwners = new JSONArray(chunkOwners);


        /*
            (2) Fetch the number of chunks the file has
        */

        String nbChunksStr = client.query("getnumberofchunks", name);

        if(nbChunksStr == null)
        {
            System.err.println("Couldn't get number of file chunks !");
            return false;
        }

        nbChunks = Integer.parseInt(nbChunksStr);

        if(verbose)
        {
            System.out.println("Chunks in the file: " + nbChunks);
            System.out.println("Chunks available for download: " + nbChunksAvailable);
        }


        /*
            (3) Calculate the number of unique chunks available, 
            and for each one how many sources are available
            Note: this method means that the downloader works with
            only one file at a time...
        */
        // Hashtable <chunk_hash, number_available>
        /*Hashtable chunks<String, Integer> 
                = sortAvailableChunks(nbChunksAvailable, 
                                     remoteChunkOwners);*/

        /*
            (4) If some chunk owners are missing, ask the client manager to create a seeder
            that will provide those chunks
            And then restart from the beginning...
        */
       /* if(nbChunksAvailable != nbChunks)
        {
            String newSeeder = client.query("createseeder", name);
            if(newSeeder == null)
            {
                System.err.println("Error requesting the creation of a new seeder");
                return false;
            }
            
            // restart, only this time with all the seeders needed...
            return downloadFile(String name);
        }*/

        /*
            (5) Get the chunks by rarity using "chunks", 
            and send all the corresponding seeders to the downloader using 
        */


        

        /*
            (5) Start downloaders based on rarity
            Give the downloader all the seeders for the chunk
            If one seeder fails, he will take the next source
            If no sources available, request the creation of a seeder
        */

        //JSONArray chunkOwnersJSONRequest = createSeeder(obj.getString("file_hash"));

        /*
            Starts a new seeder that downloads the file
        */
        ArrayList<Byte> file = new ArrayList<Byte>();
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
