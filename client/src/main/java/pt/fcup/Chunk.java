package pt.fcup;

import java.util.Properties;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;


/*
	Simple chunk structure info used by the chunk manager 
*/
public class Chunk
{
	public int numberOfSources = 0;
	public boolean isDownloaded = false;
	public int chunkNumber;
	public ArrayList<Owner> owners = new ArrayList<Owner>();

	public Chunk(JSONObject obj)
	{
		addSource(obj);
	}

	/*
		Update local info about owners
	*/
	public void addSource(JSONObject obj)
	{

		Owner ow = new Owner();
		ow.ip = obj.getString("owner_ip");
		ow.port = obj.getInt("owner_port");
		// quick and dirty fix :(
		ow.protocol = "TCP";
		// database returns t or f for true / false
		ow.is_seeder = obj.getString("is_seeder").equalsIgnoreCase("t");

		owners.add(ow);

		// save chunk number
		chunkNumber = obj.getInt("chunk_id");

		numberOfSources ++;
	}

	/*
		TODO eliminate bad sources
	*/
	public Owner getSource()
	{
		// TODO get the first owner that isn't a seeder preferably
		return owners.get(0);
	}

	public int getNumberOfSources()
	{
		return numberOfSources;
	}

	public void markDownloaded()
	{
		isDownloaded = true;
	}
}