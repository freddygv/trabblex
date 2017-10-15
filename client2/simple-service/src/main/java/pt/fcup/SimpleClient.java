package pt.fcup;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

// client imports
import org.glassfish.jersey.client.*;
import javax.ws.rs.client.*;

public class SimpleClient {

    protected final String HOST = "localhost:8080";
    protected final String URL = HOST + "/trabblex/clientmanager/getfromkeywords";

    private ClientConfig clientConfig;
    private Client client;

    public SimpleClient()
    {
        clientConfig = new ClientConfig();
        client = ClientBuilder.newClient(clientConfig);
    }

    public void getSeeders()
    {

    }

    public void getResource()
    {
        String keywords = "test, test2, test3";
        String webTarget = client.target(URL)
                             .path("{keywords}")
                             .resolveTemplate("keywords", keywords)
                             .request(MediaType.TEXT_PLAIN_TYPE)
                             .get(String.class);

        System.out.println(webTarget);
    }

    public static void main(String[] args)
    {
        SimpleClient sc = new SimpleClient();
        sc.getResource();

    }
}
