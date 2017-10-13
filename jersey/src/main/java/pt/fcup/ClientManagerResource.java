package pt.fcup;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path("clientmanager")
public class ClientManagerResource  implements IClient{
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
