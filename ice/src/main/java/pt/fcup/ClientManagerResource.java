package pt.fcup;
import org.json.JSONArray;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.PathParam;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONArray;

/**
* Gives the client a bunch of functionalities
* Used as a resource by the client manager
**/
@Path("clientmanager")
public class ClientManagerResource{

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
            System.err.println("Connection to DB Failed (" + e + ")");

        }
        return res;

    }

    /**
    * Get a list of all the seeders:
    * file_name, file_size, video_size_x, video_size_y, bitrate
    * does NOT include ip adress and port for security purposes
    * (the chunk_owners database contains info about the seeders AND peers)
    * @return all the seeders
    **/
    public JSONArray listSeeders()
    {
        JSONArray res = null;

        try
        {
            // get unique database manager
            DBManager_singleton db = DBManager_singleton.getInstance();
            // execute dummy query
            String sampleQuery =  "SELECT * FROM seeders ";

            res = db.queryTable(sampleQuery);
            //System.out.println("Executed query, result = " + res.toString());

        }
        catch(Exception e)
        {
            System.err.println("Connection to DB Failed (" + e + ")");

        }

        return res;

    }

    /**
    * TODO: replace dummy answer by real query
    * Upon getting the name of a file from the client,
    * returns all the seeders and clients that have that files
    * @return the seeders and clients that match the file, in json format
    **/
    @GET
    @Path("getfile/{file}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getSeedersFromFile(@PathParam("file") String file)
    {

        /*try{
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
            System.err.println("Connection to DB Failed (" + e + ")");

        }*/

        // 1. Scan seeders table for file
        // 2. Extract file hash
        // 3. Return all the chunk owners concerned, sorted by rarity

        String res = "[{\"file_hash\":\"file-hash-" + file + "\",\"chunk_hash\":\"chunk-hash-1\"," +
                "\"owner_ip\":\"localhost\",\"is_active\":\"t\"}]";



        return res;

    }

    /**
    * Searches all the seeders for the keywords
    * @return a json of the specific seeders
    **/
    public JSONArray getSeedersfromKeyword(ArrayList<String> keywords) {
        return new JSONArray();
    }


    /**
    * Creates a seeder for the designated file
    * @return all the seeders
    **/
    public JSONArray createSeed(String fileName)
    {
        // Call Ice Server via RPC
            return null;
    }

    public boolean informClientUnjoinable(String ip, int port)
    {
        // call Ice server via RPC
        return false;
    }


    /**
    * Calls the IceServer to inform that the client disconnected
    * @return success
    **/
    public boolean disconnectClient(String ip, int port)
    {
            return false;
    }

}
