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
    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();
    private int BASE_PORT;
    private int nbChunks;

    public UploadServer(int BASE_PORT, int nbChunks) {
        this.BASE_PORT = BASE_PORT;
        this.nbChunks = nbChunks;

    }


    @Override
    public void run() {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

        boolean listening = true;
        try (ServerSocket serverSocket = new ServerSocket(BASE_PORT)) {
            while (listening) {
                ChunkSeeder cs = new ChunkSeeder(nbChunks, serverSocket.accept());
                executor.execute(cs);

            }

        } catch(java.net.BindException e) {
            // Do nothing, UploadServer is already running

        } catch (IOException e) {
            e.printStackTrace();

        }

        executor.shutdown();

    }
}
