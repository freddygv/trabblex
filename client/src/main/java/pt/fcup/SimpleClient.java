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

    JSONArray localSeederInfo;

    // max number of concurrent downloads
    private int max_downloads = 3;

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
                    {
                        displayHelp();
                    
                    }
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
                    {
                        displayHelp();
                    
                    }
                    else
                    {
                        downloadFile(parts[1]);

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
                    {
                        displayHelp();
                    
                    }
                    else
                    {
                        max_downloads = Integer.parseInt(parts[1]);
                    }
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
            System.err.println("Unhandled error: " + e);

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
      
        String hash_to_get = null;

        if(verbose)
            System.out.println("Searching for " + name);

        for (int i = 0; i < localSeederInfo.length(); i++)
        {
            if (localSeederInfo.getJSONObject(i).getString("file_name").equals(name))
            {
                hash_to_get = localSeederInfo.getJSONObject(i).getString("file_hash");

            }
        }

        if(hash_to_get == null)
        {
            System.out.println("File not found !");

        }
        else
        {
            if(verbose)
                System.out.println("Found, hash= " + hash_to_get);
        }

        return hash_to_get;
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
            System.err.println("Unhandled error: " + e);

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
        
        String hash_to_get = getHashFromName(name);

        if(hash_to_get == null)
            return false;

        String chunk_owners = getChunksFromHash(hash_to_get); 

        if(chunk_owners == null)
            return false;

        
        /*
            Sort the owners by rarity 
        */

        /*
            CM answers info from chunk_owners: (first chunk of the file)
                owner_ip
                chunk_hash - todo later
                file_hash - todo later
                Note: can be either another client or the portal's Seeder
            ATM, use dummy info
        */

       /* String file = "test.mp4";
        try
        {
            /*String result = client.target(URL)
                                 .path("getfile/{file}")
                                 .resolveTemplate("file", file)
                                 .request(MediaType.TEXT_PLAIN_TYPE)
                                 .get(String.class);
            System.out.println(result);*/
         /*   WebTarget webTarget 
                = client.target(URL);
            WebTarget employeeWebTarget 
                = webTarget.path("resources/employees");
            Invocation.Builder invocationBuilder 
                = employeeWebTarget.request(MediaType.APPLICATION_JSON);

            String response 
                = invocationBuilder.get(String.class);

        }
        /*        catch(javax.ws.rs.ProcessingException e)
        {
            System.err.println("Cannot connect to server " + HOST);

        }*/
      /*  catch(Exception e )
        {
            System.err.println("Unhandled error: " + e);

        }
    */


        // Connect via TCP to the seeder
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
    private JSONArray createSeed(String fileName)
    {
        // call client Manager
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
        //sc.downloadFile("blablabla");
    }

    

}
