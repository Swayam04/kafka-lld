package core;

import lombok.extern.slf4j.Slf4j;
import message.parser.RequestParser;
import message.request.RequestMessage;
import message.response.ResponseMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

@Slf4j
public class Server {
    private final ServerSocket serverSocket;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
    }

    public void start() throws IOException {
        while (true) {
            Socket clientSocket = serverSocket.accept();
            try(clientSocket;
                InputStream inputStream = clientSocket.getInputStream();
                OutputStream outputStream = clientSocket.getOutputStream()) {
                DataInputStream dataInputStream = new DataInputStream(inputStream);
                int messageSize = dataInputStream.readInt();

                byte[] payload = dataInputStream.readNBytes(messageSize);
                ByteBuffer requestBuffer = ByteBuffer.allocate(4 + messageSize);
                requestBuffer.putInt(messageSize);
                requestBuffer.put(payload);
                byte[] requestData = requestBuffer.array();

                RequestMessage requestMessage = RequestParser.parseRequest(requestData);

                ResponseMessage responseMessage = new ResponseMessage(requestMessage);
                outputStream.write(responseMessage.getMessage());

            } catch (Exception ex) {
                log.error("Error processing client request", ex);
            }
        }
    }
}