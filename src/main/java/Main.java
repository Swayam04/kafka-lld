import core.Broker;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        System.err.println("Logs from your program will appear here!");
        int port = 9092;
        try {
            Broker broker = new Broker(port);
            broker.start();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
