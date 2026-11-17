package model;

public class PembayaranTransfer implements PembayaranInterface {
    public static final double BIAYA_ADMIN = 2_500.0;

    private final String bank;
    private final String noReferensi;

    public PembayaranTransfer(String bank, String noReferensi) {
        if (bank == null || bank.isBlank()) {
            throw new IllegalArgumentException("Bank tujuan wajib dipilih!");
        }
        if (noReferensi == null || !noReferensi.matches("\\d{6,20}")) {
            throw new IllegalArgumentException("No. referensi transfer harus 6-20 digit angka!");
        }
        this.bank = bank.trim();
        this.noReferensi = noReferensi.trim();
    }

    public String getBank() {
        return bank;
    }

    public String getNoReferensi() {
        return noReferensi;
    }

    @Override
    public String getMetode() {
        return "Transfer Bank (" + bank + ")";
    }

    @Override
    public double hitungTagihanAkhir(double tagihan) {
        return tagihan + BIAYA_ADMIN;
    }

    @Override
    public void prosesPembayaran(double tagihan, double nominalBayar) throws UangKurangException {
        double totalWajib = hitungTagihanAkhir(tagihan);
        if (nominalBayar < totalWajib) {
            double kurang = totalWajib - nominalBayar;
            throw new UangKurangException(
                String.format("Transfer ditolak: nominal kurang Rp %,.0f (termasuk biaya admin)", kurang), kurang);
        }
    }

    @Override
    public double getKembalian() {
        return 0.0;
    }
}

