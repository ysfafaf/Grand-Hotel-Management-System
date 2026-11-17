package model;

public interface PembayaranInterface {

    String getMetode();

    double hitungTagihanAkhir(double tagihan);

    void prosesPembayaran(double tagihan, double nominalBayar) throws UangKurangException;

    double getKembalian();
}

