package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import model.DatabaseException;
import util.PasswordUtil;

public final class DatabaseHelper {

    private static final String DEFAULT_DB_URL = "jdbc:sqlite:hotel_reservasi.db";
    private static String dbUrl = DEFAULT_DB_URL;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Peringatan: Driver SQLite JDBC tidak ditemukan di classpath: " + e.getMessage());
        }
    }

    private DatabaseHelper() {

    }

    public static void setDbUrl(String customUrl) {
        dbUrl = customUrl;
    }

    public static void resetDbUrl() {
        dbUrl = DEFAULT_DB_URL;
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(dbUrl);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    public static void initDatabase() throws DatabaseException {
        try (Connection conn = getConnection()) {
            buatTabel(conn);
            isiDataAwalAdmin(conn);
            isiDataAwalKamar(conn);
        } catch (SQLException e) {
            throw new DatabaseException("Gagal menginisialisasi basis data: " + e.getMessage(), e);
        }
    }

    private static void buatTabel(Connection conn) throws SQLException {
        String sqlAdmin = """
            CREATE TABLE IF NOT EXISTS admin (
                id_admin INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL
            );
            """;

        String sqlKamar = """
            CREATE TABLE IF NOT EXISTS kamar (
                id_kamar TEXT PRIMARY KEY,
                tipe_kamar TEXT NOT NULL CHECK (tipe_kamar IN ('Standard', 'Deluxe', 'Suite')),
                harga_dasar REAL NOT NULL CHECK (harga_dasar >= 0),
                status_kamar TEXT NOT NULL CHECK (status_kamar IN ('TERSEDIA', 'TERISI'))
            );
            """;

        String sqlPelanggan = """
            CREATE TABLE IF NOT EXISTS pelanggan (
                id_pelanggan INTEGER PRIMARY KEY AUTOINCREMENT,
                nik TEXT NOT NULL UNIQUE,
                nama_pelanggan TEXT NOT NULL,
                no_telepon TEXT NOT NULL,
                is_member INTEGER NOT NULL DEFAULT 0
            );
            """;

        String sqlReservasi = """
            CREATE TABLE IF NOT EXISTS reservasi (
                id_reservasi TEXT PRIMARY KEY,
                id_pelanggan INTEGER NOT NULL,
                id_kamar TEXT NOT NULL,
                tanggal_checkin TEXT NOT NULL,
                durasi_inap INTEGER NOT NULL CHECK (durasi_inap >= 1),
                sarapan INTEGER NOT NULL DEFAULT 0,
                extra_bed INTEGER NOT NULL DEFAULT 0,
                kode_promo TEXT,
                metode_bayar TEXT NOT NULL,
                total_bayar REAL NOT NULL,
                status_reservasi TEXT NOT NULL DEFAULT 'AKTIF' CHECK (status_reservasi IN ('AKTIF', 'SELESAI')),
                FOREIGN KEY (id_pelanggan) REFERENCES pelanggan(id_pelanggan) ON DELETE RESTRICT,
                FOREIGN KEY (id_kamar) REFERENCES kamar(id_kamar) ON DELETE RESTRICT
            );
            """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlAdmin);
            stmt.execute(sqlKamar);
            stmt.execute(sqlPelanggan);
            stmt.execute(sqlReservasi);
        }
    }

    private static void isiDataAwalAdmin(Connection conn) throws SQLException {
        String cekSql = "SELECT COUNT(*) FROM admin";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(cekSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = "INSERT INTO admin (username, password) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setString(1, "admin");
                    ps.setString(2, PasswordUtil.hashPassword("admin123"));
                    ps.executeUpdate();
                }
            }
        }
    }

    private static void isiDataAwalKamar(Connection conn) throws SQLException {
        String cekSql = "SELECT COUNT(*) FROM kamar";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(cekSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = "INSERT INTO kamar (id_kamar, tipe_kamar, harga_dasar, status_kamar) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    String[][] dataKamar = {
                        {"STD-101", "Standard", "350000", "TERSEDIA"},
                        {"STD-102", "Standard", "350000", "TERSEDIA"},
                        {"STD-103", "Standard", "350000", "TERSEDIA"},
                        {"DLX-201", "Deluxe",   "550000", "TERSEDIA"},
                        {"DLX-202", "Deluxe",   "550000", "TERSEDIA"},
                        {"DLX-203", "Deluxe",   "550000", "TERSEDIA"},
                        {"SUT-301", "Suite",    "1200000", "TERSEDIA"},
                        {"SUT-302", "Suite",    "1200000", "TERSEDIA"}
                    };

                    for (String[] k : dataKamar) {
                        ps.setString(1, k[0]);
                        ps.setString(2, k[1]);
                        ps.setDouble(3, Double.parseDouble(k[2]));
                        ps.setString(4, k[3]);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        }
    }
}
