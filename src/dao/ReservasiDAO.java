package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.DatabaseException;
import model.FasilitasTambahan;
import model.Kamar;
import model.KamarFactory;
import model.Pelanggan;
import model.Reservasi;
import model.StatusKamar;
import model.StatusReservasi;

public class ReservasiDAO implements CrudDAO<Reservasi> {

    @Override
    public void tambah(Reservasi r) throws DatabaseException {
        if (r == null) {
            throw new IllegalArgumentException("Data reservasi tidak boleh null!");
        }

        String sqlInsert = """
            INSERT INTO reservasi (
                id_reservasi, id_pelanggan, id_kamar, tanggal_checkin,
                durasi_inap, sarapan, extra_bed, kode_promo,
                metode_bayar, total_bayar, status_reservasi
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        String sqlUpdateKamar = "UPDATE kamar SET status_kamar = 'TERISI' WHERE id_kamar = ?";

        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false);

            String idReservasi = r.getIdReservasi();
            if (idReservasi == null || idReservasi.isBlank()) {
                idReservasi = buatIdBerikutnya(conn);
                r.setIdReservasi(idReservasi);
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                ps.setString(1, idReservasi);
                ps.setInt(2, r.getPelanggan().getIdPelanggan());
                ps.setString(3, r.getKamar().getIdKamar());
                ps.setString(4, r.getTanggalCheckin().toString());
                ps.setInt(5, r.getDurasiInap());
                ps.setInt(6, r.getFasilitas().isSarapan() ? 1 : 0);
                ps.setInt(7, r.getFasilitas().isExtraBed() ? 1 : 0);
                ps.setString(8, r.getKodePromo());
                ps.setString(9, r.getMetodeBayar());
                ps.setDouble(10, r.getTotalBayar());
                ps.setString(11, r.getStatus().name());
                ps.executeUpdate();
            }

            try (PreparedStatement psKamar = conn.prepareStatement(sqlUpdateKamar)) {
                psKamar.setString(1, r.getKamar().getIdKamar());
                psKamar.executeUpdate();
            }

            conn.commit();
            r.getKamar().setStatusKamar(StatusKamar.TERISI);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            throw new DatabaseException("Gagal menyimpan reservasi: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {

                }
            }
        }
    }

    private String buatIdBerikutnya(Connection conn) throws SQLException {
        String sql = "SELECT id_reservasi FROM reservasi ORDER BY id_reservasi DESC LIMIT 1";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String lastId = rs.getString(1);
                if (lastId != null && lastId.startsWith("RSV-")) {
                    try {
                        int num = Integer.parseInt(lastId.substring(4));
                        return String.format("RSV-%04d", num + 1);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return "RSV-0001";
    }

    @Override
    public List<Reservasi> semua() throws DatabaseException {
        List<Reservasi> daftar = new ArrayList<>();
        String sql = """
            SELECT r.id_reservasi, r.tanggal_checkin, r.durasi_inap, r.sarapan, r.extra_bed,
                   r.kode_promo, r.metode_bayar, r.total_bayar, r.status_reservasi,
                   p.id_pelanggan, p.nik, p.nama_pelanggan, p.no_telepon, p.is_member,
                   k.id_kamar, k.tipe_kamar, k.harga_dasar, k.status_kamar
            FROM reservasi r
            JOIN pelanggan p ON r.id_pelanggan = p.id_pelanggan
            JOIN kamar k ON r.id_kamar = k.id_kamar
            ORDER BY r.id_reservasi DESC
            """;

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Pelanggan pelanggan = new Pelanggan(
                    rs.getInt("id_pelanggan"),
                    rs.getString("nik"),
                    rs.getString("nama_pelanggan"),
                    rs.getString("no_telepon"),
                    rs.getInt("is_member") == 1
                );

                Kamar kamar = KamarFactory.buat(
                    rs.getString("tipe_kamar"),
                    rs.getString("id_kamar"),
                    rs.getDouble("harga_dasar"),
                    StatusKamar.valueOf(rs.getString("status_kamar"))
                );

                FasilitasTambahan fasilitas = new FasilitasTambahan(
                    rs.getInt("sarapan") == 1,
                    rs.getInt("extra_bed") == 1
                );

                Reservasi r = new Reservasi(
                    rs.getString("id_reservasi"),
                    pelanggan,
                    kamar,
                    LocalDate.parse(rs.getString("tanggal_checkin")),
                    rs.getInt("durasi_inap"),
                    fasilitas,
                    rs.getString("kode_promo"),
                    rs.getString("metode_bayar"),
                    rs.getDouble("total_bayar"),
                    StatusReservasi.valueOf(rs.getString("status_reservasi"))
                );
                daftar.add(r);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Gagal memuat data riwayat reservasi: " + e.getMessage(), e);
        }
        return daftar;
    }

    public void checkOut(String idReservasi) throws DatabaseException {
        if (idReservasi == null || idReservasi.isBlank()) {
            throw new IllegalArgumentException("ID Reservasi tidak boleh kosong!");
        }

        String sqlCariKamar = "SELECT id_kamar, status_reservasi FROM reservasi WHERE id_reservasi = ?";
        String sqlUpdateReservasi = "UPDATE reservasi SET status_reservasi = 'SELESAI' WHERE id_reservasi = ?";
        String sqlUpdateKamar = "UPDATE kamar SET status_kamar = 'TERSEDIA' WHERE id_kamar = ?";

        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false);

            String idKamar = null;
            try (PreparedStatement psCari = conn.prepareStatement(sqlCariKamar)) {
                psCari.setString(1, idReservasi);
                try (ResultSet rs = psCari.executeQuery()) {
                    if (!rs.next()) {
                        throw new DatabaseException("Reservasi " + idReservasi + " tidak ditemukan!");
                    }
                    if ("SELESAI".equalsIgnoreCase(rs.getString("status_reservasi"))) {
                        throw new DatabaseException("Reservasi " + idReservasi + " sudah selesai (sudah check-out sebelumnya).");
                    }
                    idKamar = rs.getString("id_kamar");
                }
            }

            try (PreparedStatement psRes = conn.prepareStatement(sqlUpdateReservasi)) {
                psRes.setString(1, idReservasi);
                psRes.executeUpdate();
            }

            try (PreparedStatement psKam = conn.prepareStatement(sqlUpdateKamar)) {
                psKam.setString(1, idKamar);
                psKam.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            throw new DatabaseException("Gagal melakukan proses check-out: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public void ubah(Reservasi r) throws DatabaseException {
        if (r == null || r.getIdReservasi() == null) {
            throw new IllegalArgumentException("Data reservasi tidak valid untuk diperbarui!");
        }
        String sql = """
            UPDATE reservasi SET
                id_pelanggan = ?, id_kamar = ?, tanggal_checkin = ?,
                durasi_inap = ?, sarapan = ?, extra_bed = ?,
                kode_promo = ?, metode_bayar = ?, total_bayar = ?, status_reservasi = ?
            WHERE id_reservasi = ?
            """;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getPelanggan().getIdPelanggan());
            ps.setString(2, r.getKamar().getIdKamar());
            ps.setString(3, r.getTanggalCheckin().toString());
            ps.setInt(4, r.getDurasiInap());
            ps.setInt(5, r.getFasilitas().isSarapan() ? 1 : 0);
            ps.setInt(6, r.getFasilitas().isExtraBed() ? 1 : 0);
            ps.setString(7, r.getKodePromo());
            ps.setString(8, r.getMetodeBayar());
            ps.setDouble(9, r.getTotalBayar());
            ps.setString(10, r.getStatus().name());
            ps.setString(11, r.getIdReservasi());
            int baris = ps.executeUpdate();
            if (baris == 0) {
                throw new DatabaseException("Reservasi " + r.getIdReservasi() + " tidak ditemukan.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Gagal memperbarui data reservasi: " + e.getMessage(), e);
        }
    }

    @Override
    public void hapus(Reservasi r) throws DatabaseException {
        if (r == null || r.getIdReservasi() == null) {
            throw new IllegalArgumentException("Data reservasi tidak valid untuk dihapus!");
        }
        String sql = "DELETE FROM reservasi WHERE id_reservasi = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getIdReservasi());
            int baris = ps.executeUpdate();
            if (baris == 0) {
                throw new DatabaseException("Reservasi " + r.getIdReservasi() + " tidak ditemukan.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Gagal menghapus reservasi: " + e.getMessage(), e);
        }
    }
}

