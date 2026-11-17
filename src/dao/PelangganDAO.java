package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.DatabaseException;
import model.Pelanggan;

public class PelangganDAO implements CrudDAO<Pelanggan> {

    @Override
    public void tambah(Pelanggan p) throws DatabaseException {
        if (p == null) {
            throw new IllegalArgumentException("Data pelanggan tidak boleh null!");
        }
        String sql = "INSERT INTO pelanggan (nik, nama_pelanggan, no_telepon, is_member) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNik());
            ps.setString(2, p.getNama());
            ps.setString(3, p.getNoTelepon());
            ps.setInt(4, p.isMember() ? 1 : 0);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setIdPelanggan(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                throw new DatabaseException("NIK '" + p.getNik() + "' sudah terdaftar untuk pelanggan lain!", e);
            }
            throw new DatabaseException("Gagal menambahkan pelanggan: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Pelanggan> semua() throws DatabaseException {
        List<Pelanggan> daftar = new ArrayList<>();
        String sql = "SELECT id_pelanggan, nik, nama_pelanggan, no_telepon, is_member FROM pelanggan ORDER BY nama_pelanggan";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                daftar.add(new Pelanggan(
                    rs.getInt("id_pelanggan"),
                    rs.getString("nik"),
                    rs.getString("nama_pelanggan"),
                    rs.getString("no_telepon"),
                    rs.getInt("is_member") == 1
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Gagal memuat data pelanggan: " + e.getMessage(), e);
        }
        return daftar;
    }

    public Pelanggan cariBerdasarkanId(int idPelanggan) throws DatabaseException {
        String sql = "SELECT id_pelanggan, nik, nama_pelanggan, no_telepon, is_member FROM pelanggan WHERE id_pelanggan = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPelanggan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Pelanggan(
                        rs.getInt("id_pelanggan"),
                        rs.getString("nik"),
                        rs.getString("nama_pelanggan"),
                        rs.getString("no_telepon"),
                        rs.getInt("is_member") == 1
                    );
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Gagal mencari pelanggan ID " + idPelanggan + ": " + e.getMessage(), e);
        }
        return null;
    }

    public Pelanggan cariBerdasarkanNik(String nik) throws DatabaseException {
        if (nik == null || nik.isBlank()) {
            return null;
        }
        String sql = "SELECT id_pelanggan, nik, nama_pelanggan, no_telepon, is_member FROM pelanggan WHERE nik = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nik.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Pelanggan(
                        rs.getInt("id_pelanggan"),
                        rs.getString("nik"),
                        rs.getString("nama_pelanggan"),
                        rs.getString("no_telepon"),
                        rs.getInt("is_member") == 1
                    );
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Gagal mencari pelanggan NIK " + nik + ": " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void ubah(Pelanggan p) throws DatabaseException {
        if (p == null) {
            throw new IllegalArgumentException("Data pelanggan tidak boleh null!");
        }
        String sql = "UPDATE pelanggan SET nik = ?, nama_pelanggan = ?, no_telepon = ?, is_member = ? WHERE id_pelanggan = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNik());
            ps.setString(2, p.getNama());
            ps.setString(3, p.getNoTelepon());
            ps.setInt(4, p.isMember() ? 1 : 0);
            ps.setInt(5, p.getIdPelanggan());
            int baris = ps.executeUpdate();
            if (baris == 0) {
                throw new DatabaseException("Pelanggan dengan ID " + p.getIdPelanggan() + " tidak ditemukan.");
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                throw new DatabaseException("NIK '" + p.getNik() + "' sudah terdaftar untuk pelanggan lain!", e);
            }
            throw new DatabaseException("Gagal memperbarui data pelanggan: " + e.getMessage(), e);
        }
    }

    @Override
    public void hapus(Pelanggan p) throws DatabaseException {
        if (p == null) {
            throw new IllegalArgumentException("Data pelanggan tidak boleh null!");
        }
        String sql = "DELETE FROM pelanggan WHERE id_pelanggan = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getIdPelanggan());
            int baris = ps.executeUpdate();
            if (baris == 0) {
                throw new DatabaseException("Pelanggan dengan ID " + p.getIdPelanggan() + " tidak ditemukan.");
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && (e.getMessage().contains("FOREIGN KEY") || e.getErrorCode() == 19)) {
                throw new DatabaseException("Pelanggan tidak dapat dihapus karena sudah memiliki riwayat reservasi.", e);
            }
            throw new DatabaseException("Gagal menghapus data pelanggan: " + e.getMessage(), e);
        }
    }
}

