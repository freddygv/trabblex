package pt.fcup;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

// client imports
import org.glassfish.jersey.client.*;
import javax.ws.rs.client.*;

public class SimpleClient {

    private ClientConfig clientConfig;
    private Client client;
    private WebTarget webTarget;
    private WebTarget resourceWebTarget;

    public SimpleClient()
    {
        clientConfig = new ClientConfig();
        //clientConfig.register(MyClientResponseFilter.class);
        //clientConfig.register(new AnotherClientFilter());

        client = ClientBuilder.newClient(clientConfig);
        //client.register(ThirdClientFilter.class);

        webTarget = client.target("http://localhost:8080/myapp/");
        //webTarget.register(FilterForExampleCom.class);
        resourceWebTarget = webTarget.path("clientmanager");


        //invocationBuilder.header("some-header", "true");

    }

    public void getResource()
    {
        Invocation.Builder invocationBuilder =
                resourceWebTarget.request(MediaType.TEXT_PLAIN_TYPE);

        Response response = invocationBuilder.get();
        System.out.println(response.getStatus());
        System.out.println(response.readEntity(String.class));
    }

    public static void main(String[] args)
    {
        SimpleClient sc = new SimpleClient();
        sc.getResource();

    }
}
