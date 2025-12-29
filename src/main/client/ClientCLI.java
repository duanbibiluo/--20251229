package main.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientCLI {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9000;

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("> ");
            String line = sc.nextLine().trim();
            if (line.equalsIgnoreCase("exit")) break;

            String[] parts = line.split("\\s+", 2);
            String cmd = parts[0].toLowerCase();
            String filename = parts.length > 1 ? parts[1] : null;

            switch (cmd) {
                case "put":
                    if (filename == null) { System.out.println("Usage: put <filename>"); break; }
                    putFile(filename); break;
                case "get":
                    if (filename == null) { System.out.println("Usage: get <filename>"); break; }
                    getFile(filename); break;
                case "ls":
                    listFiles(); break;
                default:
                    System.out.println("Unknown command");
            }
        }
        System.out.println("Client exited.");
    }

    private static void putFile(String filename) throws Exception {
        File file = new File(filename);
        if (!file.exists()) { System.out.println("File not found"); return; }

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             FileInputStream fis = new FileInputStream(file)) {

            writer.println("COMMAND: PUT");
            writer.println("FILENAME: " + file.getName());
            writer.println("FILESIZE: " + file.length());

            OutputStream out = socket.getOutputStream();
            byte[] buf = new byte[4096];
            int len;
            while ((len = fis.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.flush();

            System.out.println(reader.readLine()); // Upload finished
        }
    }

    private static void getFile(String filename) throws Exception {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            writer.println("COMMAND: GET");
            writer.println("FILENAME: " + filename);

            String status = reader.readLine();
            if (!status.contains("200")) {
                System.out.println("Download failed: " + reader.readLine());
                return;
            }

            String sizeLine = reader.readLine();
            long size = Long.parseLong(sizeLine.split(":")[1].trim());

            File file = new File("download_" + filename);
            try (FileOutputStream fos = new FileOutputStream(file);
                 InputStream in = socket.getInputStream()) {

                byte[] buf = new byte[4096];
                int len;
                long received = 0;
                while (received < size && (len = in.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                    received += len;
                }
            }
            System.out.println("Download finished: " + filename);
        }
    }

    private static void listFiles() throws Exception {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            writer.println("COMMAND: LS");
            String line;
            while ((line = reader.readLine()) != null && !line.equals("END")) {
                System.out.println(line);
            }
        }
    }
}
