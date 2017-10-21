package pt.fcup;

public class IceServer {
    public static void main(String[] args) {
        IceServer p = new IceServer();
        p.run();
    }

    private void run() {
        int status = 0;
        com.zeroc.Ice.Communicator ic = null;
        try {
            ic = com.zeroc.Ice.Util.initialize();
            com.zeroc.Ice.ObjectAdapter adapter =
                    ic.createObjectAdapterWithEndpoints("SeederRegistrationAdapter", "default -p 8081");
            adapter.add(new RegistrableI(), com.zeroc.Ice.Util.stringToIdentity("SeederRegistration"));
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
