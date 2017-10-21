package pt.fcup;

import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

import org.glassfish.jersey.client.*;
import javax.ws.rs.client.*;

import org.json.JSONObject;
import org.json.JSONArray;

public class SimpleClient {

    protected final String HOST = "http://127.0.0.1:8080";
    protected final String URL = HOST + "/trabblex/clientmanager/";

    private ClientConfig clientConfig;
    private Client client;

    public SimpleClient()
    {
        //clientConfig = new ClientConfig();
        client = ClientBuilder.newClient();

    }

    public void listSeeders()
    {

    }

    public void getResource()
    {
        String keywords = "test, test2, test3";
        try
        {
            String result = client.target(URL)
                                 .path("getfile/{file}")
                                 .resolveTemplate("file", keywords)
                                 .request(MediaType.TEXT_PLAIN_TYPE)
                                 .get(String.class);
            System.out.println(result);

        }
        catch(javax.ws.rs.ProcessingException e)
        {
            System.err.println("Cannot connect to server " + HOST);

        }
        catch(Exception e )
        {
            System.err.println("Unhandled error: " + e);

        }

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

    /**
    * Starts the download of a file
    * Via a TCP connection
    * @return a json of the specific seeders
    **/
    public boolean downloadFile(String name)
    {
        // Call client manager

        /*
            CM answers info from chunk_owners: (first chunk of the file)
                owner_ip
                chunk_hash - todo later
                file_hash - todo later
                Note: can be either another client or the portal's Seeder
            ATM, use dummy info
        */

        String file = "test.mp4";
        try
        {
            String result = client.target(URL)
                                 .path("getfile/{file}")
                                 .resolveTemplate("file", file)
                                 .request(MediaType.TEXT_PLAIN_TYPE)
                                 .get(String.class);
            System.out.println(result);

        }
        catch(javax.ws.rs.ProcessingException e)
        {
            System.err.println("Cannot connect to server " + HOST);

        }
        catch(Exception e )
        {
            System.err.println("Unhandled error: " + e);

        }



        // Connect via TCP to the seeder
        return false;
    }

    /**
    * Get info from all local files
    * @return file info in a json array
    **/
    public JSONArray listFiles(String hash)
    {
        return null;
    }

    /**
    * Creates a seeder for the designated file
    * @return all the seeders
    **/
    public JSONArray createSeed(String fileName)
    {
        // call client Manager
        return null;
    }

    public boolean informClientUnjoinable(String ip, int port)
    {
        // Inform client manager that a client is not joinable
        // eg disconnected from the network
        return false;
    }

    public static void main(String[] args)
    {
        SimpleClient sc = new SimpleClient();
        sc.getResource();

    }

    /**
    * Define the command-line parsing with Apache Commons CLI.
    * @return Definition of command-line options.
    */
    private static Options generateOptions()
    {
    final Option listOption = Option.builder("list")
        .required(false)
        .hasArg(false)
        .longOpt(LIST_OPTION)
        .desc("List all seeders available.")
        .build();

    final Options options = new Options();
        options.addOption(listOption);
        return options;
    }

}
