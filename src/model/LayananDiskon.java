package model;

import java.util.Collections;
import java.util.Map;

public class LayananDiskon {

    public static final double DISKON_MEMBER = 0.05;

    private static final Map<String, Double> DAFTAR_PROMO = Map.of(
        "PROMO2026", 50_000.0,
        "HEMAT100", 100_000.0
    );

    public static Map<String, Double> getDaftarPromo() {
        return Collections.unmodifiableMap(DAFTAR_PROMO);
    }

    public double hitungTotalBayar(Kamar kamar, int durasi) {
        return hitungRincian(kamar, durasi, null, null).total();
    }

    public double hitungTotalBayar(Kamar kamar, int durasi, KriteriaDiskon kriteria) {
        return hitungRincian(kamar, durasi, kriteria, null).total();
    }

    public double hitungTotalBayar(Kamar kamar, int durasi, KriteriaDiskon kriteria, FasilitasTambahan fasilitas) {
        return hitungRincian(kamar, durasi, kriteria, fasilitas).total();
    }

    public RincianBiaya hitungRincian(Kamar kamar, int durasi, KriteriaDiskon kriteria, FasilitasTambahan fasilitas) {
        if (kamar == null) {
            throw new IllegalArgumentException("Kamar tidak boleh null!");
        }
        if (durasi <= 0) {
            throw new IllegalArgumentException("Durasi inap minimal 1 malam!");
        }

        double tarifTipe = kamar.hitungTarif(durasi);
        double subtotal = kamar.getHargaDasar() * durasi;
        double diskonTipe = subtotal - tarifTipe;
        double diskonMember = 0.0;
        double potonganPromo = 0.0;

        if (kriteria != null) {
            if (kriteria.isMember()) {
                diskonMember = tarifTipe * DISKON_MEMBER;
            }
            String kode = kriteria.getKodePromo();
            if (kode != null && !kode.isBlank()) {
                Double nilai = DAFTAR_PROMO.get(kode.trim().toUpperCase());
                if (nilai == null) {
                    throw new IllegalArgumentException("Kode promo '" + kode + "' tidak dikenali!");
                }
                potonganPromo = Math.min(nilai, tarifTipe - diskonMember);
            }
        }

        double biayaFasilitas = (fasilitas == null) ? 0.0 : fasilitas.hitungBiaya(durasi);
        return new RincianBiaya(subtotal, diskonTipe, diskonMember, potonganPromo, biayaFasilitas);
    }
}

