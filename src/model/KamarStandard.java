package model;

public class KamarStandard extends Kamar {

    public KamarStandard(String id, double harga, StatusKamar status) {
        super(id, harga, status);
    }

    @Override
    public String getTipeKamar() {
        return "Standard";
    }

    @Override
    public String getFasilitas() {
        return "AC, TV, Wi-Fi";
    }

    @Override
    public double hitungTarif(int durasi) {
        validasiDurasi(durasi);
        return getHargaDasar() * durasi;
    }
}

