import core.Server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        System.err.println("Logs from your program will appear here!");
        int port = 9092;
        try {
            Server server = new Server(port);
            server.start();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
