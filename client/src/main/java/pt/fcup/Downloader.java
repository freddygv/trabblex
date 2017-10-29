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

	private int nbChunks;

	public Downloader(String file, int chunkNumber, String ip, int port, String protocol)
	{
		super();
		this.ip = ip;
		this.protocol = protocol;
		this.port = port;
		this.file = file;
		this.chunkNumber = chunkNumber;
	}

	@Override
	public void run()
	{
		if (protocol != "TCP") {
			System.out.println("Sorry, " + protocol + " is not yet supported!");
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

			System.out.println("Running for chunk number " + chunkNumber);
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
			System.out.println(String.format("Downloaded chunk #%s of '%s' successfully", chunkNumber, file));


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

}