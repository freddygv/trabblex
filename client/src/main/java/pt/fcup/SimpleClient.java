package pt.fcup;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


public class SimpleClient {

    public static void main()
    {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(MyClientResponseFilter.class);
        clientConfig.register(new AnotherClientFilter());

        Client client = ClientBuilder.newClient(clientConfig);
        client.register(ThirdClientFilter.class);

        WebTarget webTarget = client.target("http://example.com/rest");
        webTarget.register(FilterForExampleCom.class);
        WebTarget resourceWebTarget = webTarget.path("resource");

        Invocation.Builder invocationBuilder =
                resourceWebTarget.request(MediaType.TEXT_PLAIN_TYPE);

        Response response = invocationBuilder.get();
        System.out.println(response.getStatus());
        System.out.println(response.readEntity(String.class));
    }
}
