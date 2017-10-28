package pt.fcup;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.io.*;
import java.util.Properties;

class Downloader extends Thread
{
	private String ip, hash, protocol, file;
	private int port, chunkSize;
	private byte[] contents;
	private int nbChunks;

	public Downloader(String file, String ip, int port, String protocol, byte[] buf, int chunkNumber)
	{
		super();
		this.ip = ip;
		this.hash = hash;
		this.protocol = protocol;
		this.port = port;
		this.contents = buf;
		this.file = file;
	}

	public int getNbChunks( )
	{
		return nbChunks;
	}

	@Override
	public void run()
	{
		if(protocol != "TCP")
		{
			System.out.println("Sorry, " + protocol + " is not yet supported!");
			Thread.currentThread().interrupt();
		}

		try
		{

			/*	
				(1)
				Send info to the seeder:
				(((local ip, local port))) --> obtained via the socket info
				filename, chunk number

			*/

			Socket clientSocket = new Socket(InetAddress.getByName(ip), port);
			DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
			DataInputStream dis = new DataInputStream(clientSocket.getInputStream());

			// the seeder is already assigned to a file so he only needs a chunk number
  			dos.writeBytes(chunk);


			clientSocket.close();

            /*
           		(2) 
				Download file

			*/

			FileOutputStream fos = null;

			try
			{
				fos = new FileOutputStream("downloads/" + file + chunkNumber);
			}
			catch(IOException e)
	        {
	        		System.out.println("Couldn't create local file");
	        		System.out.println("Please check if directory downloads exists");
	        		System.out.println("    in same folder as jar");
	           		e.printStackTrace();
					Thread.currentThread().interrupt();
	        }

			// handshake
			String propertiesText = dis.readUTF();
			Properties properties = new Properties();
			properties.load(new StringReader(propertiesText));
			nbChunks = dis.readInt();
			String fileChunkName = dis.readInt();

			System.out.println("Number of chunks = " + nbChunks)

			//No of bytes read in one read() call
			int bytesRead = 0; 

			while((bytesRead=dis.read(contents)) > 0)
			{
			    fos.write(contents, 0, bytesRead);
			    // TODO add more relevant info
			    System.out.println("Downloading chunk...");
			}

			fos.flush(); 
			fos.close();
			clientSocket.close(); 

			System.out.println("Downloaded chunk " + hash + " successfully");
		}
		catch(Exception e)
        {
           	e.printStackTrace();
        }

	}

}