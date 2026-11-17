package controller;

import dao.KamarDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.DatabaseException;
import model.Kamar;
import model.KamarFactory;
import model.StatusKamar;
import util.SceneNavigator;

public class KamarFormController {

    @FXML
    private Label lblJudul;

    @FXML
    private TextField txtIdKamar;

    @FXML
    private ComboBox<String> cmbTipe;

    @FXML
    private TextField txtHargaDasar;

    @FXML
    private ComboBox<StatusKamar> cmbStatus;

    private Stage stage;
    private Kamar kamarToEdit;
    private Runnable onSavedCallback;
    private final KamarDAO kamarDAO = new KamarDAO();

    @FXML
    private void initialize() {
        cmbTipe.setItems(FXCollections.observableArrayList("Standard", "Deluxe", "Suite"));
        cmbTipe.setValue("Standard");

        cmbStatus.setItems(FXCollections.observableArrayList(StatusKamar.TERSEDIA, StatusKamar.TERISI));
        cmbStatus.setValue(StatusKamar.TERSEDIA);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setOnSavedCallback(Runnable callback) {
        this.onSavedCallback = callback;
    }

    public void setKamar(Kamar kamar) {
        this.kamarToEdit = kamar;
        if (kamar != null) {
            lblJudul.setText("Ubah Data Kamar");
            txtIdKamar.setText(kamar.getIdKamar());
            txtIdKamar.setDisable(true);
            cmbTipe.setValue(kamar.getTipeKamar());
            txtHargaDasar.setText(String.format("%.0f", kamar.getHargaDasar()));
            cmbStatus.setValue(kamar.getStatusKamar());
        } else {
            lblJudul.setText("Tambah Kamar Baru");
            txtIdKamar.setDisable(false);
            txtIdKamar.clear();
            txtHargaDasar.clear();
            cmbTipe.setValue("Standard");
            cmbStatus.setValue(StatusKamar.TERSEDIA);
        }
    }

    @FXML
    private void onSimpan() {
        try {
            String id = txtIdKamar.getText() == null ? "" : txtIdKamar.getText().trim();
            String tipe = cmbTipe.getValue();
            StatusKamar status = cmbStatus.getValue();

            if (txtHargaDasar.getText() == null || txtHargaDasar.getText().isBlank()) {
                throw new IllegalArgumentException("Harga kamar wajib diisi!");
            }

            double harga;
            try {
                harga = Double.parseDouble(txtHargaDasar.getText().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Harga kamar harus berupa angka yang valid!");
            }

            Kamar kamar = KamarFactory.buat(tipe, id, harga, status);

            if (kamarToEdit == null) {
                kamarDAO.tambah(kamar);
            } else {
                kamarDAO.ubah(kamar);
            }

            if (onSavedCallback != null) {
                onSavedCallback.run();
            }

            stage.close();
        } catch (IllegalArgumentException | DatabaseException ex) {
            SceneNavigator.tampilkanAlert(AlertType.ERROR, "Validasi Gagal", "Input Tidak Valid", ex.getMessage());
        }
    }

    @FXML
    private void onBatal() {
        if (stage != null) {
            stage.close();
        }
    }
}

