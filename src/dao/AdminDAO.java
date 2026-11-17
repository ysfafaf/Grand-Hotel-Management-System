package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Admin;
import model.DatabaseException;
import util.PasswordUtil;

public class AdminDAO {

    public Admin validasiLogin(String username, String plainPassword) throws DatabaseException {
        if (username == null || plainPassword == null || username.isBlank() || plainPassword.isBlank()) {
            return null;
        }

        Admin admin = cariBerdasarkanUsername(username.trim());
        if (admin != null) {
            if (PasswordUtil.checkPassword(plainPassword, admin.getPassword())) {
                return admin;
            }
        }
        return null;
    }

    public Admin cariBerdasarkanUsername(String username) throws DatabaseException {
        if (username == null || username.isBlank()) {
            return null;
        }
        String sql = "SELECT id_admin, username, password FROM admin WHERE username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Admin(
                        rs.getInt("id_admin"),
                        rs.getString("username"),
                        rs.getString("password")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Gagal mencari data admin: " + e.getMessage(), e);
        }
        return null;
    }

    public void tambah(Admin admin) throws DatabaseException {
        if (admin == null) {
            throw new IllegalArgumentException("Data admin tidak boleh null!");
        }
        String sql = "INSERT INTO admin (username, password) VALUES (?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, admin.getUsername());
            ps.setString(2, admin.getPassword());
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                throw new DatabaseException("Username '" + admin.getUsername() + "' sudah digunakan!", e);
            }
            throw new DatabaseException("Gagal menambahkan admin: " + e.getMessage(), e);
        }
    }

    public List<Admin> semua() throws DatabaseException {
        List<Admin> daftar = new ArrayList<>();
        String sql = "SELECT id_admin, username, password FROM admin ORDER BY id_admin";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                daftar.add(new Admin(
                    rs.getInt("id_admin"),
                    rs.getString("username"),
                    rs.getString("password")
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Gagal memuat daftar admin: " + e.getMessage(), e);
        }
        return daftar;
    }
}

