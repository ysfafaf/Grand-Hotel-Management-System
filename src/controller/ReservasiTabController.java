package controller;

import dao.KamarDAO;
import dao.PelangganDAO;
import java.time.LocalDate;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import model.DatabaseException;
import model.FasilitasTambahan;
import model.Kamar;
import model.KriteriaDiskon;
import model.LayananDiskon;
import model.Pelanggan;
import model.Reservasi;
import model.RincianBiaya;
import model.StatusReservasi;
import util.SceneNavigator;

public class ReservasiTabController {

    @FXML private ComboBox<Pelanggan> cmbPelanggan;
    @FXML private Label lblStatusMember;
    @FXML private ComboBox<Kamar> cmbKamarTersedia;
    @FXML private Label lblDetailKamar;
    @FXML private DatePicker dpCheckin;
    @FXML private Spinner<Integer> spDurasi;
    @FXML private CheckBox chkSarapan;
    @FXML private CheckBox chkExtraBed;
    @FXML private TextField txtKodePromo;
    @FXML private Label lblPreviewSubtotal;
    @FXML private Label lblPreviewDiskon;
    @FXML private Label lblPreviewTotal;
    @FXML private Button btnHitungDanLanjut;

    private final KamarDAO kamarDAO = new KamarDAO();
    private final PelangganDAO pelangganDAO = new PelangganDAO();
    private final LayananDiskon layananDiskon = new LayananDiskon();

    @FXML
    private void initialize() {
        konfigurasiKomponen();
        pasangLiveListeners();
        muatDataPilihan();
    }

    private void konfigurasiKomponen() {
        dpCheckin.setValue(LocalDate.now());
        spDurasi.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 1));
        spDurasi.setEditable(true);
        if (btnHitungDanLanjut != null) {
            btnHitungDanLanjut.setOnAction(e -> onHitungDanLanjutPembayaran());
        }
    }

    private void pasangLiveListeners() {

        cmbPelanggan.valueProperty().addListener((obs, lama, p) -> {
            if (p != null) {
                lblStatusMember.setText(p.isMember() ? "★ Status: MEMBER (Diskon Tambahan 5%)" : "Status: Non-Member");
            } else {
                lblStatusMember.setText("Pilih pelanggan untuk melihat status.");
            }
            hitungEstimasiBiayaLive();
        });

        cmbKamarTersedia.valueProperty().addListener((obs, lama, k) -> {
            if (k != null) {
                lblDetailKamar.setText(String.format("Tarif: Rp %,.0f/malam | Fasilitas: %s", k.getHargaDasar(), k.getFasilitas()));
            } else {
                lblDetailKamar.setText("Pilih kamar.");
            }
            hitungEstimasiBiayaLive();
        });

        spDurasi.valueProperty().addListener((obs, lama, baru) -> hitungEstimasiBiayaLive());
        chkSarapan.selectedProperty().addListener((obs, lama, baru) -> hitungEstimasiBiayaLive());
        chkExtraBed.selectedProperty().addListener((obs, lama, baru) -> hitungEstimasiBiayaLive());
        txtKodePromo.textProperty().addListener((obs, lama, baru) -> hitungEstimasiBiayaLive());
    }

    public void muatDataPilihan() {
        try {

            List<Pelanggan> pelangganList = pelangganDAO.semua();
            cmbPelanggan.setItems(FXCollections.observableArrayList(pelangganList));

            List<Kamar> kamarList = kamarDAO.semuaTersedia();
            cmbKamarTersedia.setItems(FXCollections.observableArrayList(kamarList));

            hitungEstimasiBiayaLive();
        } catch (DatabaseException e) {
            SceneNavigator.tampilkanAlert(AlertType.ERROR, "Kesalahan", "Gagal Memuat Data Form Reservasi", e.getMessage());
        }
    }

    private void hitungEstimasiBiayaLive() {
        Kamar k = cmbKamarTersedia.getValue();
        Pelanggan p = cmbPelanggan.getValue();
        if (k == null) {
            lblPreviewSubtotal.setText("Rp 0");
            lblPreviewDiskon.setText("Rp 0");
            lblPreviewTotal.setText("Rp 0");
            return;
        }

        int durasi = spDurasi.getValue() != null ? spDurasi.getValue() : 1;
        boolean isMember = (p != null && p.isMember());
        String promo = txtKodePromo.getText() != null ? txtKodePromo.getText().trim() : null;

        KriteriaDiskon kd = new KriteriaDiskon(isMember, promo);
        FasilitasTambahan ft = new FasilitasTambahan(chkSarapan.isSelected(), chkExtraBed.isSelected());

        try {
            RincianBiaya rb = layananDiskon.hitungRincian(k, durasi, kd, ft);
            lblPreviewSubtotal.setText(String.format("Rp %,.0f", rb.subtotalKamar()));
            lblPreviewDiskon.setText(String.format("-Rp %,.0f", rb.diskonTipe() + rb.diskonMember() + rb.potonganPromo()));
            lblPreviewTotal.setText(String.format("Rp %,.0f", rb.total()));
        } catch (IllegalArgumentException e) {
            lblPreviewTotal.setText("Kode promo tidak valid");
        }
    }

    @FXML
    public void onHitungDanLanjutPembayaran() {
        try {
            Pelanggan p = cmbPelanggan.getValue();
            if (p == null) {
                throw new IllegalArgumentException("Silakan pilih tamu/pelanggan terlebih dahulu!");
            }

            Kamar k = cmbKamarTersedia.getValue();
            if (k == null) {
                throw new IllegalArgumentException("Silakan pilih kamar yang tersedia terlebih dahulu!");
            }

            LocalDate checkin = dpCheckin.getValue();
            if (checkin == null) {
                throw new IllegalArgumentException("Tanggal check-in wajib dipilih!");
            }
            if (checkin.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Tanggal check-in tidak boleh di masa lalu!");
            }

            int durasi = spDurasi.getValue() != null ? spDurasi.getValue() : 1;
            if (durasi < 1) {
                throw new IllegalArgumentException("Durasi inap minimal 1 malam!");
            }

            String kodePromo = txtKodePromo.getText() == null ? "" : txtKodePromo.getText().trim();
            KriteriaDiskon kriteria = new KriteriaDiskon(p.isMember(), kodePromo);
            FasilitasTambahan fasilitas = new FasilitasTambahan(chkSarapan.isSelected(), chkExtraBed.isSelected());

            RincianBiaya rincian = layananDiskon.hitungRincian(k, durasi, kriteria, fasilitas);

            Reservasi draft = new Reservasi(
                p, k, checkin, durasi, fasilitas, kodePromo,
                "Belum Dipilih", rincian.total(), StatusReservasi.AKTIF
            );

            SceneNavigator.bukaPembayaran(draft, rincian);

        } catch (IllegalArgumentException ex) {
            SceneNavigator.tampilkanAlert(AlertType.ERROR, "Validasi Reservasi Gagal", "Input Form Tidak Lengkap", ex.getMessage());
        }
    }

    public void resetForm() {
        cmbPelanggan.setValue(null);
        cmbKamarTersedia.setValue(null);
        dpCheckin.setValue(LocalDate.now());
        spDurasi.getValueFactory().setValue(1);
        chkSarapan.setSelected(false);
        chkExtraBed.setSelected(false);
        txtKodePromo.clear();
        muatDataPilihan();
    }
}

