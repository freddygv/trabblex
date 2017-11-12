package pt.fcup;

import java.util.Properties;
import java.util.ArrayList;
import java.util.Scanner;


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
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;



public class FileDownloader extends Thread
{
    private String name, fileHash;
    private JerseyClient client;
    private int nbChunksNotDownloaded;

    private final int localPort = 26000;
    private String localIP;

    private ChunkManager chm;

    // TODO pass the hashing algorithm through the header...
    private final String HASHING_ALGORITHM = "SHA-256";

    public FileDownloader(String name, String hash, JerseyClient client)
    {
        this.name = name;
        this.fileHash = hash;

        // client reference passed by the parent client
        this.client = client;

        try{
            localIP = InetAddress.getLocalHost().getHostAddress();

            if(localIP.equals("127.0.1.1"))
            {
                Scanner sc = new Scanner(System.in);
                System.out.println("Please enter your IP adress: ");
                localIP = sc.nextLine();

            }
        }
        catch(UnknownHostException e)
        {
            System.err.println("Couldn't get local IP adress !");
            // do something
        }
        System.err.println("Local IP adress is " + localIP);
    }

    public String getFileName()
    {
        return name;
    } 

    public int getnbChunksNotDownloaded()
    {
        return nbChunksNotDownloaded;
    }

