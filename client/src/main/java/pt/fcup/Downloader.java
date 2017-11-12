package pt.fcup;

import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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

		System.out.println("Started downloader");
	}

	@Override
	public void run()
	{ 
		downloadChunk();
	}

	public int downloadChunk()
	{
		// dobug
		System.out.println("Connecting to " + ip + ":" + port + " to get file hash " + hash);

		if (protocol != "TCP") {
			System.out.println("Sorry, protocol " + protocol + " is not yet supported!");
			return 0;
		}

		try{
			Socket clientSocket = new Socket();
			InetSocketAddress adr = new InetSocketAddress(ip, port);
			clientSocket.connect(adr, 5000);
			clientSocket.setSoTimeout(10000);

			PrintWriter out =
					new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in =
					new BufferedReader(
							new InputStreamReader(clientSocket.getInputStream()));

			DataInputStream dis = new DataInputStream(clientSocket.getInputStream());

			FileOutputStream fos = new FileOutputStream("downloads/" + file + "-" + chunkNumber);

			// debug
        	System.out.println("Connection successfull to " + ip + ":" + port);
			System.out.println(String.format("Requesting chunk id #%s for file: %s", chunkNumber, file));
			out.println(chunkNumber);
			out.println(file);

			// debug
        	System.out.println("Sent chunk number and file name");

			nbChunks = Integer.parseInt(in.readLine());

			// debug
        	System.out.println("Got number of chunks = " + nbChunks);

			byte[] contents = new byte[1024*1024];
			int bytesRead = 0;
			while ((bytesRead = dis.read(contents)) > 0) {
				fos.write(contents, 0, bytesRead);
			}

			System.out.println("Finished reading bytes");

			fos.flush();
			fos.close();
			dis.close();
			// debug
			System.out.println(String.format("Downloaded chunk %s of '%s' successfully", chunkNumber, file));

		}
		catch(java.io.FileNotFoundException e)
		{
			// debug
			//System.err.println("Couldn't create output file - check if downloads folder exists");

			// Create download folder
			boolean success = (new File("downloads")).mkdir();
			if (!success) {
				System.err.println("Couldn't create local directory downloads, please try creating it manually");
			}

			// if download folder created, try again downloading the chunk
			return downloadChunk();
		}
		catch(java.net.ConnectException e)
		{
			// debug
        	System.out.println("Couldn't connect to " + ip + ":" + port);
        	e.printStackTrace();
        	return 0;
		}
		catch(Exception e)
        {
        	// debug
        	System.out.println("Couldn't connect to " + ip + ":" + port);
           	e.printStackTrace();
        	return 0;
        }

        return 1;
	}

	public int getNbChunks()
	{
		return nbChunks;
	}
}