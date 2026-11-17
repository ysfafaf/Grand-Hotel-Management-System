package controller;

import dao.PelangganDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.DatabaseException;
import model.Pelanggan;
import util.SceneNavigator;
import java.util.Optional;

public class PelangganTabController {

    @FXML private TextField txtCariPelanggan;
    @FXML private TableView<Pelanggan> tablePelanggan;
    @FXML private TableColumn<Pelanggan, String> colPelangganNik;
    @FXML private TableColumn<Pelanggan, String> colPelangganNama;
    @FXML private TableColumn<Pelanggan, String> colPelangganTelepon;
    @FXML private TableColumn<Pelanggan, String> colPelangganMember;

    @FXML private Button btnTambahPelanggan;
    @FXML private Button btnUbahPelanggan;
    @FXML private Button btnHapusPelanggan;

    private final ObservableList<Pelanggan> listPelanggan = FXCollections.observableArrayList();
    private FilteredList<Pelanggan> filteredPelanggan;
    private final PelangganDAO pelangganDAO = new PelangganDAO();
    private Runnable onDataChangedCallback;

    @FXML
    private void initialize() {
        konfigurasiKolomTabel();
        konfigurasiPencarian();
        pasangEventHandlers();
        muatData();
    }

    public void setOnDataChangedCallback(Runnable callback) {
        this.onDataChangedCallback = callback;
    }

    private void konfigurasiKolomTabel() {
        colPelangganNik.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNik()));
        colPelangganNama.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNama()));
        colPelangganTelepon.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNoTelepon()));
        colPelangganMember.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().isMember() ? "Member (Diskon 5%)" : "Non-Member"));
    }

    private void konfigurasiPencarian() {
        filteredPelanggan = new FilteredList<>(listPelanggan, p -> true);

        txtCariPelanggan.textProperty().addListener((obs, lama, baru) -> {
            String k = (baru == null) ? "" : baru.trim().toLowerCase();
            filteredPelanggan.setPredicate(p -> k.isEmpty()
                    || p.getNik().toLowerCase().contains(k)
                    || p.getNama().toLowerCase().contains(k)
                    || p.getNoTelepon().toLowerCase().contains(k));
        });

        SortedList<Pelanggan> sortedPelanggan = new SortedList<>(filteredPelanggan);
        sortedPelanggan.comparatorProperty().bind(tablePelanggan.comparatorProperty());
        tablePelanggan.setItems(sortedPelanggan);

        tablePelanggan.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tablePelanggan.getSelectionModel().getSelectedItem() != null) {
                onUbahPelanggan();
            }
        });
    }

    private void pasangEventHandlers() {
        if (btnTambahPelanggan != null) btnTambahPelanggan.setOnAction(e -> onTambahPelanggan());
        if (btnUbahPelanggan != null) btnUbahPelanggan.setOnAction(e -> onUbahPelanggan());
        if (btnHapusPelanggan != null) btnHapusPelanggan.setOnAction(e -> onHapusPelanggan());
    }

    public void muatData() {
        try {
            listPelanggan.setAll(pelangganDAO.semua());
        } catch (DatabaseException e) {
            SceneNavigator.tampilkanAlert(AlertType.ERROR, "Kesalahan", "Gagal Memuat Pelanggan", e.getMessage());
        }
    }

    public ObservableList<Pelanggan> getListPelanggan() {
        return listPelanggan;
    }

    @FXML
    public void onTambahPelanggan() {
        SceneNavigator.bukaModalPelanggan(null, () -> {
            muatData();
            if (onDataChangedCallback != null) onDataChangedCallback.run();
        });
    }

    @FXML
    public void onUbahPelanggan() {
        Pelanggan terpilih = tablePelanggan.getSelectionModel().getSelectedItem();
        if (terpilih == null) {
            SceneNavigator.tampilkanAlert(AlertType.WARNING, "Peringatan", "Pilih Pelanggan", "Silakan pilih pelanggan dari tabel terlebih dahulu!");
            return;
        }
        SceneNavigator.bukaModalPelanggan(terpilih, () -> {
            muatData();
            if (onDataChangedCallback != null) onDataChangedCallback.run();
        });
    }

    @FXML
    public void onHapusPelanggan() {
        Pelanggan terpilih = tablePelanggan.getSelectionModel().getSelectedItem();
        if (terpilih == null) {
            SceneNavigator.tampilkanAlert(AlertType.WARNING, "Peringatan", "Pilih Pelanggan", "Silakan pilih pelanggan dari tabel terlebih dahulu!");
            return;
        }

        Alert konfirmasi = new Alert(AlertType.CONFIRMATION);
        konfirmasi.setTitle("Konfirmasi Hapus Pelanggan");
        konfirmasi.setHeaderText("Hapus Tamu " + terpilih.getNama());
        konfirmasi.setContentText("Apakah Anda yakin ingin menghapus data pelanggan ini?");

        Optional<ButtonType> hasil = konfirmasi.showAndWait();
        if (hasil.isPresent() && hasil.get() == ButtonType.OK) {
            try {
                pelangganDAO.hapus(terpilih);
                muatData();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
                SceneNavigator.tampilkanAlert(AlertType.INFORMATION, "Sukses", "Pelanggan Dihapus", "Data tamu " + terpilih.getNama() + " berhasil dihapus.");
            } catch (DatabaseException e) {
                SceneNavigator.tampilkanAlert(AlertType.ERROR, "Gagal Menghapus", "Konstrain Database", e.getMessage());
            }
        }
    }
}

