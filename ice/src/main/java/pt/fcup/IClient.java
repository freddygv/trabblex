package pt.fcup;

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
    * Get a list of all the seeders
    * @return all the seeders
    **/
    public ArrayList<HashMap<String, String>> listSeeders();

    /**
    * Creates a seeder for the designated file
    * @return all the seeders
    **/
    public HashMap<String, String> createSeed(String fileName);

    /**
    * Creates a seeder for the designated file
    * @return all the seeders
    **/
    public HashMap<String, String> getSeeder(String fileHash);
}
