package pt.fcup;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class UploadManager {

    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();

    public void createPool(Seeder fileSeeder, int port) throws IOException {
        System.out.println(String.format("Starting ThreadPool with %s threads", MAX_THREADS));
        System.out.println("Number of available processors: " + Runtime.getRuntime().availableProcessors());

        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

        boolean listening = true;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (listening) {
<<<<<<< HEAD
                System.out.println("Listening for new connections on port " + port);
                new ChunkSeeder(fileSeeder, serverSocket.accept()).start();
=======
                System.out.println("Listening for new connections...");
                new ChunkSeeder(port, fileSeeder.getNumberOfChunks(), fileSeeder.getFilepath(), fileSeeder.getFileName(),
                                serverSocket.accept()).start();
>>>>>>> master

//                    // TODO: Need to find a way to check for inactive seeder and break out of this loop
//                    // TODO: Keep track of time? Let a certain amount of time pass before de-reg
//                    if (((ThreadPoolExecutor) executor).getActiveCount() == 0) {
//                        listening = false;
//                    }

            }

        } catch (IOException e) {
            System.err.println("Could not listen on port " + port++);
            System.out.println("Trying " + port);

        }

        executor.shutdown();

        System.out.println("No active connections. De-registering seeder.");
        fileSeeder.deregisterSeeder();

    }
}