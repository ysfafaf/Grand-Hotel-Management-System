package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.DatabaseException;
import model.Kamar;
import model.KamarFactory;
import model.StatusKamar;

public class KamarDAO implements CrudDAO<Kamar> {

    @Override
    public void tambah(Kamar kamar) throws DatabaseException {
        if (kamar == null) {
            throw new IllegalArgumentException("Data kamar tidak boleh null!");
        }
        String sql = "INSERT INTO kamar (id_kamar, tipe_kamar, harga_dasar, status_kamar) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kamar.getIdKamar());
            ps.setString(2, kamar.getTipeKamar());
            ps.setDouble(3, kamar.getHargaDasar());
            ps.setString(4, kamar.getStatusKamar().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                throw new DatabaseException("ID Kamar '" + kamar.getIdKamar() + "' sudah terdaftar!", e);
            }
            throw new DatabaseException("Gagal menambahkan data kamar: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Kamar> semua() throws DatabaseException {
        List<Kamar> daftar = new ArrayList<>();
        String sql = "SELECT id_kamar, tipe_kamar, harga_dasar, status_kamar FROM kamar ORDER BY id_kamar";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                daftar.add(KamarFactory.buat(
                    rs.getString("tipe_kamar"),
                    rs.getString("id_kamar"),
                    rs.getDouble("harga_dasar"),
                    StatusKamar.valueOf(rs.getString("status_kamar"))
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Gagal memuat data kamar: " + e.getMessage(), e);
        }
        return daftar;
    }

    public List<Kamar> semuaTersedia() throws DatabaseException {
        List<Kamar> daftar = new ArrayList<>();
        String sql = "SELECT id_kamar, tipe_kamar, harga_dasar, status_kamar FROM kamar WHERE status_kamar = 'TERSEDIA' ORDER BY id_kamar";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                daftar.add(KamarFactory.buat(
                    rs.getString("tipe_kamar"),
                    rs.getString("id_kamar"),
                    rs.getDouble("harga_dasar"),
                    StatusKamar.valueOf(rs.getString("status_kamar"))
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Gagal memuat daftar kamar tersedia: " + e.getMessage(), e);
        }
        return daftar;
    }

    public Kamar cariBerdasarkanId(String idKamar) throws DatabaseException {
        if (idKamar == null || idKamar.isBlank()) {
            return null;
        }
        String sql = "SELECT id_kamar, tipe_kamar, harga_dasar, status_kamar FROM kamar WHERE id_kamar = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idKamar);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return KamarFactory.buat(
                        rs.getString("tipe_kamar"),
                        rs.getString("id_kamar"),
                        rs.getDouble("harga_dasar"),
                        StatusKamar.valueOf(rs.getString("status_kamar"))
                    );
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Gagal mencari kamar dengan ID " + idKamar + ": " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void ubah(Kamar kamar) throws DatabaseException {
        if (kamar == null) {
            throw new IllegalArgumentException("Data kamar tidak boleh null!");
        }
        String sql = "UPDATE kamar SET tipe_kamar = ?, harga_dasar = ?, status_kamar = ? WHERE id_kamar = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kamar.getTipeKamar());
            ps.setDouble(2, kamar.getHargaDasar());
            ps.setString(3, kamar.getStatusKamar().name());
            ps.setString(4, kamar.getIdKamar());
            int baris = ps.executeUpdate();
            if (baris == 0) {
                throw new DatabaseException("Kamar dengan ID '" + kamar.getIdKamar() + "' tidak ditemukan.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Gagal memperbarui data kamar: " + e.getMessage(), e);
        }
    }

    @Override
    public void hapus(Kamar kamar) throws DatabaseException {
        if (kamar == null) {
            throw new IllegalArgumentException("Data kamar tidak boleh null!");
        }
        if (kamar.getStatusKamar() == StatusKamar.TERISI) {
            throw new DatabaseException("Kamar " + kamar.getIdKamar() + " sedang berstatus TERISI dan tidak boleh dihapus.");
        }

        String sql = "DELETE FROM kamar WHERE id_kamar = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kamar.getIdKamar());
            int baris = ps.executeUpdate();
            if (baris == 0) {
                throw new DatabaseException("Kamar dengan ID '" + kamar.getIdKamar() + "' tidak ditemukan.");
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && (e.getMessage().contains("FOREIGN KEY") || e.getErrorCode() == 19)) {
                throw new DatabaseException("Kamar tidak dapat dihapus karena sudah memiliki riwayat reservasi.", e);
            }
            throw new DatabaseException("Gagal menghapus kamar: " + e.getMessage(), e);
        }
    }

    public void ubahStatus(String idKamar, StatusKamar status, Connection conn) throws SQLException {
        String sql = "UPDATE kamar SET status_kamar = ? WHERE id_kamar = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, idKamar);
            ps.executeUpdate();
        }
    }
}

