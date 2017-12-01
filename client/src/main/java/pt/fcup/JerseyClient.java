package pt.fcup;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;
import org.json.JSONArray;


import java.util.Scanner;

/**
 * Makes requests to the ClientManager
 */

public class JerseyClient {

    protected String host;
    protected String url;
    private Client client;

    public JerseyClient(String host, String url) {
        client = ClientBuilder.newClient();
        this.host = host;
        this.url = host + url;

    }

    public String query(String path) {
        return query(path, null, null);

    }

    public String query(String path, String param) {
        return query(path, param, null);

    }

    public String query(String path, String param, Map<String,String> queryParams) {
        String result = null;

        if (param != null) {
            path = path + "/";

        } else {
            param = "";

        }

        try {
            WebTarget resourceWebTarget = client.target(url).path(path + param);

            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                resourceWebTarget = resourceWebTarget.queryParam(entry.getKey(),
                                                                 entry.getValue());

            }

            result = resourceWebTarget.request(MediaType.TEXT_PLAIN).get(String.class);

        } catch(ProcessingException e) {
            System.err.println("Cannot connect to server " + host);
            e.printStackTrace();

        } catch(NotFoundException e) {
            System.err.println("Resource not found: " + url);
            e.printStackTrace();

        } catch(InternalServerErrorException e) {
            System.err.println("Could not connect to database");
            e.printStackTrace();

        }

        return result;
    }

}