    /**
    * Starts the download of a file
    * Via a TCP connection
    **/
    @Override
    public void run()
    {
        try{
            downloadFile();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private String getChunkOwners()
    {
        if(fileHash == null)
        {
            System.err.println("Couldn't solve hash from filename!");
            System.err.println("Error downloading file " + name);
            return null;
        }

        String chunkOwners = client.query("getowners", fileHash);

        if(chunkOwners == null)
        {
            System.err.println("Couldn't get the chunk owners of the file!");
        }

        System.out.println("ChunkOwners = " + chunkOwners);
        System.out.println("Filehash = " + fileHash);

        return chunkOwners;
    }

    private void checkDownload(Chunk nextChunkToDownload, Owner chunkSource) throws Exception
    {
        Map<String,String> queryParams = new HashMap<String,String>();
        queryParams.put("file_hash", fileHash);
        queryParams.put("chunk_hash", chunkSource.hash);
        queryParams.put("chunk_id", Integer.toString(nextChunkToDownload.chunkNumber));
        queryParams.put("ip", localIP);
        queryParams.put("port", Integer.toString(localPort));

        if(checkHash("downloads/" + name + "-" + nextChunkToDownload.chunkNumber, chunkSource.hash) == true)
        {
            // mark chunk as downloaded
            chm.markChunkDownloaded(nextChunkToDownload.chunkNumber);

            // update database
            client.query("registerclientseeder", null, queryParams);

            // move file to sources
            File f = new File("downloads/" + name + "-" + nextChunkToDownload.chunkNumber);
            File f2 = new File("sources/" + name + "-" + nextChunkToDownload.chunkNumber);
            f.renameTo(f2);
        }
        else{
            nextChunkToDownload.removeOwner(chunkSource.ip, chunkSource.port, chunkSource.hash);
            // inform database to remove chunk owner
            client.query("unregisterclientseeder", null, queryParams);
            System.out.println("Bad chunk hash " + "downloads/" + name + "-" + nextChunkToDownload.chunkNumber + "\n"
                + hashFile("downloads/" + name + "-" + nextChunkToDownload.chunkNumber) + "\n"
                + chunkSource.hash + "\n"
                + "Will now try another source");
            // delete local chunk and try again
            java.nio.file.Path localFile = Paths.get("downloads/" + name + "-" + nextChunkToDownload.chunkNumber);
            //Files.delete(localFile);
        }

    }

    private boolean downloadFile() throws IOException
    {
        // the protocol that will be used - atm, fixed
        String protocol = "TCP";

        // storing the file
        ArrayList<Byte> file = new ArrayList<Byte>();

        // Fetch all the chunk owners related to the client
        String chunkOwners = getChunkOwners();
        JSONArray remoteChunkOwners = new JSONArray(chunkOwners);
        // have the chunkmanager point to a new instance
        // TODO does this work ?
        chm = new ChunkManager(remoteChunkOwners);


        UploadServer up = null;

        int nbChunksInFile = 0;

        if(chunkOwners == null)
            Thread.currentThread().interrupt();

        do{
            System.out.println("Still has to download " + chm.numberOfChunksNotDownloaded() + " chunks");
            System.out.println("nbChunksInFile " + nbChunksInFile + "");
            
            // determine next chunk to download
            Chunk nextChunkToDownload = chm.getRarestChunk();


            if(nextChunkToDownload == null)
            {
                System.err.println("Requesting a new seeder...\n");

                String newSeeder = client.query("createseeder", name);
                if(newSeeder == null)
                {
                    System.err.println("Error requesting the creation of a new seeder: " + newSeeder);
                    return false;
                }
                System.out.println("Result = " + newSeeder);
                
                // get to next loop
                continue;
            }

            // update chunkmanager to make sure we have all the sources
                
          /*  String newChunkOwners = getChunkOwners();
            JSONArray newRemoteChunkOwners = new JSONArray(newChunkOwners);
            chm.addChunkOwners(newRemoteChunkOwners);*/

            System.out.println("Downloading chunk " + nextChunkToDownload.chunkNumber);

            // get a source for this chunk
            Owner chunkSource = nextChunkToDownload.getSource();

            // if no sources available, request a seeder
          /*  if(chunkSource == null)
            {
                
            }*/

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
                // todo check if number of chunks has changed ?
                nbChunksInFile = dwl.getNbChunks();
                checkDownload(nextChunkToDownload, chunkSource);

                // Assess if all chunks are available 
                // If no, create a seeder, and start all over again
                if(chm.getNbChunksAvailable() < nbChunksInFile)
                {
                    // debug
                    //System.out.println("Chunks available: " + chm.getNbChunksAvailable() + "/" + nbChunksInFile);
                    String newSeeder = client.query("createseeder", name);
                    if(newSeeder == null)
                    {
                        System.err.println("Error requesting the creation of a new seeder");
                        return false;
                    }
                    
                    // restart, only this time with all the seeders needed...
                    return downloadFile();
                }

                // if no uploadserver running, create one
                if(up == null)
                {
                    up = new UploadServer(localPort, nbChunksInFile);
                    Thread usThread = new Thread(up, "Client upload Thread");
                    usThread.start();
                }


            }
            catch(Exception e)
            {
                e.printStackTrace();
                return false;
            }


        } while(chm.numberOfChunksNotDownloaded() > 0 || nbChunksInFile == 0);


        // assemble files bla bla bla
        if(assembleFile(name, nbChunksInFile) == false)
        {
            System.err.println("Error assembling file " + name);
            return false;
        }

        // check file hash
        if(checkHash("downloads/" + name, fileHash) == true)
        {
            System.out.print("File " + name + " successfully downloaded\n> ");
        }
        else{
            System.err.print("File " + name + " hash is not valid :(\n> ");
        }

        return true;


    }

    private boolean assembleFile(String name, int nbChunks)
    {
        try (FileOutputStream fos = new FileOutputStream("downloads/" + name);
            BufferedOutputStream mergingStream = new BufferedOutputStream(fos)) {
            for (int i = 0; i < nbChunks; i++) 
            {
                java.nio.file.Path localFile = Paths.get("sources/" + name + "-" + i);
                Files.copy(localFile, mergingStream);
                //Files.delete(localFile);
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
            //System.err.println("Error opening file " + file + " to check hash");
            return false;
        }

        String realHash = hashFile(file);
        if(!realHash.equals(hash))
        {
            // debug - now automatically managed
            //System.err.println("Error comparing remote file hash=" + hash + 
            //    " and local hash=" + realHash);
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