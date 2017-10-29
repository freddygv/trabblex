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
		ow.hash = obj.getString("hash");

		owners.add(ow);

		// save chunk number
		chunkNumber = obj.getInt("chunk_id");

		numberOfSources ++;
	}

	/*
		If a source doesn't have the chunk we asked for,
		or the hash is bad, then remove that source
	*/
	public void removeOwner(String ip, int port, String hash)
	{
		for(int i = 0; i < owners.length; i++)
		{
			if(owners.get(i).ip == ip
				&& owners.get(i).hash == hash
				&& owners.get(i).port == port){
				owners.remove(i);
				break;
			}
		}
	}


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