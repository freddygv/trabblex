package pt.fcup;

import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.io.*;

class Downloader extends Thread
{
	private final String ip;
	private final String protocol;
	private final String file;
	private final int port;
	private final int chunkNumber;
	private final String hash;

	private int nbChunks;

	public Downloader(String file, int chunkNumber, String ip, int port, String protocol, String hash)
	{
		super();
		this.ip = ip;
		this.protocol = protocol;
		this.port = port;
		this.file = file;
		this.chunkNumber = chunkNumber;
		this.hash = hash;
	}

	@Override
	public void run() throws FileHashException
	{
		//System.out.println("Connecting to " + ip + ":" + port);

		if (protocol != "TCP") {
			System.out.println("Sorry, protocol " + protocol + " is not yet supported!");
			Thread.currentThread().interrupt();
		}

		try (
				Socket clientSocket = new Socket(InetAddress.getByName(ip), port);

				PrintWriter out =
						new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in =
						new BufferedReader(
								new InputStreamReader(clientSocket.getInputStream()));

				DataInputStream dis = new DataInputStream(clientSocket.getInputStream());

				FileOutputStream fos = new FileOutputStream("downloads/" + file + "-" + chunkNumber);
		) {

			System.out.println("Downloader: requesting chunk number " + chunkNumber);
			out.println(chunkNumber);


			// TODO: Are these needed? where are these properties used
//			// handshake
//			String propertiesText = dis.readUTF();
//			Properties properties = new Properties();
//			properties.load(new StringReader(propertiesText));

			nbChunks = Integer.parseInt(in.readLine());
			System.out.println("Number of chunks is: " + nbChunks);

			byte[] contents = new byte[1024*1024];
			int bytesRead = 0;
			while ((bytesRead = dis.read(contents)) > 0) {
				fos.write(contents, 0, bytesRead);
			}

			fos.flush();
			System.out.println(String.format("Downloaded chunk %s of '%s' successfully", chunkNumber, file));

			if(checkChunkHash() == false )
			{
				System.err.println("Error: chunk hash isn't good one");
				throw new FileHashException;
			}

		}
		catch(java.io.FileNotFoundException e)
		{
			System.err.println("Couldn't create output file - check if downloads folder exists");
		}
		catch(Exception e)
        {
           	e.printStackTrace();
        }

	}

	public int getNbChunks()
	{
		return nbChunks;
	}


	/**
	*	Makes sure a chunk hash is correct
	*/
	public boolean checkChunkHash()
	{
		String realHash = hashFile("downloads/" + file + "-" + chunkNumber);
		if(realHash != hash)
			return false;
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
    private String hashFile(String file) throws FileHashException {
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
            throw new FileHashException("Could not generate hash from file", e);

        }
    }

}