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
public class ClientManagerResource implements IClient{

    public ClientManagerResource()
    {

    }

    /**
    * Upon getting a list of keywords from the client,
    * returns all the seeders that match that list
    * @return the seeders that match the list, in json format
    **/
    @GET
    @Path("getfromkeywords/{kwds}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt(@PathParam("kwds") String keywords) {
        String res = null;
        try{
            String sampleQuery =  "SELECT pg.tablename FROM pg_catalog.pg_tables pg WHERE pg.tablename = 'seeders';";

            res = DBManager_singleton.getInstance()
                            .queryTable(sampleQuery).toString();
            System.out.println("Executed query, result = " + res);
        }
        catch(Exception e)
        {

        }
        return res;
    }

    /*public String[] getSeedersfromdB()
    {

    }*/

    public void list_seeders()
    {
        // get seeders from db
        // return seeders to client
    }

    public void create_seed()
    {

    }
}
