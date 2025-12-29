package main.serve;

import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class FileService {
    private final String baseDir;

    public FileService(String baseDir) {
        this.baseDir = baseDir;
        File f = new File(baseDir);
        if (!f.exists()) f.mkdirs();
    }

    private Connection getConnection() throws Exception {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            props.load(is);
        }
        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }

    public void handleClient(Socket socket) throws IOException {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            while (true) {
                String command;
                try {
                    command = dis.readUTF();
                } catch (EOFException e) {
                    System.out.println("Client disconnected.");
                    break;
                }

                switch (command) {
                    case "LIST":
                        handleList(dos);
                        break;
                    case "UPLOAD":
                        handleUpload(dis, dos);
                        break;
                    case "DOWNLOAD":
                        handleDownload(dis, dos);
                        break;
                    case "CREATE_DIR":
                        handleCreateDir(dis, dos);
                        break;
                    default:
                        dos.writeUTF("UNKNOWN_COMMAND");
                        dos.flush();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleList(DataOutputStream dos) throws IOException, SQLException {
        List<String> items = new ArrayList<>();

        try (Connection conn = getConnection()) {
            // 目录
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT abs_path FROM directories")) {
                while (rs.next()) {
                    items.add(rs.getString("abs_path") + "/");
                }
            }
            // 文件
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT dir_path, name FROM files")) {
                while (rs.next()) {
                    items.add(rs.getString("dir_path") + "/" + rs.getString("name"));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        dos.writeInt(items.size());
        for (String item : items) dos.writeUTF(item);
        dos.flush();
    }

    private void handleUpload(DataInputStream dis, DataOutputStream dos) throws Exception {
        String dirPath = dis.readUTF();
        String filename = dis.readUTF();
        long fileLength = dis.readLong();

        // 在磁盘上创建目录
        File folder = new File(baseDir + dirPath);
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            long read = 0;
            while (read < fileLength) {
                int r = dis.read(buffer, 0, (int) Math.min(buffer.length, fileLength - read));
                fos.write(buffer, 0, r);
                read += r;
            }
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO files (name, dir_path, abs_path) VALUES (?, ?, ?)")) {
            ps.setString(1, filename);
            ps.setString(2, dirPath);
            ps.setString(3, file.getAbsolutePath());
            ps.executeUpdate();
        }

        dos.writeUTF("UPLOAD_SUCCESS");
        dos.flush();
    }

    private void handleDownload(DataInputStream dis, DataOutputStream dos) throws IOException, SQLException {
        String filePath = dis.readUTF();
        File file;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT abs_path FROM files WHERE dir_path || '/' || name = ?")) {
            ps.setString(1, filePath);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                dos.writeUTF("FILE_NOT_FOUND");
                dos.flush();
                return;
            }
            file = new File(rs.getString("abs_path"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (!file.exists()) {
            dos.writeUTF("FILE_NOT_FOUND");
            dos.flush();
            return;
        }

        dos.writeUTF("DOWNLOAD_START");
        dos.writeLong(file.length());

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int r;
            while ((r = fis.read(buffer)) != -1) dos.write(buffer, 0, r);
        }
        dos.flush();
    }

    private void handleCreateDir(DataInputStream dis, DataOutputStream dos) throws Exception {
        String dirPath = dis.readUTF();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO directories (name, parent_dir, abs_path) VALUES (?, ?, ?)")) {
            String name = new File(dirPath).getName();
            String parent = new File(dirPath).getParent();
            ps.setString(1, name);
            ps.setString(2, parent);
            ps.setString(3, dirPath);
            ps.executeUpdate();
        }

        dos.writeUTF("CREATE_DIR_SUCCESS");
        dos.flush();
    }
}
