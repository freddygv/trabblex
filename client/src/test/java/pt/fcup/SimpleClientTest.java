package pt.fcup;

import java.util.Properties;
import java.util.ArrayList;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;


/*import org.glassfish.jersey.client.*;*/
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Scanner;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



public class SimpleClientTest {
	private SimpleClient simpleClient;

	@BeforeEach
	void setIp() throws Exception{
		simpleClient = new SimpleClient();
	}


}