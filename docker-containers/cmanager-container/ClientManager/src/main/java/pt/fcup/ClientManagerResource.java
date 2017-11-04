package pt.fcup;
import org.json.JSONArray;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.PathParam;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import java.io.IOException;
import pt.fcup.generated.*;
import java.sql.SQLException;

import org.json.JSONObject;
import org.json.JSONArray;

import javax.ws.rs.QueryParam;

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
        boolean cluster = true;

        try
        {
            seedboxAddress = InetAddress.getByName("seedbox").getHostAddress();

        }
        catch (UnknownHostException e) {
            cluster = false;
            seedboxAddress = "localhost";

        }

        host = String.format("%s -p 8082", seedboxAddress);

        try {
            if(cluster) {
                db = new DBManager(cluster);

            } else {
                db = new DBManager();

            }

        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();

        }

    }

    private JSONArray runQuery( String query )
    {
        JSONArray res = null;

        try{
            //System.out.println("Executing query " + query);
            res = db.queryTable(query);
            //System.out.println("Result = " + res.toString());

        }
        catch(Exception e)
        {
            System.err.println("Connection to DB Failed (" + e + ")");
            System.err.println("While running query \n>>>>>>>>>>>>>>>>\n" + query);
            System.err.println(">>>>>>>>>>>>>>>>");

        }

        return res;
    }

    private void runUpdate( String query )
    {

        try{
            //System.out.println("Executing query " + query);
            db.singleUpdate(query);
            //System.out.println("Result = " + res.toString());

        }
        catch(Exception e)
        {
            System.err.println("Connection to DB Failed (" + e + ")");
            System.err.println("While running query \n>>>>>>>>>>>>>>>>\n" + query);
            System.err.println(">>>>>>>>>>>>>>>>");

        }
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
    @GET
    @Path("listfromkeyword/{keyword}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getSeedersfromKeyword(@PathParam("keyword") String keyword)
    {
        JSONArray result = null;

        String query =  "SELECT * FROM videos WHERE file_name LIKE '%" + keyword +"%';";

        return runQuery(query).toString();
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
     * Registers a client as chunk seeder
     */
    @Path("registerclientseeder")
    @Produces(MediaType.TEXT_PLAIN)
    public String registerClientSeeder(@QueryParam("file_hash") String file_hash,
                                       @QueryParam("chunk_hash") String chunk_hash,
                                       @QueryParam("chunk_id") String chunk_id,
                                       @QueryParam("ip") String ip,
                                       @QueryParam("port") String port)
    {

        String query =  "INSERT INTO chunk_owners(file_hash,chunk_hash,chunk_id,owner_ip,owner_port,is_seeder)"
                +" VALUES('" + file_hash + "',"
                + "'" + chunk_hash + "',"
                + "'" + Integer.parseInt(chunk_id) + "',"
                + "'" + ip + "',"
                + "'" + Integer.parseInt(port) + "',"
                + "'f'"
                + ");";

        runUpdate(query);

        return null;
    }

    /**
     * De-registers the client as chunk seeder
     */
    /**
     * Registers a client as chunk seeder
     */
    @Path("unregisterclientseeder")
    @Produces(MediaType.TEXT_PLAIN)
    public String unRegisterClientSeeder(@QueryParam("file_hash") String file_hash,
                                         @QueryParam("chunk_hash") String chunk_hash,
                                         @QueryParam("chunk_id") String chunk_id,
                                         @QueryParam("ip") String ip,
                                         @QueryParam("port") String port)
    {

        String query =  "DELETE FROM chunk_owners WHERE "
                +"file_hash='" + file_hash + "' AND "
                + "chunk_hash='" + chunk_hash + "' AND "
                + "chunk_id='" + Integer.parseInt(chunk_id) + "' AND "
                + "owner_ip='" + ip + "' AND "
                + "owner_port='" + Integer.parseInt(port) + "'"
                + ";";

        runUpdate(query);

        return null;
    }


}
