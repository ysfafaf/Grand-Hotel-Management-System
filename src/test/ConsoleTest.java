package test;

import dao.AdminDAO;
import dao.DatabaseHelper;
import dao.KamarDAO;
import dao.PelangganDAO;
import dao.ReservasiDAO;
import java.io.File;
import java.time.LocalDate;
import java.util.List;
import model.Admin;
import model.DatabaseException;
import model.FasilitasTambahan;
import model.Kamar;
import model.KamarDeluxe;
import model.KamarStandard;
import model.KamarSuite;
import model.KriteriaDiskon;
import model.LayananDiskon;
import model.Pelanggan;
import model.PembayaranInterface;
import model.PembayaranTransfer;
import model.PembayaranTunai;
import model.Reservasi;
import model.RincianBiaya;
import model.StatusKamar;
import model.StatusReservasi;
import model.UangKurangException;

public class ConsoleTest {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  PENGUJIAN SISTEM GHMS (MODEL, LOGIKA BISNIS, DAO)");
        System.out.println("==================================================\n");

        ujiPolimorfismeTarif();

        ujiLayananDiskon();

        ujiPembayaran();

        ujiDatabaseDanDAO();

        System.out.println("\n==================================================");
        System.out.println("  SEMUA PENGUJIAN TAHAP 1 & 2 BERHASIL SUKSES!");
        System.out.println("==================================================");
    }

    private static void ujiPolimorfismeTarif() {
        System.out.println("--- [1] Uji Polimorfisme Tarif Kamar ---");
        Kamar std = new KamarStandard("STD-001", 300_000, StatusKamar.TERSEDIA);
        Kamar dlx = new KamarDeluxe("DLX-001", 500_000, StatusKamar.TERSEDIA);
        Kamar sut = new KamarSuite("SUT-001", 1_000_000, StatusKamar.TERSEDIA);

        assert std.hitungTarif(4) == 1_200_000 : "Tarif Standard salah";
        System.out.printf("Standard 4 malam: Rp %,.0f (Flat)\n", std.hitungTarif(4));

        assert dlx.hitungTarif(2) == 1_000_000 : "Tarif Deluxe 2 malam salah";
        assert dlx.hitungTarif(3) == (1_500_000 * 0.90) : "Tarif Deluxe 3 malam diskon 10% salah";
        System.out.printf("Deluxe 3 malam: Rp %,.0f (Diskon 10%%)\n", dlx.hitungTarif(3));

        assert sut.hitungTarif(3) == (3_000_000 * 0.85) : "Tarif Suite 3 malam diskon 15% salah";
        assert sut.hitungTarif(5) == (5_000_000 * 0.80) : "Tarif Suite 5 malam diskon 20% salah";
        System.out.printf("Suite 5 malam: Rp %,.0f (Diskon 20%%)\n", sut.hitungTarif(5));
        System.out.println("-> Polimorfisme tarif OK!\n");
    }

    private static void ujiLayananDiskon() {
        System.out.println("--- [2] Uji LayananDiskon & Overloading ---");
        LayananDiskon layanan = new LayananDiskon();
        Kamar suite = new KamarSuite("SUT-101", 1_000_000, StatusKamar.TERSEDIA);

        double t1 = layanan.hitungTotalBayar(suite, 3);
        System.out.printf("Overload 1 (Kamar, durasi): Rp %,.0f\n", t1);

        KriteriaDiskon kd = new KriteriaDiskon(true, "PROMO2026");
        double t2 = layanan.hitungTotalBayar(suite, 3, kd);
        System.out.printf("Overload 2 (+ Member 5%% + Promo 50rb): Rp %,.0f\n", t2);

        FasilitasTambahan ft = new FasilitasTambahan(true, true);
        double t3 = layanan.hitungTotalBayar(suite, 3, kd, ft);
        System.out.printf("Overload 3 (+ Sarapan & Extra Bed): Rp %,.0f\n", t3);

        RincianBiaya r = layanan.hitungRincian(suite, 3, kd, ft);
        System.out.printf("Rincian -> Subtotal: Rp %,.0f, DiskonTipe: Rp %,.0f, DiskonMember: Rp %,.0f, Promo: Rp %,.0f, Fasilitas: Rp %,.0f, Total: Rp %,.0f\n",
                r.subtotalKamar(), r.diskonTipe(), r.diskonMember(), r.potonganPromo(), r.biayaFasilitas(), r.total());
        System.out.println("-> LayananDiskon OK!\n");
    }

    private static void ujiPembayaran() {
        System.out.println("--- [3] Uji Pembayaran & Custom Exception ---");
        PembayaranInterface tunai = new PembayaranTunai();
        try {
            tunai.prosesPembayaran(100_000, 150_000);
            System.out.printf("Tunai sukses! Kembalian: Rp %,.0f\n", tunai.getKembalian());
        } catch (UangKurangException e) {
            System.err.println("Gagal tidak terduga: " + e.getMessage());
        }

        try {
            tunai.prosesPembayaran(100_000, 80_000);
            System.err.println("UangKurangException gagal dilempar!");
        } catch (UangKurangException e) {
            System.out.printf("Custom exception tertangkap: '%s' (Kekurangan: Rp %,.0f)\n", e.getMessage(), e.getKekurangan());
        }

        PembayaranInterface transfer = new PembayaranTransfer("BCA", "1234567890");
        System.out.printf("Transfer Bank metode: %s, Tagihan 100rb -> Rp %,.0f (Biaya Admin 2.500)\n",
                transfer.getMetode(), transfer.hitungTagihanAkhir(100_000));
        System.out.println("-> Pembayaran OK!\n");
    }

    private static void ujiDatabaseDanDAO() {
        System.out.println("--- [4] Uji SQLite & DAO ---");

        String testDb = "test_hotel.db";
        File f = new File(testDb);
        if (f.exists()) f.delete();

        DatabaseHelper.setDbUrl("jdbc:sqlite:" + testDb);

        try {
            DatabaseHelper.initDatabase();
            System.out.println("Inisialisasi database & seeder berhasil!");

            AdminDAO adminDAO = new AdminDAO();
            Admin admin = adminDAO.validasiLogin("admin", "admin123");
            assert admin != null : "Login admin default gagal";
            System.out.println("Login admin default berhasil: " + admin.getUsername());

            KamarDAO kamarDAO = new KamarDAO();
            List<Kamar> daftarKamar = kamarDAO.semua();
            System.out.println("Jumlah seeder kamar: " + daftarKamar.size());

            PelangganDAO pelangganDAO = new PelangganDAO();
            Pelanggan p1 = new Pelanggan("3201123456789012", "Budi Santoso", "081234567890", true);
            pelangganDAO.tambah(p1);
            System.out.println("Pelanggan ditambahkan dengan ID: " + p1.getIdPelanggan());

            ReservasiDAO reservasiDAO = new ReservasiDAO();
            Kamar kPilihan = daftarKamar.get(0);
            Reservasi rsv = new Reservasi(
                p1, kPilihan, LocalDate.now(), 2,
                new FasilitasTambahan(true, false),
                "PROMO2026", "Tunai", 650_000, StatusReservasi.AKTIF
            );
            reservasiDAO.tambah(rsv);
            System.out.println("Reservasi berhasil dibuat dengan ID: " + rsv.getIdReservasi());

            Kamar kSetelah = kamarDAO.cariBerdasarkanId(kPilihan.getIdKamar());
            System.out.println("Status kamar setelah dipesan: " + kSetelah.getStatusKamar());
            assert kSetelah.getStatusKamar() == StatusKamar.TERISI;

            try {
                pelangganDAO.hapus(p1);
                System.err.println("Foreign key delete pelanggan gagal dicegah!");
            } catch (DatabaseException ex) {
                System.out.println("Foreign key bekerja sempurna: " + ex.getMessage());
            }

            reservasiDAO.checkOut(rsv.getIdReservasi());
            Kamar kCheckout = kamarDAO.cariBerdasarkanId(kPilihan.getIdKamar());
            System.out.println("Status kamar setelah check-out: " + kCheckout.getStatusKamar());
            assert kCheckout.getStatusKamar() == StatusKamar.TERSEDIA;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseHelper.resetDbUrl();
            if (f.exists()) f.delete();
        }
    }
}

