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

    public static void main(String[] args)
    {
        SimpleClient sc = new SimpleClient();
        sc.getResource();

    }

}
