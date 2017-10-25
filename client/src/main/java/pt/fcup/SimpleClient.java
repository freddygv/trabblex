package pt.fcup;

import java.util.Properties;

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


    // TODO nicely print everything
    private JSONArray listSeeders()
    {

        try
        {
           
            if(verbose)
                System.out.println("Querying server...");

            // Query database
            String result = client.target(URL)
                                 .path("list")
                                 .request(MediaType.TEXT_PLAIN)
                                 .get(String.class);

            /*
                Here we consider that the client connects only to one
                server. Hence, we delete all local seeder info, then
                re-load it from the server.

                The client can't store info from multiple servers.

                If theory it would be possible, but would add dev hours,
                and not in the scope of this project.
            */
          
            /*
                Save seeder info locally for later use
                Display seeder info from server
            */
            

            /*********************************************
                Add json object to local json seeder info
                if it doesn't already exist

                Not really useful here, but could be used later 
                for multi-server management ?
            */

            /*
            JSONArray jrr = new JSONArray(result);
            localSeederInfo=new jsonArray("[{}]");*/

            /*for (int i = 0 ; i < jrr.length(); i++) 
            {
                JSONObject obj = jrr.getJSONObject(i);
            */
                
            /*    if(!localSeederInfo
                    .toString()
                    .contains("\"" + seeder_identifier + "\":\""
                        + obj.getString("" + seeder_identifier + "") + "\""))
                {
                    localSeederInfo.put(obj);

                }

            }********************************************/


            if(verbose)
                System.out.println("Server answered - copying and displaying results...");

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

   
        }
        catch(javax.ws.rs.ProcessingException e)
        {
            System.err.println("Cannot connect to server " + HOST);

        }
        catch(Exception e )
        {
            e.printStackTrace();

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

    /*
        Calls client manager to get chunk owners with 
        corresponding file hash
    */
    private String getChunksFromHash(String hash)
    {
        String result = null;
        try
        {
           
            if(verbose)
                System.out.println("Querying server...");

            // Query database
            result = client.target(URL)
                                 .path("getowners/" + hash)
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
    * Starts the download of a file
    * Via a TCP connection
    * @return a json of the specific seeders
    **/
    private boolean downloadFile(String name)
    {
        
      //  String hashToGet = getHashFromName(name);

        // TODO uncomment when working
      //  if(hashToGet == null)
      //      return false;

      //  String chunkOwners = getChunksFromHash(hashToGet); 

        // TODO uncomment when working
        //if(chunkOwners == null)
          //  return false;

        /*
            
        */
        
        /*
            Sort the owners by rarity and available-ness
        */

        /*
            Dummy test - take the first one that is active.
            If none is active, request creation of one (TODO)
        */

      //  JSONArray chunkOwnersJSON = new JSONArray(chunkOwners);
       
      //  JSONObject obj = chunkOwnersJSON.getJSONObject(0);
     //   System.out.println(obj.toString());

        /*
            Calculate the number of chunks the file has,
            and compare it to the chunks in the database.
            If some are missing, ask the client manager to create one
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
