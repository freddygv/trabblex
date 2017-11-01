package pt.fcup;

import java.util.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;
import org.json.JSONArray;


import java.util.Scanner;

/*
    Used to query the client manager
*/

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

    public String query(String path)
    {
        return query(path, null, null);
    }

    public String query(String path, String param)
    {
        return query(path, param, null);
    }

	public String query(String path, String param, Map<String,String> queryParams)
   	{
        String result = null;

        try
        {
            if(param != null)
            {
                path = path + "/";
            }
            else{
                param = "";
            }

            // Query client manager
            WebTarget resourceWebTarget = client.target(URL).path(path + param);

            // add query params
            if(queryParams != null)
            {
                Iterator it = queryParams.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    resourceWebTarget = resourceWebTarget.queryParam(pair.getKey().toString(), pair.getValue());
                }
            }
                
            result = resourceWebTarget.request(MediaType.TEXT_PLAIN).get(String.class);
        }
        catch(javax.ws.rs.ProcessingException e)
        {
            System.err.println("Cannot connect to server " + HOST);

        }
        catch(javax.ws.rs.NotFoundException e)
        {
            // debug
            //System.err.println("Resource not found: " + URL);

        }
        catch(javax.ws.rs.InternalServerErrorException e)
        {
            System.err.println("Could not connect to database");
        }
        catch(Exception e )
        {
            e.printStackTrace();
        }  

        return result;
    }

}