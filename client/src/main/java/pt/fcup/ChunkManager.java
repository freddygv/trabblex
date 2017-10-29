package pt.fcup;

import java.util.Properties;
import java.util.ArrayList;
import java.util.Hashtable;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;


/*import org.glassfish.jersey.client.*;*/
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.*;

class ChunkManager
{
    Hashtable<String, Chunk> chunks = new Hashtable<String, Chunk>();
    int nbChunksNotDownloaded = 0;

	public ChunkManager(JSONArray remoteChunkOwners)
	{

        for (int i = 0 ; i < remoteChunkOwners.length(); i++) 
        {
            JSONObject obj = remoteChunkOwners.getJSONObject(i);

        	System.out.println("Chunk manager saving chunk " + obj.getString("chunk_id"));

            String hash = obj.getString("chunk_hash");
            if(!chunks.containsKey(hash))
            {
                chunks.put(hash, new Chunk(obj));
                nbChunksNotDownloaded ++;
            }
            else
            {
            	// update chunk info to add new source
            	Chunk ch = chunks.get(hash);
            	ch.addSource(obj);
                chunks.put(hash, ch);
            }
        }
	}

	public Chunk getRarestChunk()
	{
		Chunk ch = null;
		int minowners = -1;

		Iterator iter = chunks.entrySet().iterator();
		while (iter.hasNext())
		{
			Map.Entry pair = (Map.Entry)iter.next();
			Chunk value = (Chunk)pair.getValue();
			//System.out.println("Evaluating chunk #" + value.chunkNumber);
			if(minowners == -1 || value.getNumberOfSources() < minowners)
			{
				if(value.isDownloaded == false)
				{
					ch = value;
					minowners = ch.getNumberOfSources();
					//System.out.println("Next chunk to download is #" + ch.chunkNumber);
				}
			}

        	//iter.remove(); // avoids a ConcurrentModificationException
		}
    	

		return ch;
	}

	public int numberOfChunksNotDownloaded()
	{
		return nbChunksNotDownloaded;
	}

	public void markChunkDownloaded(int n)
	{
		Chunk ch = null;

		Iterator iter = chunks.entrySet().iterator();
		while (iter.hasNext())
		{
			Map.Entry pair = (Map.Entry)iter.next();
			Chunk value = (Chunk)pair.getValue();
			if(value.chunkNumber == n)
			{
				value.markDownloaded();
				nbChunksNotDownloaded --;
			}

        	//iter.remove(); // avoids a ConcurrentModificationException
		}
	}

	public int getNbChunksAvailable()
	{
		return chunks.size();
	}
}