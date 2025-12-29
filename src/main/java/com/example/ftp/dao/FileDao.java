package com.example.ftp.dao;

import java.sql.*;

public class FileDao {

    public static void insert(int userId, String filename, int dirId, String absPath, long size) {
        String sql = "INSERT INTO file_info(user_id, filename, directory_id, abs_path, size, create_time) VALUES(?, ?, ?, ?, ?, NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, filename);
            ps.setInt(3, dirId);
            ps.setString(4, absPath);
            ps.setLong(5, size);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static String getAbsPath(int fileId) {
        String sql = "SELECT abs_path FROM file_info WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("abs_path");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}
