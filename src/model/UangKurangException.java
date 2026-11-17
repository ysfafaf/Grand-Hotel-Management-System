package model;

public class UangKurangException extends Exception {
    private final double kekurangan;

    public UangKurangException(String pesan, double kekurangan) {
        super(pesan);
        this.kekurangan = kekurangan;
    }

    public double getKekurangan() {
        return kekurangan;
    }
}

