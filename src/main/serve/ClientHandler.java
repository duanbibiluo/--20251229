package main.serve;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(
                        socket.getOutputStream(), true)
        ) {
            String commandLine = reader.readLine(); // COMMAND: XXX
            if (commandLine == null) return;

            if (commandLine.contains("LS")) {
                FileService.handleLs(writer);
            } else if (commandLine.contains("PUT")) {
                FileService.handlePut(reader, writer, socket);
            } else if (commandLine.contains("GET")) {
                FileService.handleGet(reader, writer, socket);
            } else {
                writer.println("STATUS: 400");
                writer.println("MESSAGE: Unknown command");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
