package pt.fcup;

import org.json.JSONArray;

import java.util.ArrayList;

/**
* Interface used by
- the main portal and client
- the main portal and client manager
to communicate
*/


public interface IClient
{
    /**
    * Get a list of all the seeders:
    * file_name, file_size, video_size_x, video_size_y, bitrate
    * does NOT include ip adress and port for security purposes
    * (the chunk_owners database contains info about the seeders AND peers)
    * @return all the seeders
    **/
    public JSONArray listSeeders();

    /**
    * Searches all the seeders for the keywords
    * @return a json of the specific seeders
    **/
    public JSONArray getSeedersfromKeyword(ArrayList<String> keywords);


    /**
    * Creates a seeder for the designated file
    * @return all the seeders
    **/
    public JSONArray createSeed(String fileName);

    /**
    * Calls the IceServer to inform that the client disconnected
    * @return success
    **/
    public boolean disconnectClient(String ip, int port);

}
