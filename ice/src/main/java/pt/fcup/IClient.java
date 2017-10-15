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
    public ArrayList<Seeder> listSeeders();

    /**
    * Creates a seeder for the designated file
    * @return all the seeders
    **/
    public Seeder createSeed(String fileName);

    /**
    * Creates a seeder for the designated file
    * @return all the seeders
    **/
    public Seeder getSeeder(String fileHash);
}
