package pt.fcup;
import org.json.JSONArray;
import pt.fcup.generated.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Used as a resource by the ClientManager to expose functionality to clients
 **/
@Path("clientmanager")
public class ClientManagerResource {

    private final int MAX_RETRIES = 6;

    private String host;
    private boolean cluster = false;

    private DBManager db = null;

    public ClientManagerResource() {
        setSeedboxHostAddr();
        createDBManager();

    }

    /**
     * Sets address for the Seedbox to enable seeder requests from clients.
     * If seedbox address is not resolved by DNS, then the server is running locally.
     */
    private void setSeedboxHostAddr() {
        String seedboxAddress = "localhost";

        try {
            seedboxAddress = InetAddress.getByName("seedbox").getHostAddress();
            cluster = true;

        } catch (UnknownHostException e) {
            // Do nothing, just checking if we are running locally

        }

        host = String.format("%s -p 8082", seedboxAddress);

    }

    private void createDBManager() {
        try {
            db = (cluster) ? new DBManager(cluster)
                           : new DBManager();


        } catch (ClassNotFoundException | IOException e) {
            // TODO: Handle failure
            e.printStackTrace();

        }
    }

    private JSONArray runQuery( String query ) {
        JSONArray res = null;

        try{
            res = db.queryTable(query);

        } catch(Exception e) {
            queryError(query);

        }

        return res;
    }

    private void runUpdate( String query ) {
        try{
            db.singleUpdate(query);

        } catch(SQLException e) {
            queryError(query);

        }
    }

    private void queryError(String query) {
        System.err.println("DB Update/Query failed while running query:");
        System.err.println(query);

    }

    /**
     * Get a list of files on the server:
     * file_name, file_size, video_size_x, video_size_y, bitrate
     * @return all the seeders
     **/
    @GET
    @Path("list")
    @Produces(MediaType.TEXT_PLAIN)
    public String listSeeders() {

        String query =  "SELECT * FROM videos;";
        return runQuery(query).toString();

    }

    /**
     * Returns the neighborhood of peers for a given file
     * @return the seeders and clients that match the file, in json format
     **/
    @GET
    @Path("getowners/{hash}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getChunkOwners(@PathParam("hash") String filehash) {

        String query = String.format("SELECT * FROM chunk_owners WHERE file_hash='%s';", filehash);
        return runQuery(query).toString();

    }

    /**
     * Requests a seeder for the designated file
     * @return seeder info
     **/
    @GET
    @Path("createseeder/{filename}")
    @Produces(MediaType.TEXT_PLAIN)
    public String createSeeder(@PathParam("filename") String filename) {
        boolean creationSuccess = false;

        // Retry policy for RPC communications
        int retries = 0;
        while (true) {
            try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize()) {
                String iceHost = "SeederRequest:default -h " + host;
                RequestableIPrx create = RequestableIPrx.checkedCast(communicator.stringToProxy(iceHost));
                creationSuccess = create.requestSeeder(filename);
                break;

            } catch (Exception e) {
                if (++retries == MAX_RETRIES) { return "Error: Try again later"; }

            }

            retries++;
        }

        // TODO: Deregister video on failure, means there's no server nor peer sources
        return (creationSuccess) ? "Creation success."
                                 : null;


    }

    /**
     * When a client has a chunk, it registers as a seeder
     */
    @Path("registerclientseeder")
    @Produces(MediaType.TEXT_PLAIN)
    public String registerClientSeeder(@QueryParam("file_hash") String file_hash,
                                       @QueryParam("chunk_hash") String chunk_hash,
                                       @QueryParam("chunk_id") String chunk_id,
                                       @QueryParam("ip") String ip,
                                       @QueryParam("port") String port) {

        String query = "INSERT INTO chunk_owners(file_hash, chunk_hash, chunk_id, "
                                              + "owner_ip, owner_port, is_seeder)"

                     + "VALUES('%s', '%s', %s, '%s', %s, 'f');".format(file_hash,
                                                                       chunk_hash,
                                                                       chunk_id,
                                                                       ip,
                                                                       port);

        runUpdate(query);
        return null;
    }

    /**
     * De-registers the client from the list of seeders
     */
    @Path("unregisterclientseeder")
    @Produces(MediaType.TEXT_PLAIN)
    public String deregisterClientSeeder(@QueryParam("file_hash") String file_hash,
                                         @QueryParam("chunk_hash") String chunk_hash,
                                         @QueryParam("chunk_id") String chunk_id,
                                         @QueryParam("ip") String ip,
                                         @QueryParam("port") String port) {

        String query =  String.format("DELETE FROM chunk_owners "
                                    + "WHERE owner_ip='%s' AND owner_port = %s;", ip, port);
        runUpdate(query);

        return null;
    }


}
