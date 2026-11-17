package model;

public class FasilitasTambahan {
    public static final double TARIF_SARAPAN = 50_000.0;
    public static final double TARIF_EXTRA_BED = 100_000.0;

    private final boolean sarapan;
    private final boolean extraBed;

    public FasilitasTambahan(boolean sarapan, boolean extraBed) {
        this.sarapan = sarapan;
        this.extraBed = extraBed;
    }

    public boolean isSarapan() {
        return sarapan;
    }

    public boolean isExtraBed() {
        return extraBed;
    }

    public double hitungBiaya(int durasi) {
        if (durasi <= 0) {
            throw new IllegalArgumentException("Durasi inap minimal 1 malam!");
        }
        double perMalam = (sarapan ? TARIF_SARAPAN : 0.0) + (extraBed ? TARIF_EXTRA_BED : 0.0);
        return perMalam * durasi;
    }
}

