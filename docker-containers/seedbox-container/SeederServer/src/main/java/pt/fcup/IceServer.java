package pt.fcup;

/**
 * Server threads accepting incoming connections from Client Manager.
 * Requests come as RPC calls using ZeroC's ICE.
 */
public class IceServer implements Runnable {
    @Override
    public void run() {

        com.zeroc.Ice.Communicator ic = null;

        try {
            ic = com.zeroc.Ice.Util.initialize();
            com.zeroc.Ice.ObjectAdapter adapter = ic.createObjectAdapterWithEndpoints("SeederRequestAdapter",
                                                                                      "default -p 8082");

            adapter.add(new RequestableI(), com.zeroc.Ice.Util.stringToIdentity("SeederRequest"));
            adapter.activate();
            ic.waitForShutdown();

        } catch (com.zeroc.Ice.LocalException e) {
            e.printStackTrace();

        } catch (Exception e) {
            System.err.println(e.getMessage());

        }

        if (ic != null) {
            try {
                ic.destroy();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
