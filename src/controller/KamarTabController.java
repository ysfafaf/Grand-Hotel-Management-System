package controller;

import dao.KamarDAO;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.DatabaseException;
import model.Kamar;
import model.StatusKamar;
import util.SceneNavigator;
import java.util.Optional;

public class KamarTabController {

    @FXML private TextField txtCariKamar;
    @FXML private ComboBox<String> cmbFilterStatusKamar;
    @FXML private TableView<Kamar> tableKamar;
    @FXML private TableColumn<Kamar, String> colKamarId;
    @FXML private TableColumn<Kamar, String> colKamarTipe;
    @FXML private TableColumn<Kamar, String> colKamarFasilitas;
    @FXML private TableColumn<Kamar, String> colKamarTarif;
    @FXML private TableColumn<Kamar, String> colKamarStatus;

    @FXML private Button btnTambahKamar;
    @FXML private Button btnUbahKamar;
    @FXML private Button btnHapusKamar;

    private final ObservableList<Kamar> listKamar = FXCollections.observableArrayList();
    private FilteredList<Kamar> filteredKamar;
    private final KamarDAO kamarDAO = new KamarDAO();
    private Runnable onDataChangedCallback;

    @FXML
    private void initialize() {
        konfigurasiKolomTabel();
        konfigurasiFilterDanPencarian();
        pasangEventHandlers();
        muatData();
    }

    public void setOnDataChangedCallback(Runnable callback) {
        this.onDataChangedCallback = callback;
    }

    private void konfigurasiKolomTabel() {

        colKamarId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIdKamar()));
        colKamarTipe.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipeKamar()));
        colKamarFasilitas.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFasilitas()));
        colKamarTarif.setCellValueFactory(data -> new SimpleStringProperty(String.format("Rp %,.0f", data.getValue().getHargaDasar())));
        colKamarStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatusKamar().name()));
    }

    private void konfigurasiFilterDanPencarian() {
        cmbFilterStatusKamar.setItems(FXCollections.observableArrayList("Semua", "TERSEDIA", "TERISI"));
        cmbFilterStatusKamar.setValue("Semua");

        filteredKamar = new FilteredList<>(listKamar, p -> true);

        txtCariKamar.textProperty().addListener((obs, lama, baru) -> terapkanFilter());
        cmbFilterStatusKamar.valueProperty().addListener((obs, lama, baru) -> terapkanFilter());

        SortedList<Kamar> sortedKamar = new SortedList<>(filteredKamar);
        sortedKamar.comparatorProperty().bind(tableKamar.comparatorProperty());
        tableKamar.setItems(sortedKamar);

        tableKamar.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tableKamar.getSelectionModel().getSelectedItem() != null) {
                onUbahKamar();
            }
        });
    }

    private void pasangEventHandlers() {
        if (btnTambahKamar != null) btnTambahKamar.setOnAction(e -> onTambahKamar());
        if (btnUbahKamar != null) btnUbahKamar.setOnAction(e -> onUbahKamar());
        if (btnHapusKamar != null) btnHapusKamar.setOnAction(e -> onHapusKamar());
    }

    private void terapkanFilter() {
        String keyword = txtCariKamar.getText() == null ? "" : txtCariKamar.getText().trim().toLowerCase();
        String statusFilter = cmbFilterStatusKamar.getValue();

        filteredKamar.setPredicate(kamar -> {
            boolean cocokStatus = "Semua".equalsIgnoreCase(statusFilter)
                    || kamar.getStatusKamar().name().equalsIgnoreCase(statusFilter);

            boolean cocokCari = keyword.isEmpty()
                    || kamar.getIdKamar().toLowerCase().contains(keyword)
                    || kamar.getTipeKamar().toLowerCase().contains(keyword)
                    || kamar.getFasilitas().toLowerCase().contains(keyword);

            return cocokStatus && cocokCari;
        });
    }

    public void muatData() {
        try {
            listKamar.setAll(kamarDAO.semua());
        } catch (DatabaseException e) {
            SceneNavigator.tampilkanAlert(AlertType.ERROR, "Kesalahan", "Gagal Memuat Kamar", e.getMessage());
        }
    }

    public ObservableList<Kamar> getListKamar() {
        return listKamar;
    }

    @FXML
    public void onTambahKamar() {
        SceneNavigator.bukaModalKamar(null, () -> {
            muatData();
            if (onDataChangedCallback != null) onDataChangedCallback.run();
        });
    }

    @FXML
    public void onUbahKamar() {
        Kamar terpilih = tableKamar.getSelectionModel().getSelectedItem();
        if (terpilih == null) {
            SceneNavigator.tampilkanAlert(AlertType.WARNING, "Peringatan", "Pilih Kamar", "Silakan pilih kamar dari tabel terlebih dahulu!");
            return;
        }
        SceneNavigator.bukaModalKamar(terpilih, () -> {
            muatData();
            if (onDataChangedCallback != null) onDataChangedCallback.run();
        });
    }

    @FXML
    public void onHapusKamar() {
        Kamar terpilih = tableKamar.getSelectionModel().getSelectedItem();
        if (terpilih == null) {
            SceneNavigator.tampilkanAlert(AlertType.WARNING, "Peringatan", "Pilih Kamar", "Silakan pilih kamar dari tabel terlebih dahulu!");
            return;
        }

        if (terpilih.getStatusKamar() == StatusKamar.TERISI) {
            SceneNavigator.tampilkanAlert(AlertType.WARNING, "Tidak Dapat Dihapus", "Kamar Sedang Terisi",
                    "Kamar " + terpilih.getIdKamar() + " sedang berstatus TERISI dan tidak boleh dihapus.");
            return;
        }

        Alert konfirmasi = new Alert(AlertType.CONFIRMATION);
        konfirmasi.setTitle("Konfirmasi Hapus Kamar");
        konfirmasi.setHeaderText("Hapus Kamar " + terpilih.getIdKamar());
        konfirmasi.setContentText("Apakah Anda yakin ingin menghapus data kamar ini secara permanen?");

        Optional<ButtonType> hasil = konfirmasi.showAndWait();
        if (hasil.isPresent() && hasil.get() == ButtonType.OK) {
            try {
                kamarDAO.hapus(terpilih);
                muatData();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
                SceneNavigator.tampilkanAlert(AlertType.INFORMATION, "Sukses", "Kamar Dihapus", "Kamar " + terpilih.getIdKamar() + " berhasil dihapus.");
            } catch (DatabaseException e) {
                SceneNavigator.tampilkanAlert(AlertType.ERROR, "Gagal Menghapus", "Konstrain Database", e.getMessage());
            }
        }
    }
}

