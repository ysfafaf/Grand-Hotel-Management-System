package model;

public record RincianBiaya(
    double subtotalKamar,
    double diskonTipe,
    double diskonMember,
    double potonganPromo,
    double biayaFasilitas
) {

    public double total() {
        return subtotalKamar - diskonTipe - diskonMember - potonganPromo + biayaFasilitas;
    }
}

