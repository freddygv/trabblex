package pt.fcup;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.PathParam;
import java.util.ArrayList;

/**
* Gives the client a bunch of functionalities
* Used as a resource by the client manager
**/
@Path("clientmanager")
public class ClientManagerResource implements IClient{

    public ClientManagerResource()
    {

    }

    /**
    * TODO: replace query by call to seeder manager
    * Upon getting a list of keywords from the client,
    * returns all the seeders that match that list
    * @return the seeders that match the list, in json format
    **/
    @GET
    @Path("getfromkeywords/{kwds}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getSeedersfromKeyword(@PathParam("kwds") String keywords)
    {
        String res = null;

        try{
            // get unique database manager
            DBManager_singleton db = DBManager_singleton.getInstance();
            // execute dummy query
            String sampleQuery =  "SELECT file_hash, chunk_hash, owner_ip, is_active FROM chunk_owners " +
                    "WHERE file_hash = 'file-hash-1'";

            res = db.queryTable(sampleQuery).toString();
            System.out.println("Executed query, result = " + res);

        }
        catch(Exception e)
        {
            System.err.println("Connection to DB Failed.");
        }
        return res;

    }


    // see IClient interface for spec
    public ArrayList<HashMap<String, String>> listSeeders()
    {
        return null;

    }

    // see IClient interface for spec
    public HashMap<String, String> createSeed(String fileName)
    {
            return null;

    }

    // see IClient interface for spec
    public HashMap<String, String> getSeeder(String fileHash)
    {
            return null;

    }
}
