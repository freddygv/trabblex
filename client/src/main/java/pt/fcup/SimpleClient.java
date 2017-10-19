package pt.fcup;

import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

// client imports
import org.glassfish.jersey.client.*;
import javax.ws.rs.client.*;

public class SimpleClient {

    protected final String HOST = "http://127.0.0.1:8080";
    protected final String URL = HOST + "/trabblex/clientmanager/getfromkeywords";

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
                                 .path("{keywords}")
                                 .resolveTemplate("keywords", keywords)
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
    public JSONArray fileInfo(String fileName);

    /**
    * Starts the download of a file
    * Via a TCP connection
    * @return a json of the specific seeders
    **/
    public boolean downloadFile(String name)
    {
        // Call client manager
        // CM answers info from chunk_owners
        // Connect via TCP to the seeder

    }

    /**
    * Get info from all local files
    * @return file info in a json array
    **/
    public JSONArray listFiles(String hash);

    /**
    * Creates a seeder for the designated file
    * @return all the seeders
    **/
    public JSONArray createSeed(String fileName)
    {
        // call client Manager
    }

    public boolean informClientUnjoinable(String ip, int port)
    {
        // Inform client manager that a client is not joinable
        // eg disconnected from the network
    }

    public static void main(String[] args)
    {
        SimpleClient sc = new SimpleClient();
        sc.getResource();

    }

}
