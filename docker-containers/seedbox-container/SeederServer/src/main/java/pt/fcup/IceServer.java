package pt.fcup;

import java.net.InetAddress;

public class IceServer implements Runnable {
    @Override
    public void run() {

        int status = 0;
        com.zeroc.Ice.Communicator ic = null;

        try {
            ic = com.zeroc.Ice.Util.initialize();

            // Creates ICE adapter and binds RequestableI interface
            com.zeroc.Ice.ObjectAdapter adapter =
                    ic.createObjectAdapterWithEndpoints("SeederRequestAdapter", "default -p 8082");
            adapter.add(new RequestableI(), com.zeroc.Ice.Util.stringToIdentity("SeederRequest"));
            adapter.activate();
            ic.waitForShutdown();

        } catch (com.zeroc.Ice.LocalException e) {
            e.printStackTrace();
            status = 1;

        } catch (Exception e) {
            System.err.println(e.getMessage());
            status = 1;

        }

        if (ic != null) {
            try {
                ic.destroy();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                status = 1;
            }
        }
        System.exit(status);
    }
}
