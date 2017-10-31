package pt.fcup;
import org.json.JSONArray;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.PathParam;
import java.net.InetAddress;
import java.util.ArrayList;

import java.io.IOException;
import pt.fcup.generated.*;
import java.sql.SQLException;

import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Gives the client a bunch of functionalities
 * Used as a resource by the client manager
 **/
@Path("clientmanager")
public class ClientManagerResource{

    DBManager db = null;
    private final int MAX_RETRIES = 6;
    private int numberOfChunks;

    private String seedboxAddress;
    private String host;

    public ClientManagerResource()
    {
        try
        {
            db = new DBManager(true);
            seedboxAddress = InetAddress.getByName("seedbox").getHostAddress();
            host = String.format("%s -p 8082", seedboxAddress);
        }
        catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    private JSONArray runQuery( String query )
    {
        JSONArray res = null;

        try{
            System.out.println("Executing query " + query);
            res = db.queryTable(query);
            System.out.println("Result = " + res.toString());

        }
        catch(Exception e)
        {
            System.err.println("Connection to DB Failed (" + e + ")");

        }

        return res;
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

        String query =  "SELECT file_hash, chunk_hash, owner_ip, is_active FROM chunk_owners " +
                "WHERE file_hash = 'file-hash-1';";

        return runQuery(query).toString();

    }

    /**
     * Get a list of all the seeders:
     * file_name, file_size, video_size_x, video_size_y, bitrate
     * does NOT include ip adress and port for security purposes
     * (the chunk_owners database contains info about the seeders AND peers)
     * @return all the seeders
     **/
    @GET
    @Path("list")
    @Produces(MediaType.TEXT_PLAIN)
    public String listSeeders()
    {
        JSONArray result = null;

        String query =  "SELECT * FROM videos;";

        return runQuery(query).toString();

    }

    /**
     * Upon getting the name of a file from the client,
     * returns all the seeders and clients that have that files
     * @return the seeders and clients that match the file, in json format
     **/
    @GET
    @Path("getowners/{hash}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getChunkOwners(@PathParam("hash") String filehash)
    {

        String query =  "SELECT * FROM chunk_owners WHERE file_hash='"
                + filehash
                + "';";

        return runQuery(query).toString();

    }

    /**
     * Searches all the seeders for the keywords
     * @return a json of the specific seeders
     **/
    public JSONArray getSeedersfromKeyword(ArrayList<String> keywords)
    {
        return new JSONArray();
    }


    /**
     * Creates a seeder for the designated file
     * @return seeder info
     **/
    @GET
    @Path("createseeder/{filename}")
    @Produces(MediaType.TEXT_PLAIN)
    public String createSeeder(@PathParam("filename") String filename)
    {
        boolean reqResult = false;

        for (int retries = 0; retries < MAX_RETRIES; retries++) {
            try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize()) {
                RequestableIPrx create = RequestableIPrx.checkedCast(communicator.stringToProxy("SeederRequest:default -h " + host));
                numberOfChunks = create.requestSeeder(filename);
                System.out.println("Number of chunks: " + numberOfChunks);
                reqResult = true;
            }

            if (reqResult) {
                break;
            }
        }

        // TODO: Make a real JSONArray, if needed
        if (reqResult)
            return "success";
        return null;
    }

    /**
     * Calls the IceServer to inform that the client disconnected
     * @return success
     **/
    public boolean disconnectClient(String ip, int port)
    {
        boolean regResult = false;

        for (int retries = 0; retries < MAX_RETRIES; retries++) {
            try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize()) {
                RequestableIPrx disconnect = RequestableIPrx.checkedCast(communicator.stringToProxy("SeederRequest:default -h " + host));
                regResult = disconnect.disconnectClient();
                System.out.println("Disconnection result: " + regResult);
                regResult = true; // TODO: Remove when disconnect is implemented
            }

            if (regResult) {
                break;
            }
        }

        return regResult;
    }

}
