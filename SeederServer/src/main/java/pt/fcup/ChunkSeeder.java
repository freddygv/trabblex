package pt.fcup;

import org.json.JSONObject;
import pt.fcup.generated.*;
import pt.fcup.exception.FileHashException;
import pt.fcup.generated.RegistrableIPrx;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;

// TCP imports
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ChunkSeeder {

	public ChunkSeeder(String file, int port)
	{
		// open TCP connection on port X
	}

	public void run()
	{
		// wait for chunk number message via TCP
		// seed via TCP the chunk that has been asked

	}
}