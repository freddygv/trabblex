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
    ArrayList<String> chunksNotYetDownloaded = new ArrayList<String>();
    int nbChunksNotDownloaded = 0;

    // TODO calculate unique number of chunks (based on numbers)
	public ChunkManager(JSONArray remoteChunkOwners)
	{
		addChunkOwners(remoteChunkOwners);
	}

	public void addChunkOwners(JSONArray remoteChunkOwners) {
		System.out.println("Saving remote chunks..");

        for (int i = 0 ; i < remoteChunkOwners.length(); i++) {
            JSONObject obj = remoteChunkOwners.getJSONObject(i);

            // debug
        	System.out.print("Chunk manager saving chunk " + obj.getString("chunk_id")
        		+ " (" + obj.getString("owner_ip") + ":" + obj.getString("owner_port") + ")...");

            String hash = obj.getString("chunk_hash");
            if (!chunks.containsKey(hash)) {
            	// debug
            	//System.out.println("New chunk !");
                chunks.put(hash, new Chunk(obj));

                // Check out if chunk number is already in list
                if (!chunksNotYetDownloaded.contains(obj.getString("chunk_id"))) {
                	nbChunksNotDownloaded ++;
                	chunksNotYetDownloaded.add(obj.getString("chunk_id"));
                }

            } else {
            	// debug
            	System.out.println("Already available chunk, new source !");
            	// update chunk info to add new source
            	chunks.get(hash).addSource(obj);
            }
        }
	}

	public Chunk getRarestChunk()
	{
		Chunk ch = null;
		int minowners = -1;

		Iterator iter = chunks.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry pair = (Map.Entry)iter.next();
			Chunk value = (Chunk)pair.getValue();

			// quick and dirty fix: some chunks will be bad, eg bad file hash
			// if they don't have any sources left, ignore them
			if ((minowners == -1 || value.getNumberOfSources() < minowners)
				&& value.getNumberOfSources() > 0) {

			    if (value.isDownloaded == false) {
					ch = value;
					minowners = ch.getNumberOfSources();
					
				}
			}

        	//iter.remove(); // avoids a ConcurrentModificationException
		}

		if (ch == null) {
		//	System.out.println("Couldn't get rarest chunk!");
			return null;
		}

		// debug
		System.out.println("Rarest chunk is number "+ ch.chunkNumber);
    	

		return ch;
	}

	public Chunk getChunk(int n) {
		Iterator iter = chunks.entrySet().iterator();

		while (iter.hasNext()) {

		    Map.Entry pair = (Map.Entry)iter.next();
			Chunk value = (Chunk)pair.getValue();

			if (value.chunkNumber == n) {
				return value;

			}
		}
		return null;
	}

	public int numberOfChunksNotDownloaded() {
		return nbChunksNotDownloaded;
	}

	public void markChunkDownloaded(int n) {
		// mark all chunks number n as downloaded
		Chunk ch = null;

		Iterator iter = chunks.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry pair = (Map.Entry)iter.next();
			Chunk value = (Chunk)pair.getValue();

			if (value.chunkNumber == n) {
				value.markDownloaded();

			}
		}

		nbChunksNotDownloaded --;
	}

	public int getNbChunksAvailable() {
		return chunks.size();
	}
}