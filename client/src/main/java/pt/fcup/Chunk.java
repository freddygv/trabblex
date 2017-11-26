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
	public void addSource(JSONObject obj) {

		Owner ow = new Owner();
		ow.ip = obj.getString("owner_ip");
		ow.port = obj.getInt("owner_port");
		// quick and dirty fix :(
		ow.protocol = "TCP";
		// database returns t or f for true / false
		ow.is_seeder = obj.getString("is_seeder").equalsIgnoreCase("t");
		ow.hash = obj.getString("chunk_hash");

		if(owners.contains(ow))
			System.out.println("Owner already known !");

		owners.add(ow);

		// save chunk number
		chunkNumber = obj.getInt("chunk_id");

		// debug
		//System.out.println("Adding source, now has " + owners.size() + " sources");
	}

	/*
		If a source doesn't have the chunk we asked for,
		or the hash is bad, then remove that source
	*/
	public void removeOwner(String ip, int port, String hash) {
		for(int i = 0; i < owners.size(); i++) {
			if(owners.get(i).ip == ip
				&& owners.get(i).hash == hash
				&& owners.get(i).port == port) {
				// debug
				//System.out.println("Removing bad owner " + owners.get(i).ip + ":" + owners.get(i).port);
				owners.remove(i);
			}
		}
	}


	public Owner getSource() {
		int sourceFromSeeder = -1;

		// Get the first owner that isn't a seeder preferably
		if(owners.size() == 0) {
			System.out.println("No source found!");
			return null;

		}

		for(int i = 0; i < owners.size(); i++) {
			if(owners.get(i).is_seeder == false) {
				System.out.println("Got source from client for chunk " + chunkNumber);
				return owners.get(i);

			} else {
			    sourceFromSeeder = i;

			}
		}

		System.out.println("Got source from seeder for chunk " + chunkNumber);
		return owners.get(sourceFromSeeder);

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