package model;

public class Pelanggan {
    private int idPelanggan;
    private String nik;
    private String nama;
    private String noTelepon;
    private boolean member;

    public Pelanggan(String nik, String nama, String noTelepon, boolean member) {
        setNik(nik);
        setNama(nama);
        setNoTelepon(noTelepon);
        this.member = member;
    }

    public Pelanggan(int idPelanggan, String nik, String nama, String noTelepon, boolean member) {
        this(nik, nama, noTelepon, member);
        this.idPelanggan = idPelanggan;
    }

    public void setNik(String nik) {
        if (nik == null || !nik.matches("\\d{16}")) {
            throw new IllegalArgumentException("NIK harus terdiri dari 16 digit angka!");
        }
        this.nik = nik;
    }

    public void setNama(String nama) {
        if (nama == null || nama.isBlank()) {
            throw new IllegalArgumentException("Nama pelanggan tidak boleh kosong!");
        }
        this.nama = nama.trim();
    }

    public void setNoTelepon(String noTelepon) {
        if (noTelepon == null || !noTelepon.matches("\\d{10,13}")) {
            throw new IllegalArgumentException("Nomor telepon harus 10-13 digit angka!");
        }
        this.noTelepon = noTelepon;
    }

    public void setMember(boolean member) {
        this.member = member;
    }

    public void setIdPelanggan(int idPelanggan) {
        this.idPelanggan = idPelanggan;
    }

    public int getIdPelanggan() {
        return idPelanggan;
    }

    public String getNik() {
        return nik;
    }

    public String getNama() {
        return nama;
    }

    public String getNoTelepon() {
        return noTelepon;
    }

    public boolean isMember() {
        return member;
    }

    @Override
    public String toString() {
        return nama + " (" + nik + ")" + (member ? " [MEMBER]" : "");
    }
}

