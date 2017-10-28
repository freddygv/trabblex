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

class ChunkManager
{
	public ChunkManager(JSONArray remoteChunkOwners)
	{
		nbChunksAvailable = 0;
        Hashtable chunks<String, Chunk> = new Hashtable<String, Chunk>();

        for (int i = 0 ; i < remoteChunkOwners.length(); i++) 
        {
            JSONObject obj = remoteChunkOwners.getJSONObject(i);
            String hash = obj.getString("chunk_hash");
            if(!chunks.containsKey(hash))
            {
                chunks.put(hash, new Chunk(obj));
            }
            else
            {
            	// update chunk info to add new source
            	Chunk ch = chunks.get(hash);
            	ch.addSource(obj);
                chunks.put(hash, ch);
            }
            nbChunksAvailable ++;
        }

        return chunks;
	}

	public Chunk getRarestChunk()
	{
		Chunk ch = null;
		int minowners = -1;

		for(Chunk it : chunks)
		{
			if(minowners == -1 || it.getNumberOfSources < minowners)
			{
				if(it.isDownloaded == false)
				{
					ch = it;
					minowners = it.getNumberOfSources;
				}
			}
		}

		return ch;
	}

	public markChunkDownloaded(String hash)
	{
		chunks.get(hash).markDownloaded();
	}
}