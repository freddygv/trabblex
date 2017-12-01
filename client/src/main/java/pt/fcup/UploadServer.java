package pt.fcup;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * UploadServer creates a thread pool to handle incoming requests for file chunks.
 */
public class UploadServer implements Runnable {
    private final int MAX_THREADS = Runtime.getRuntime().availableProcessors();
    private final int MAX_RETRIES = 6;
    private int port;

    public UploadServer(int port) {
        this.port = port;

    }

    @Override
    public void run() {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

        int counter = 0;
        while (counter++ < MAX_RETRIES) {
            try (ServerSocket serverSocket = new ServerSocket(port)) {

                // ServerSocket created, listen for incoming connections
                while (true) {
                    ChunkSeeder cs = new ChunkSeeder(serverSocket.accept());
                    executor.execute(cs);

                }

            } catch (IOException e) {
                // Try a new port
                port++;

            }
        }

        executor.shutdown();
    }
}
