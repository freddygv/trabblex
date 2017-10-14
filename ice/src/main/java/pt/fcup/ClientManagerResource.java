package pt.fcup;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.PathParam;

/**
* Gives the client a bunch of functionalities
**/
@Path("clientmanager")
public class ClientManagerResource  implements IClient{

    /**
    * Upon getting a list of keywords from the client,
    * returns all the seeders that match that list
    **/
    @GET
    @Path("getfromkeywords/{kwds}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt(@PathParam("kwds") String keywords) {
        return keywords;
    }

    /*public String[] getSeedersfromdB()
    {

    }*/

    public void list_seeders()
    {

    }

    public void create_seed()
    {

    }
}
