import java.util.Properties;
import java.util.ArrayList;

package pt.fcup;




/*
	Simple chunk structure info used by the chunk manager 
*/
public class Chunk
{

	public hash = 0;
	public numberOfSources = 0;
	public isDownloaded = 0;
	public chunkNumber;
	public List<Owner> owners = new List<Owner>();

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
		ow.ip = obj.getString("seeder_ip");
		ow.port = obj.getString("port");
		ow.protocol = obj.getString("protocol");
		ow.is_seeder = obj.getString("is_seeder");

		owners.add(ow);

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