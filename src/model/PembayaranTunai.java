package model;

public class PembayaranTunai implements PembayaranInterface {
    private double kembalian;

    @Override
    public String getMetode() {
        return "Tunai";
    }

    @Override
    public double hitungTagihanAkhir(double tagihan) {
        return tagihan;
    }

    @Override
    public void prosesPembayaran(double tagihan, double nominalBayar) throws UangKurangException {
        if (nominalBayar < tagihan) {
            double kurang = tagihan - nominalBayar;
            throw new UangKurangException(
                String.format("Pembayaran gagal: uang bayar kurang Rp %,.0f", kurang), kurang);
        }
        this.kembalian = nominalBayar - tagihan;
    }

    @Override
    public double getKembalian() {
        return kembalian;
    }
}

