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
		ow.hash = obj.getString("chunk_hash");

		owners.add(ow);

		// save chunk number
		chunkNumber = obj.getInt("chunk_id");

		System.out.println("Adding source, now has " + owners.size() + " sources");
	}

	/*
		If a source doesn't have the chunk we asked for,
		or the hash is bad, then remove that source
	*/
	public void removeOwner(String ip, int port, String hash)
	{
		for(int i = 0; i < owners.size(); i++)
		{
			if(owners.get(i).ip == ip
				&& owners.get(i).hash == hash
				&& owners.get(i).port == port){
				System.out.println("Removing bad owner " + owners.get(i).ip + ":" + owners.get(i).port);
				owners.remove(i);
			}
		}
	}


	public Owner getSource()
	{
		// TODO get the first owner that isn't a seeder preferably
		if(owners.size() == 0)
		{
			System.out.println("No source found!");
			return null;
		}
		System.out.println("Providing source " + owners.get(0).ip + "/" + ":" + owners.get(0).port 
			+ "(" + owners.size() + " sources available)");
		return owners.get(0);
	}

	public int getNumberOfSources()
	{
		return owners.size();
	}

	public void markDownloaded()
	{
		isDownloaded = true;
	}
}