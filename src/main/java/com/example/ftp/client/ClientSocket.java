package com.example.ftp.client;

import java.io.*;
import java.net.Socket;

public class ClientSocket {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String host;
    private int port;

    public ClientSocket(String host, int port) { this.host = host; this.port = port; }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public String sendCommand(String cmd) throws IOException {
        writer.write(cmd);
        writer.newLine();
        writer.flush();
        return reader.readLine();
    }

    public void sendFile(File file) throws IOException {
        writer.write("PUT|" + file.getName() + "|" + file.length());
        writer.newLine();
        writer.flush();
        if (!"READY".equals(reader.readLine())) throw new IOException("Server not ready");
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream out = socket.getOutputStream()) {
            byte[] buf = new byte[4096]; int read;
            while ((read = fis.read(buf)) != -1) out.write(buf,0,read);
        }
        reader.readLine();
    }

    public void getFile(int fileId, File targetFile) throws IOException {
        writer.write("GET|" + fileId); writer.newLine(); writer.flush();
        try (InputStream in = socket.getInputStream();
             FileOutputStream fos = new FileOutputStream(targetFile)) {
            byte[] buf = new byte[4096]; int read;
            while ((read = in.read(buf)) != -1) { fos.write(buf,0,read); if (in.available()==0) break; }
        }
    }

    public void close() throws IOException { socket.close(); }
}
