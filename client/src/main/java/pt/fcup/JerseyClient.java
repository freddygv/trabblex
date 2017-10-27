package pt.fcup;

import java.util.Properties;
import java.util.ArrayList;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;
import org.json.JSONArray;


import java.util.Scanner;


public class JerseyClient {

	protected String HOST;
	protected String URL;
	private Client client;

	public JerseyClient(String HOST, String URL)
	{
		client = ClientBuilder.newClient();
		this.HOST = HOST;
		this.URL = HOST + URL;
	}

	public String query(String path, String param)
   	{
        String result = null;

        try
        {
            if(param != null)
            {
                path = path + "/";
            }

            // Query database
            result = client.target(URL)
                                 .path(path + param)
                                 .request(MediaType.TEXT_PLAIN)
                                 .get(String.class);
        }
        catch(javax.ws.rs.ProcessingException e)
        {
            System.err.println("Cannot connect to server " + HOST);

        }
        catch(Exception e )
        {
            e.printStackTrace();
        }  

        return result;
    }

}