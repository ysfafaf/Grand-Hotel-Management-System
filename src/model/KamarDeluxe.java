package model;

public class KamarDeluxe extends Kamar {

    public KamarDeluxe(String id, double harga, StatusKamar status) {
        super(id, harga, status);
    }

    @Override
    public String getTipeKamar() {
        return "Deluxe";
    }

    @Override
    public String getFasilitas() {
        return "AC, TV, Wi-Fi, Minibar, Balkon";
    }

    @Override
    public double hitungTarif(int durasi) {
        validasiDurasi(durasi);
        double total = getHargaDasar() * durasi;
        return (durasi >= 3) ? total * 0.90 : total;
    }
}

