package controller;

import dao.PelangganDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import model.DatabaseException;
import model.Pelanggan;
import util.SceneNavigator;

public class PelangganFormController {

    @FXML
    private Label lblJudul;

    @FXML
    private TextField txtNik;

    @FXML
    private TextField txtNama;

    @FXML
    private TextField txtNoTelepon;

    @FXML
    private RadioButton rdoMember;

    @FXML
    private RadioButton rdoNonMember;

    @FXML
    private ToggleGroup tgMember;

    private Stage stage;
    private Pelanggan pelangganToEdit;
    private Runnable onSavedCallback;
    private final PelangganDAO pelangganDAO = new PelangganDAO();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setOnSavedCallback(Runnable callback) {
        this.onSavedCallback = callback;
    }

    public void setPelanggan(Pelanggan pelanggan) {
        this.pelangganToEdit = pelanggan;
        if (pelanggan != null) {
            lblJudul.setText("Ubah Data Pelanggan");
            txtNik.setText(pelanggan.getNik());
            txtNama.setText(pelanggan.getNama());
            txtNoTelepon.setText(pelanggan.getNoTelepon());
            if (pelanggan.isMember()) {
                rdoMember.setSelected(true);
            } else {
                rdoNonMember.setSelected(true);
            }
        } else {
            lblJudul.setText("Tambah Pelanggan Baru");
            txtNik.clear();
            txtNama.clear();
            txtNoTelepon.clear();
            rdoNonMember.setSelected(true);
        }
    }

    @FXML
    private void onSimpan() {
        try {
            String nik = txtNik.getText();
            String nama = txtNama.getText();
            String telepon = txtNoTelepon.getText();
            boolean isMember = rdoMember.isSelected();

            if (pelangganToEdit == null) {
                Pelanggan baru = new Pelanggan(nik, nama, telepon, isMember);
                pelangganDAO.tambah(baru);
            } else {
                pelangganToEdit.setNik(nik);
                pelangganToEdit.setNama(nama);
                pelangganToEdit.setNoTelepon(telepon);
                pelangganToEdit.setMember(isMember);
                pelangganDAO.ubah(pelangganToEdit);
            }

            if (onSavedCallback != null) {
                onSavedCallback.run();
            }

            stage.close();
        } catch (IllegalArgumentException | DatabaseException ex) {
            SceneNavigator.tampilkanAlert(AlertType.ERROR, "Validasi Gagal", "Input Pelanggan Tidak Valid", ex.getMessage());
        }
    }

    @FXML
    private void onBatal() {
        if (stage != null) {
            stage.close();
        }
    }
}

