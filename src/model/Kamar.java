package model;

public abstract class Kamar {
    private String idKamar;
    private double hargaDasar;
    private StatusKamar statusKamar;

    protected Kamar(String idKamar, double hargaDasar, StatusKamar statusKamar) {
        setIdKamar(idKamar);
        setHargaDasar(hargaDasar);
        setStatusKamar(statusKamar);
    }

    public final void setIdKamar(String idKamar) {
        if (idKamar == null || !idKamar.matches("[A-Z]{3}-\\d{3}")) {
            throw new IllegalArgumentException("ID kamar harus berformat XXX-123, contoh: DLX-201");
        }
        this.idKamar = idKamar;
    }

    public final void setHargaDasar(double hargaDasar) {
        if (hargaDasar < 0) {
            throw new IllegalArgumentException("Harga kamar tidak boleh bernilai negatif!");
        }
        this.hargaDasar = hargaDasar;
    }

    public final void setStatusKamar(StatusKamar statusKamar) {
        if (statusKamar == null) {
            throw new IllegalArgumentException("Status kamar wajib diisi!");
        }
        this.statusKamar = statusKamar;
    }

    public String getIdKamar() {
        return idKamar;
    }

    public double getHargaDasar() {
        return hargaDasar;
    }

    public StatusKamar getStatusKamar() {
        return statusKamar;
    }

    protected void validasiDurasi(int durasi) {
        if (durasi <= 0) {
            throw new IllegalArgumentException("Durasi inap minimal 1 malam!");
        }
    }

    public abstract String getTipeKamar();
    public abstract String getFasilitas();
    public abstract double hitungTarif(int durasi);

    @Override
    public String toString() {
        return String.format("%s - %s (Rp %,.0f/malam)", idKamar, getTipeKamar(), hargaDasar);
    }
}

