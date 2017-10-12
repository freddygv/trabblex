package pt.fcup;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

class SimpleClient implements IClient {

    /**
    * Method handling HTTP GET requests. The returned object will be sent
    * to the client as "text/plain" media type.
    *
    * @return String that will be returned as a text/plain response.
    */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
       return "Got it!";
    }

    public void list_seeders()
    {

    }

    public void create_seed()
    {

    }
}
