package pt.fcup;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.io.*;


class Downloader extends Thread
{
	String ip, hash, protocol, file;
	int port, chunkSize;
	byte[] contents;


	public Downloader(String file, String ip, int port, String protocol, byte[] buf)
	{
		super();
		this.ip = ip;
		this.hash = hash;
		this.protocol = protocol;
		this.port = port;
		this.contents = buf;
		this.file = file;
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
			System.out.println(System.getProperty("user.dir"));
			//Initialize socket
			Socket socket = new Socket(InetAddress.getByName(ip), port);
			FileOutputStream fos = null;

			try
			{
				fos = new FileOutputStream("downloads/" + file);
			}
			catch(IOException e)
	        	{
	        		System.out.println("Couldn't create local file");
	        		System.out.println("Please check if directory downloads exists");
	        		System.out.println("    in same folder as jar");
	           	e.printStackTrace();
				Thread.currentThread().interrupt();
	        	}
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			InputStream is = socket.getInputStream();

			//No of bytes read in one read() call
			int bytesRead = 0;

			while((bytesRead=is.read(contents))!=-1)
			{
			    bos.write(contents, 0, bytesRead);
			    // TODO add more relevant info
			    System.out.println("Downloading chunk...");
			}

			bos.flush();
			socket.close();

			System.out.println("Downloaded chunk " + hash + " successfully");
		}
		catch(Exception e)
        	{
           	e.printStackTrace();
        	}

	}

}