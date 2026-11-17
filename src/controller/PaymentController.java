package controller;

import dao.ReservasiDAO;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import model.DatabaseException;
import model.PembayaranInterface;
import model.PembayaranTransfer;
import model.PembayaranTunai;
import model.Reservasi;
import model.RincianBiaya;
import model.UangKurangException;
import util.SceneNavigator;

public class PaymentController {

    @FXML private Label lblInfoTamu;
    @FXML private Label lblInfoKamar;
    @FXML private Label lblSubtotal;
    @FXML private Label lblDiskonTipe;
    @FXML private Label lblDiskonMember;
    @FXML private Label lblPotonganPromo;
    @FXML private Label lblBiayaFasilitas;
    @FXML private Label lblBiayaAdmin;
    @FXML private Label lblTotalTagihan;

    @FXML private RadioButton rdoTunai;
    @FXML private RadioButton rdoTransfer;
    @FXML private ToggleGroup tgMetode;

    @FXML private VBox boxTunai;
    @FXML private TextField txtNominalTunai;

    @FXML private VBox boxTransfer;
    @FXML private ComboBox<String> cmbBank;
    @FXML private TextField txtNoReferensi;
    @FXML private TextField txtNominalTransfer;

    @FXML private Button btnKonfirmasi;
    @FXML private TextArea txtStruk;
    @FXML private Button btnSimpanStruk;
    @FXML private Button btnKembaliDashboard;

    private Reservasi draftReservasi;
    private RincianBiaya rincianBiaya;
    private final ReservasiDAO reservasiDAO = new ReservasiDAO();
    private String teksStrukTerakhir = "";

    @FXML
    private void initialize() {
        cmbBank.setItems(FXCollections.observableArrayList("BCA", "Mandiri", "BNI", "BRI"));
        cmbBank.setValue("BCA");

        tgMetode.selectedToggleProperty().addListener((obs, lama, baru) -> {
            boolean isTunai = rdoTunai.isSelected();
            boxTunai.setVisible(isTunai);
            boxTunai.setManaged(isTunai);
            boxTransfer.setVisible(!isTunai);
            boxTransfer.setManaged(!isTunai);
            perbaruiTampilanBiaya();
        });

        boxTransfer.setVisible(false);
        boxTransfer.setManaged(false);
        btnSimpanStruk.setDisable(true);
    }

    public void inisialisasiData(Reservasi draft, RincianBiaya rincian) {
        this.draftReservasi = draft;
        this.rincianBiaya = rincian;

        lblInfoTamu.setText(draft.getPelanggan().getNama() + " (" + draft.getPelanggan().getNik() + ")");
        lblInfoKamar.setText(draft.getKamar().getIdKamar() + " - " + draft.getKamar().getTipeKamar() +
                " (" + draft.getDurasiInap() + " malam)");

        lblSubtotal.setText(String.format("Rp %,.0f", rincian.subtotalKamar()));
        lblDiskonTipe.setText(String.format("-Rp %,.0f", rincian.diskonTipe()));
        lblDiskonMember.setText(String.format("-Rp %,.0f", rincian.diskonMember()));
        lblPotonganPromo.setText(String.format("-Rp %,.0f", rincian.potonganPromo()));
        lblBiayaFasilitas.setText(String.format("+Rp %,.0f", rincian.biayaFasilitas()));

        perbaruiTampilanBiaya();
    }

    private void perbaruiTampilanBiaya() {
        if (rincianBiaya == null) return;
        boolean isTransfer = rdoTransfer.isSelected();
        double biayaAdmin = isTransfer ? PembayaranTransfer.BIAYA_ADMIN : 0.0;
        double totalAkhir = rincianBiaya.total() + biayaAdmin;

        lblBiayaAdmin.setText(isTransfer ? String.format("+Rp %,.0f", biayaAdmin) : "Rp 0");
        lblTotalTagihan.setText(String.format("Rp %,.0f", totalAkhir));
    }

