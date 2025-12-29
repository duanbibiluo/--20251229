package com.example.ftp.dao;

import com.example.ftp.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DirectoryDao {

    public static int insertRoot(int userId, String absPath) {
        String sql = "INSERT INTO directory(user_id, name, parent_id, abs_path, create_time) VALUES(?, ?, ?, ?, NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, "root");
            ps.setNull(3, Types.INTEGER);
            ps.setString(4, absPath);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    public static int getRootId(int userId) {
        String sql = "SELECT id FROM directory WHERE user_id=? AND parent_id IS NULL";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    public static void insert(int userId, String name, int parentId, String absPath) {
        String sql = "INSERT INTO directory(user_id, name, parent_id, abs_path, create_time) VALUES(?, ?, ?, ?, NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.setInt(3, parentId);
            ps.setString(4, absPath);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static String getPath(int dirId) {
        String sql = "SELECT abs_path FROM directory WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dirId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("abs_path");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public static String listAsString(int parentId) {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT id, name FROM directory WHERE parent_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, parentId);
            try (ResultSet rs = ps.executeQuery()) {
                List<String> dirs = new ArrayList<>();
                while (rs.next()) {
                    dirs.add(rs.getInt("id") + ":" + rs.getString("name"));
                }
                sb.append(String.join(",", dirs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return sb.toString();
    }
}
