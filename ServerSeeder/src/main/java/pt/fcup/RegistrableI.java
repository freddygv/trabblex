package pt.fcup;

public class RegistrableI implements pt.fcup.generated.RegistrableI {
    public void registerSeeder(String regMessage, com.zeroc.Ice.Current current) {
        System.out.println(regMessage);

    }

    public void deregisterSeeder(String deregMessage, com.zeroc.Ice.Current current){
        System.out.println(deregMessage);

    }

}