package pt.fcup;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.*;

/**
 * Handles the list of chunks for a given file, and processes the owners for said chunks
 */
class ChunkManager {
    Map<String, Chunk> chunks = new HashMap<>();
    Set<String> chunksPending = new HashSet<>();

	public ChunkManager(JSONArray remoteChunkOwners) {
		addChunkOwners(remoteChunkOwners);

	}

	public void addChunkOwners(JSONArray remoteChunkOwners) {
        for (int i = 0 ; i < remoteChunkOwners.length(); i++) {
            JSONObject obj = remoteChunkOwners.getJSONObject(i);

            String chunkID = obj.getString("chunk_id");

            if (!chunks.containsKey(chunkID)) {
                chunks.put(chunkID, new Chunk(obj));

                if (!chunksPending.contains(chunkID)) {
                	chunksPending.add(chunkID);

                }

            } else {
                // Already have the chunk, just add the source
            	chunks.get(chunkID).addSource(obj);

            }
        }
	}

    /**
     * Loops over all remaining chunks and return the one with the fewest sources
     */
	public Chunk getRarestChunk() {
		Chunk rarest = null;
		int minowners = Integer.MAX_VALUE;

		for (String id : chunksPending) {
            Chunk currentChunk = chunks.get(id);
            int numSources = currentChunk.getNumberOfSources();

            if (currentChunk.getNumberOfSources() < minowners && numSources > 0) {
                rarest = currentChunk;
                minowners = numSources;

            }
        }

        // TODO: Verify that null value is being handled upstream
        return rarest;
	}

	public void markChunkDownloaded(int n) {
        chunksPending.remove(Integer.toString(n));

	}

    public int numberOfChunksNotDownloaded() {
        return chunksPending.size();

    }
}