    @FXML
    private void onKonfirmasiPembayaran() {
        try {
            PembayaranInterface metode;
            double nominalBayar;

            if (rdoTunai.isSelected()) {
                metode = new PembayaranTunai();
                if (txtNominalTunai.getText() == null || txtNominalTunai.getText().isBlank()) {
                    throw new IllegalArgumentException("Nominal bayar tunai wajib diisi!");
                }
                nominalBayar = Double.parseDouble(txtNominalTunai.getText().trim());
            } else {
                String bank = cmbBank.getValue();
                String ref = txtNoReferensi.getText();
                metode = new PembayaranTransfer(bank, ref);
                if (txtNominalTransfer.getText() == null || txtNominalTransfer.getText().isBlank()) {
                    throw new IllegalArgumentException("Nominal transfer wajib diisi!");
                }
                nominalBayar = Double.parseDouble(txtNominalTransfer.getText().trim());
            }

            metode.prosesPembayaran(rincianBiaya.total(), nominalBayar);

            draftReservasi.setMetodeBayar(metode.getMetode());
            draftReservasi.setTotalBayar(metode.hitungTagihanAkhir(rincianBiaya.total()));
            reservasiDAO.tambah(draftReservasi);

            teksStrukTerakhir = susunStruk(metode, nominalBayar);
            txtStruk.setText(teksStrukTerakhir);

            btnKonfirmasi.setDisable(true);
            btnSimpanStruk.setDisable(false);

            SceneNavigator.tampilkanAlert(
                AlertType.INFORMATION,
                "Pembayaran Berhasil",
                "Reservasi " + draftReservasi.getIdReservasi() + " Sukses!",
                "Pembayaran telah diterima dan kamar berhasil direservasi."
            );

        } catch (NumberFormatException e) {
            SceneNavigator.tampilkanAlert(AlertType.ERROR, "Input Gagal", "Nominal Tidak Valid", "Nominal pembayaran harus berupa angka!");
        } catch (UangKurangException e) {
            SceneNavigator.tampilkanAlert(AlertType.ERROR, "Pembayaran Kurang", "Uang Pembayaran Tidak Cukup", e.getMessage());
        } catch (IllegalArgumentException | DatabaseException e) {
            SceneNavigator.tampilkanAlert(AlertType.ERROR, "Kesalahan", "Gagal Memproses Transaksi", e.getMessage());
        }
    }

    private String susunStruk(PembayaranInterface metode, double nominalBayar) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        sb.append("================================================================\n");
        sb.append("                 GRAND HOTEL MANAGEMENT SYSTEM                  \n");
        sb.append("               STRUK BUKTI RESERVASI DAN PEMBAYARAN             \n");
        sb.append("================================================================\n");
        sb.append(String.format("ID Reservasi   : %s\n", draftReservasi.getIdReservasi()));
        sb.append(String.format("Tanggal Checkin: %s (%d Malam)\n", draftReservasi.getTanggalCheckin().format(dtf), draftReservasi.getDurasiInap()));
        sb.append(String.format("Nama Tamu      : %s (NIK: %s)\n", draftReservasi.getPelanggan().getNama(), draftReservasi.getPelanggan().getNik()));
        sb.append(String.format("Kamar          : %s - %s\n", draftReservasi.getKamar().getIdKamar(), draftReservasi.getKamar().getTipeKamar()));
        sb.append(String.format("Fasilitas Tamb.: Sarapan (%s) | Extra Bed (%s)\n",
                draftReservasi.getFasilitas().isSarapan() ? "Ya" : "Tidak",
                draftReservasi.getFasilitas().isExtraBed() ? "Ya" : "Tidak"));
        sb.append("----------------------------------------------------------------\n");
        sb.append(String.format("Subtotal Tarif Kamar   : Rp %,12.0f\n", rincianBiaya.subtotalKamar()));
        sb.append(String.format("Diskon Durasi / Tipe   : -Rp%,12.0f\n", rincianBiaya.diskonTipe()));
        sb.append(String.format("Diskon Member (5%%)     : -Rp%,12.0f\n", rincianBiaya.diskonMember()));
        sb.append(String.format("Potongan Promo         : -Rp%,12.0f\n", rincianBiaya.potonganPromo()));
        sb.append(String.format("Biaya Fasilitas Tambah : +Rp%,12.0f\n", rincianBiaya.biayaFasilitas()));
        if (metode instanceof PembayaranTransfer) {
            sb.append(String.format("Biaya Admin Transfer   : +Rp%,12.0f\n", PembayaranTransfer.BIAYA_ADMIN));
        }
        sb.append("----------------------------------------------------------------\n");
        sb.append(String.format("TOTAL TAGIHAN AKHIR    : Rp %,12.0f\n", draftReservasi.getTotalBayar()));
        sb.append(String.format("Metode Pembayaran      : %s\n", metode.getMetode()));
        sb.append(String.format("Nominal Diterima       : Rp %,12.0f\n", nominalBayar));
        if (metode instanceof PembayaranTunai) {
            sb.append(String.format("Kembalian              : Rp %,12.0f\n", metode.getKembalian()));
        }
        sb.append("================================================================\n");
        sb.append("     Terima kasih atas kunjungan Anda di Grand Hotel!           \n");
        sb.append("================================================================\n");
        return sb.toString();
    }

    @FXML
    private void onSimpanStruk() {
        if (teksStrukTerakhir == null || teksStrukTerakhir.isBlank()) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Bukti Struk Reservasi");
        fileChooser.setInitialFileName("Struk_" + draftReservasi.getIdReservasi() + ".txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));

        File file = fileChooser.showSaveDialog(SceneNavigator.getPrimaryStage());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(teksStrukTerakhir);
                SceneNavigator.tampilkanAlert(AlertType.INFORMATION, "Sukses", "Struk Disimpan", "File berhasil disimpan ke:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                SceneNavigator.tampilkanAlert(AlertType.ERROR, "Gagal", "Simpan File Gagal", e.getMessage());
            }
        }
    }

    @FXML
    private void onKembaliKeDashboard() {
        SceneNavigator.bukaDashboard(3);
    }
}

