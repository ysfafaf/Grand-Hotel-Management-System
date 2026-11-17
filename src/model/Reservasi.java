package model;

import java.time.LocalDate;

public class Reservasi {
    private String idReservasi;
    private Pelanggan pelanggan;
    private Kamar kamar;
    private LocalDate tanggalCheckin;
    private int durasiInap;
    private FasilitasTambahan fasilitas;
    private String kodePromo;
    private String metodeBayar;
    private double totalBayar;
    private StatusReservasi status;

    public Reservasi(String idReservasi, Pelanggan pelanggan, Kamar kamar, LocalDate tanggalCheckin,
                     int durasiInap, FasilitasTambahan fasilitas, String kodePromo,
                     String metodeBayar, double totalBayar, StatusReservasi status) {
        setIdReservasi(idReservasi);
        setPelanggan(pelanggan);
        setKamar(kamar);
        setTanggalCheckin(tanggalCheckin);
        setDurasiInap(durasiInap);
        this.fasilitas = (fasilitas != null) ? fasilitas : new FasilitasTambahan(false, false);
        this.kodePromo = kodePromo;
        setMetodeBayar(metodeBayar);
        setTotalBayar(totalBayar);
        setStatus(status);
    }

    public Reservasi(Pelanggan pelanggan, Kamar kamar, LocalDate tanggalCheckin,
                     int durasiInap, FasilitasTambahan fasilitas, String kodePromo,
                     String metodeBayar, double totalBayar, StatusReservasi status) {
        setPelanggan(pelanggan);
        setKamar(kamar);
        setTanggalCheckin(tanggalCheckin);
        setDurasiInap(durasiInap);
        this.fasilitas = (fasilitas != null) ? fasilitas : new FasilitasTambahan(false, false);
        this.kodePromo = kodePromo;
        setMetodeBayar(metodeBayar);
        setTotalBayar(totalBayar);
        setStatus(status);
    }

    public void setIdReservasi(String idReservasi) {
        if (idReservasi != null && !idReservasi.isBlank()) {
            this.idReservasi = idReservasi.trim();
        } else {
            this.idReservasi = null;
        }
    }

    public void setPelanggan(Pelanggan pelanggan) {
        if (pelanggan == null) {
            throw new IllegalArgumentException("Pelanggan wajib dipilih!");
        }
        this.pelanggan = pelanggan;
    }

    public void setKamar(Kamar kamar) {
        if (kamar == null) {
            throw new IllegalArgumentException("Kamar wajib dipilih!");
        }
        this.kamar = kamar;
    }

    public void setTanggalCheckin(LocalDate tanggalCheckin) {
        if (tanggalCheckin == null) {
            throw new IllegalArgumentException("Tanggal check-in wajib diisi!");
        }
        this.tanggalCheckin = tanggalCheckin;
    }

    public void setDurasiInap(int durasiInap) {
        if (durasiInap <= 0) {
            throw new IllegalArgumentException("Durasi inap minimal 1 malam!");
        }
        this.durasiInap = durasiInap;
    }

    public void setFasilitas(FasilitasTambahan fasilitas) {
        this.fasilitas = (fasilitas != null) ? fasilitas : new FasilitasTambahan(false, false);
    }

    public void setKodePromo(String kodePromo) {
        this.kodePromo = kodePromo;
    }

    public void setMetodeBayar(String metodeBayar) {
        if (metodeBayar == null || metodeBayar.isBlank()) {
            throw new IllegalArgumentException("Metode pembayaran wajib diisi!");
        }
        this.metodeBayar = metodeBayar.trim();
    }

    public void setTotalBayar(double totalBayar) {
        if (totalBayar < 0) {
            throw new IllegalArgumentException("Total bayar tidak boleh bernilai negatif!");
        }
        this.totalBayar = totalBayar;
    }

    public void setStatus(StatusReservasi status) {
        if (status == null) {
            throw new IllegalArgumentException("Status reservasi wajib diisi!");
        }
        this.status = status;
    }

    public String getIdReservasi() {
        return idReservasi;
    }

    public Pelanggan getPelanggan() {
        return pelanggan;
    }

    public Kamar getKamar() {
        return kamar;
    }

    public LocalDate getTanggalCheckin() {
        return tanggalCheckin;
    }

    public int getDurasiInap() {
        return durasiInap;
    }

    public FasilitasTambahan getFasilitas() {
        return fasilitas;
    }

    public String getKodePromo() {
        return kodePromo;
    }

    public String getMetodeBayar() {
        return metodeBayar;
    }

    public double getTotalBayar() {
        return totalBayar;
    }

    public StatusReservasi getStatus() {
        return status;
    }
}

