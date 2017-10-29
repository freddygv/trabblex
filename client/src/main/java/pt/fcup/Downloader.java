package pt.fcup;

import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.io.*;
import pt.fcup.exception.*;

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
	public void run()
	{
		//System.out.println("Connecting to " + ip + ":" + port);

		if (protocol != "TCP") {
			System.out.println("Sorry, protocol " + protocol + " is not yet supported!");
			return;
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

			System.out.println(String.format("Requesting chunk id #%s for file: %s", chunkNumber, file));
			out.println(chunkNumber);
			out.println(file);

			nbChunks = Integer.parseInt(in.readLine());

			byte[] contents = new byte[1024*1024];
			int bytesRead = 0;
			while ((bytesRead = dis.read(contents)) > 0) {
				fos.write(contents, 0, bytesRead);
			}

			fos.flush();
			fos.close();
			dis.close();
			System.out.println(String.format("Downloaded chunk %s of '%s' successfully", chunkNumber, file));

		}
		catch(java.io.FileNotFoundException e)
		{
			System.err.println("Couldn't create output file - check if downloads folder exists");
		}
		catch(java.net.ConnectException e)
		{
        	System.out.println("couldn't connect to " + ip + ":" + port);
        	return;
		}
		catch(Exception e)
        {
        	System.out.println("couldn't connect to " + ip + ":" + port);
           	e.printStackTrace();
        	return;
        }

        return;

	}

	public int getNbChunks()
	{
		return nbChunks;
	}
}