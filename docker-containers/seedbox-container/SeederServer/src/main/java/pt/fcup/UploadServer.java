package pt.fcup;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * UploadServer creates a thread pool to handle incoming requests for file chunks.
 *
 */
public class UploadServer implements Runnable {
    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();
    private final int BASE_PORT = 29200;

    @Override
    public void run() {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

        boolean listening = true;
        try (ServerSocket serverSocket = new ServerSocket(BASE_PORT)) {
            while (listening) {
                ChunkSeeder cs = new ChunkSeeder(serverSocket.accept());
                executor.execute(cs);

            }

        } catch (IOException e) {
            e.printStackTrace();

        }

        executor.shutdown();

    }
}
