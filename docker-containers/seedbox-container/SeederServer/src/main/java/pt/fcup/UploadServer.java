package pt.fcup;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class UploadServer implements Runnable {
    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();
    private final int BASE_PORT = 29200;

    @Override
    public void run() {
        System.out.println(String.format("Starting Upload ThreadPool with %s threads", MAX_THREADS));

        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

        boolean listening = true;
        try (ServerSocket serverSocket = new ServerSocket(BASE_PORT)) {
            while (listening) {
                System.out.println("Listening for new connections...");
                ServerChunkSeeder cs = new ServerChunkSeeder(serverSocket.accept());
                executor.execute(cs);

            }

        } catch (IOException e) {
            e.printStackTrace();

        }

        executor.shutdown();

    }
}
