package pt.fcup;

public class Owner {
    public String ip;
    public int port; 
    public String protocol;
    public boolean is_seeder;
    public String hash;

    Owner(String ip, int port, String protocol, boolean is_seeder, String hash) {
        this.ip = ip;
        this.port = port;
        this.protocol = protocol;
        this.is_seeder = is_seeder;
        this.hash = hash;

    }
}