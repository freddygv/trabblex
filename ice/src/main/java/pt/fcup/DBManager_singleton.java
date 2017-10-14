package pt.fcup;

import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import java.sql.*;
import java.io.FileInputStream;
import java.util.Properties;

public class DBManager_singleton extends DBManager{

    private static DBManager instance = null;

    private DBManager() throws IOException, ClassNotFoundException{
        try {
            Class.forName("org.postgresql.Driver");

        } catch (ClassNotFoundException e) {
            System.err.println("Postgres Driver not found.");
            throw e;
        }

        try {
            loadDBProperties(DB_PROPS_LOCATION);

        } catch (IOException e) {
            System.err.println("Error loading properties file.");
            throw e;

        }
    }

    /** Singleton access point */
	public static Singleton getInstance()
	{
		if (instance == null)
		{ 	instance = new DBManager();
		}
		return instance;
	}

}
