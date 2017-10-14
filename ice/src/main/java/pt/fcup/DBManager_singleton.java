package pt.fcup;

import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import java.sql.*;
import java.io.FileInputStream;
import java.util.Properties;

/**
*   Database manager implemented as singleton
*   Ensures only one database manager is present in the system
**/
public class DBManager_singleton extends DBManager{

    private static DBManager_singleton instance = null;

    private DBManager_singleton() throws IOException, ClassNotFoundException{
        super();
    }

    /** Singleton access point */
	public static DBManager_singleton getInstance() throws IOException, ClassNotFoundException
	{
		if (instance == null)
		{
            instance = new DBManager_singleton();
		}
		return instance;
	}

}
