package pt.fcup;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UploadServer implements Runnable {
    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();
    private int BASE_PORT;
    private String file;
    private int nbChunks;

    public UploadServer(int BASE_PORT, int nbChunks, String file)
    {
        this.BASE_PORT = BASE_PORT;
        this.file = file;
        this.nbChunks = nbChunks;
    }


    @Override
    public void run() {
        //System.out.println(String.format("Starting Upload ThreadPool with %s threads", MAX_THREADS));

        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

        boolean listening = true;
        try (ServerSocket serverSocket = new ServerSocket(BASE_PORT)) {
            while (listening) {
                //System.out.println("Listening for new connections...");
                new ChunkSeeder(BASE_PORT, nbChunks, serverSocket.accept()).start();

//                    // TODO: Need to find a way to check for inactive seeder and break out of this loop
//                    // TODO: Keep track of time? Let a certain amount of time pass before de-reg
//                    if (((ThreadPoolExecutor) executor).getActiveCount() == 0) {
//                        listening = false;
//                    }

            }

        } 
        catch(java.net.BindException e)
        {
            // this means an uploadserver is already started for another file
        }
        catch (IOException e) {
            e.printStackTrace();

        }

        executor.shutdown();

    }
}
