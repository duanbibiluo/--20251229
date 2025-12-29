package main.serve;

import main.common.Config;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class FileService {

    /**
     * 处理 PUT 上传文件
     */
    public static void handlePut(BufferedReader reader,
                                 PrintWriter writer,
                                 Socket socket) throws Exception {
        // 读取文件信息
        String filenameLine = reader.readLine(); // FILENAME: xxx
        String sizeLine = reader.readLine();     // FILESIZE: xxx
        String filename = filenameLine.split(":")[1].trim();
        long filesize = Long.parseLong(sizeLine.split(":")[1].trim());

        // 确认服务器存储目录
        File rootDir = new File(Config.get("server.rootDir"));
        if (!rootDir.exists()) rootDir.mkdirs();

        File file = new File(rootDir, filename);

        // 接收文件二进制流
        try (FileOutputStream fos = new FileOutputStream(file);
             InputStream in = socket.getInputStream()) {

            byte[] buffer = new byte[4096];
            int len;
            long received = 0;

            while (received < filesize && (len = in.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                received += len;
            }
        }

        // 写入数据库
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO file_info(filename, filepath, filesize) VALUES(?,?,?)")) {
            ps.setString(1, filename);
            ps.setString(2, file.getAbsolutePath());
            ps.setLong(3, filesize);
            ps.executeUpdate();
        }

        writer.println("Upload finished.");
    }

    /**
     * 处理 GET 下载文件
     */
    public static void handleGet(BufferedReader reader,
                                 PrintWriter writer,
                                 Socket socket) throws Exception {
        // 读取客户端请求的文件名
        String filenameLine = reader.readLine(); // FILENAME: xxx
        String filename = filenameLine.split(":")[1].trim();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT filepath, filesize FROM file_info WHERE filename=?")) {

            ps.setString(1, filename);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                writer.println("STATUS: 404");
                writer.println("MESSAGE: File not found");
                return;
            }

            String path = rs.getString("filepath");
            long size = rs.getLong("filesize");

            writer.println("STATUS: 200");
            writer.println("FILESIZE: " + size);

            try (FileInputStream fis = new FileInputStream(path);
                 OutputStream out = socket.getOutputStream()) {

                byte[] buffer = new byte[4096];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            }
        }
    }

    /**
     * 处理 LS 列出服务器文件
     */
    public static void handleLs(PrintWriter writer) throws Exception {
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT filename FROM file_info")) {

            while (rs.next()) {
                writer.println(rs.getString("filename"));
            }
            writer.println("END"); // 结束标记
        }
    }
}
