package core;

import lombok.extern.slf4j.Slf4j;
import message.parser.RequestParser;
import message.request.RequestMessage;
import message.response.ResponseMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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
            RequestMessage requestMessage = null;
            try(InputStream inputStream = clientSocket.getInputStream()) {
                byte[] requestData = inputStream.readAllBytes();
                requestMessage = RequestParser.parseRequest(requestData);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            OutputStream outputStream = clientSocket.getOutputStream();
            if (requestMessage == null) {
                log.error("Failed to parse request message");
            }
            ResponseMessage responseMessage = new ResponseMessage(requestMessage);
            outputStream.write(responseMessage.getMessage());
            outputStream.close();
            clientSocket.close();
        }
    }

}
