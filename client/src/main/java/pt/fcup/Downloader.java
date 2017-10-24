package pt.fcup;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;


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
			//Initialize socket
			Socket socket = new Socket(InetAddress.getByName(ip), port);

			//Initialize the FileOutputStream to the output file's full path.
			FileOutputStream fos = new FileOutputStream("files/" + file);
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