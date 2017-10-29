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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class FileDownloader extends Thread
{
    private String name, hashToGet;
    private JerseyClient client;

    // TODO pass the hashing algorithm through the header...
    private final String HASHING_ALGORITHM = "SHA-256";

    public FileDownloader(String name, String hash, JerseyClient client)
    {
        this.name = name;
        this.hashToGet = hash;

        // client reference passed by the parent client
        this.client = client;
    }

    /**
    * Starts the download of a file
    * Via a TCP connection
    **/
    @Override
    public void run()
    {
        downloadFile();
    }

    private boolean downloadFile()
    {

        // the owners of the chunk of the file (seeder + other clients)
        JSONArray remoteChunkOwners = null;

        // the protocol that will be used - atm, fixed
        String protocol = "TCP";

        // storing the file
        ArrayList<Byte> file = new ArrayList<Byte>();
        
        /*
            (1) Fetch all the chunk owners related to the client
        */

        if(hashToGet == null)
        {
            System.err.println("Couldn't solve hash from filename!");
            System.err.println("Error downloading file " + name);
            Thread.currentThread().interrupt();
        }

        String chunkOwners = client.query("getowners", hashToGet);

        if(chunkOwners == null)
        {
            System.err.println("Couldn't get the chunk owners of the file!");
            Thread.currentThread().interrupt();
        }

        remoteChunkOwners = new JSONArray(chunkOwners);

        // if no seeders available, request a new one
        /*
            Note: this means that if we downloaded a corrupt chunk from the seeder,
            and no other source is available,
            the client will be stuck in a loop 
            Possible solution: have a chunk owners blacklist instead of simply
            removing them
        */
        if(remoteChunkOwners.length() == 0)
        {
            String newSeeder = client.query("createseeder", name);
            if(newSeeder == null)
            {
                System.err.println("Error requesting the creation of a new seeder: " + newSeeder);
                return false;
            }
            
            // restart, only this time with all the seeders needed...
            return downloadFile();   
        }

        /*
            (2) Starting phase - download the first chunk from the first owner available
            In order to get metadata on the file
        */

        ChunkManager chm = new ChunkManager(remoteChunkOwners);

        /*
            Download the first chunk and check that it is valid
            TODO this code is redundant with the code later
            Note: what to do if zero owners available ?...
                --> create seeder
                --> would need to revisit whole algorithm, don't have time for that
        */
        Downloader firstdwl = null;
        boolean chunkIsValid = false;
        int tries= 0;
        while(chunkIsValid == false)
        {

            // debug
            System.out.println("This is try " + tries);

            JSONObject obj = remoteChunkOwners.getJSONObject(tries);

            // if we have tested all sedeers, then file unavailable :(
            if(obj == null)
            {
                System.err.println("FATAL ERROR no seeder available to download the first chunk of the file");
                return false;
            }

            int chunkNumber = obj.getInt("chunk_id");

            firstdwl = new Downloader(
                name,
                chunkNumber,
                obj.getString("owner_ip"), 
                Integer.parseInt(obj.getString("owner_port")),
                protocol,
                obj.getString("chunk_hash")
            );

            // the thread will automatically save the file locally
            firstdwl.start();

            try{
                firstdwl.join();   
                if(checkHash("downloads/" + name + "-" + chunkNumber, obj.getString("chunk_hash")) == true)
                {
                    // mark chunk as downloaded
                    chm.markChunkDownloaded(chunkNumber);
                    chunkIsValid = true;
                }
                else{
                    System.out.println("Error couldn't verify hash of chunk number "+ chunkNumber);
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                return false;
            }
            tries ++;
        }


        /*
            (4) Assess if all chunks are available 
            If no, create a seeder, and start all over again
        */
        int nbChunksInFile = firstdwl.getNbChunks();
        if(chm.getNbChunksAvailable() != nbChunksInFile)
        {
            String newSeeder = client.query("createseeder", name);
            if(newSeeder == null)
            {
                System.err.println("Error requesting the creation of a new seeder");
                return false;
            }
            
            // restart, only this time with all the seeders needed...
            return downloadFile();
        }

        /*
            At this point,
            we normally have all the chunk owners we need
        */

        /* 
            (5) Download chunks one by one
            TODO later pool of downloaders
            TODO priority management (later...)
                -- could be done by using a treemap instead of hashmap for remoteChunkOwners
        */
        while(chm.numberOfChunksNotDownloaded() > 0)
        {
            // determine next chunk to download
            Chunk nextChunkToDownload = chm.getRarestChunk();

            if(nextChunkToDownload == null)
            {
                System.err.println("Error couldn't get a source for the next chunk !");
                System.err.println("Note: this shouldn't be happening");
            }

            // get a source for this chunk
            Owner chunkSource = nextChunkToDownload.getSource();

            // start downloader
            Downloader dwl = new Downloader(
                name,
                nextChunkToDownload.chunkNumber,
                chunkSource.ip,
                chunkSource.port,
                chunkSource.protocol,
                chunkSource.hash
            );

            // the thread will automatically save the file locally
            dwl.start();

            // wait for it to finish
            try{
                dwl.join();   
                if(checkHash("downloads/" + name + "-" + nextChunkToDownload.chunkNumber, chunkSource.hash) == true)
                {
                    // mark chunk as downloaded
                    chm.markChunkDownloaded(nextChunkToDownload.chunkNumber);
                }
                else{
                    nextChunkToDownload.removeOwner(chunkSource.ip, chunkSource.port, chunkSource.hash);
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                return false;
            }

            // TODO manage local seeder

            // TODO update database

        }

        // terminate all local seeders
        // update database

        // assemble file
        if(assembleFile(name, nbChunksInFile) == true)
        {
        }
        else{
            System.out.println("Error assembling the file");
            return false;
        }

        // check file hash
        if(checkHash("downloads/" + name, hashToGet) == true)
        {
            System.out.println("File " + name + " successfully downloaded");
        }
        else{
            System.out.println("File " + name + " chunk is not valid :(");
        }

        return true;
    }

    private boolean assembleFile(String name, int nbChunks)
    {
        try (FileOutputStream fos = new FileOutputStream("downloads/" + name);
            BufferedOutputStream mergingStream = new BufferedOutputStream(fos)) {
            for (int i = 0; i < nbChunks; i++) 
            {
                java.nio.file.Path localFile = Paths.get("downloads/" + name + "-" + i);
                Files.copy(localFile, mergingStream);
                Files.delete(localFile);
            }
        }   
        catch(IOException e)
        {
            System.err.println("Error re-assembling the file\n");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
    *   Makes sure a file or chunk hash is correct
    */
    public boolean checkHash(String file, String hash)
    {
        // check if file exists
        File f = new File(file);
        if (f.isFile() && f.canRead()) {

        }
        else{
            System.err.println("Error opening file " + file + " to check hash");
            return false;
        }

        // "downloads/" + file + "-" + chunkNumber
        String realHash = hashFile(file);
        if(!realHash.equals(hash))
        {
            System.err.println("Error comparing remote file hash=" + hash + 
                " and local hash=" + realHash);
            return false;
        }
        else
            return true;
    }

    /**
     * Read file to byte array with buffer, hash, then convert to hex string
     * http://www.codejava.net/coding/how-to-calculate-md5-and-sha-hash-values-in-java
     * @param file
     * @return hex string hash
     * @throws FileHashException
     * Copied from Seeder.java, dirty...
     */
    //
    private String hashFile(String file){
        try (FileInputStream inputStream = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance(HASHING_ALGORITHM);

            byte[] bytesBuffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(bytesBuffer)) > 0) {
                digest.update(bytesBuffer, 0, bytesRead);
            }

            byte[] hashedBytes = digest.digest();

            return bytesToHex(hashedBytes);

        } catch (NoSuchAlgorithmException | IOException e) {
            // do something !
            // running out of time...

        }
        return null;
    }

    /**
     * Converts byte array to hex string
     * https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
     * Copied from Seeder.java, dirty...
    */
    private final char[] hexArray = "0123456789ABCDEF".toCharArray();
    public String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


}