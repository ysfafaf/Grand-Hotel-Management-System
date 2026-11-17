package controller;

import dao.ReservasiDAO;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
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
import model.Reservasi;
import model.StatusReservasi;
import util.SceneNavigator;

public class RiwayatTabController {

    @FXML private TextField txtCariRiwayat;
    @FXML private TableView<Reservasi> tableRiwayat;
    @FXML private TableColumn<Reservasi, String> colRiwayatId;
    @FXML private TableColumn<Reservasi, String> colRiwayatCheckin;
    @FXML private TableColumn<Reservasi, String> colRiwayatTamu;
    @FXML private TableColumn<Reservasi, String> colRiwayatKamar;
    @FXML private TableColumn<Reservasi, String> colRiwayatDurasi;
    @FXML private TableColumn<Reservasi, String> colRiwayatTotal;
    @FXML private TableColumn<Reservasi, String> colRiwayatMetode;
    @FXML private TableColumn<Reservasi, String> colRiwayatStatus;

    @FXML private Button btnCheckOut;
    @FXML private Button btnRefresh;

    private final ObservableList<Reservasi> listReservasi = FXCollections.observableArrayList();
    private FilteredList<Reservasi> filteredReservasi;
    private final ReservasiDAO reservasiDAO = new ReservasiDAO();
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
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        colRiwayatId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIdReservasi()));
        colRiwayatCheckin.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTanggalCheckin().format(dtf)));
        colRiwayatTamu.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPelanggan().getNama()));
        colRiwayatKamar.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getKamar().getIdKamar() + " (" + data.getValue().getKamar().getTipeKamar() + ")"));
        colRiwayatDurasi.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDurasiInap() + " Malam"));
        colRiwayatTotal.setCellValueFactory(data -> new SimpleStringProperty(String.format("Rp %,.0f", data.getValue().getTotalBayar())));
        colRiwayatMetode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMetodeBayar()));
        colRiwayatStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
    }

    private void konfigurasiPencarian() {
        filteredReservasi = new FilteredList<>(listReservasi, r -> true);

        txtCariRiwayat.textProperty().addListener((obs, lama, baru) -> {
            String k = (baru == null) ? "" : baru.trim().toLowerCase();
            filteredReservasi.setPredicate(r -> k.isEmpty()
                    || r.getIdReservasi().toLowerCase().contains(k)
                    || r.getPelanggan().getNama().toLowerCase().contains(k)
                    || r.getKamar().getIdKamar().toLowerCase().contains(k)
                    || r.getStatus().name().toLowerCase().contains(k));
        });

        SortedList<Reservasi> sorted = new SortedList<>(filteredReservasi);
        sorted.comparatorProperty().bind(tableRiwayat.comparatorProperty());
        tableRiwayat.setItems(sorted);
    }

    private void pasangEventHandlers() {
        if (btnCheckOut != null) btnCheckOut.setOnAction(e -> onCheckOut());
        if (btnRefresh != null) btnRefresh.setOnAction(e -> onRefreshData());
    }

    public void muatData() {
        try {
            listReservasi.setAll(reservasiDAO.semua());
        } catch (DatabaseException e) {
            SceneNavigator.tampilkanAlert(AlertType.ERROR, "Kesalahan", "Gagal Memuat Riwayat Reservasi", e.getMessage());
        }
    }

    public ObservableList<Reservasi> getListReservasi() {
        return listReservasi;
    }

    @FXML
    public void onCheckOut() {
        Reservasi terpilih = tableRiwayat.getSelectionModel().getSelectedItem();
        if (terpilih == null) {
            SceneNavigator.tampilkanAlert(AlertType.WARNING, "Peringatan", "Pilih Reservasi", "Silakan pilih reservasi yang ingin di-check-out terlebih dahulu!");
            return;
        }

        if (terpilih.getStatus() == StatusReservasi.SELESAI) {
            SceneNavigator.tampilkanAlert(AlertType.INFORMATION, "Informasi", "Sudah Check-out",
                    "Reservasi " + terpilih.getIdReservasi() + " sudah berstatus SELESAI (sudah check-out).");
            return;
        }

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Check-out Kamar");
        alert.setHeaderText("Check-out Tamu: " + terpilih.getPelanggan().getNama());
        alert.setContentText(String.format("Lakukan proses check-out untuk reservasi %s di kamar %s?\n" +
                "Status reservasi akan diubah menjadi SELESAI dan kamar kembali TERSEDIA dalam satu transaksi database.",
                terpilih.getIdReservasi(), terpilih.getKamar().getIdKamar()));

        Optional<ButtonType> hasil = alert.showAndWait();
        if (hasil.isPresent() && hasil.get() == ButtonType.OK) {
            try {

                reservasiDAO.checkOut(terpilih.getIdReservasi());
                muatData();
                if (onDataChangedCallback != null) {
                    onDataChangedCallback.run();
                }
                SceneNavigator.tampilkanAlert(AlertType.INFORMATION, "Check-out Berhasil", "Selesai",
                        "Tamu " + terpilih.getPelanggan().getNama() + " berhasil check-out. Kamar " +
                        terpilih.getKamar().getIdKamar() + " sekarang telah TERSEDIA kembali.");
            } catch (DatabaseException e) {
                SceneNavigator.tampilkanAlert(AlertType.ERROR, "Gagal Check-out", "Kesalahan Database", e.getMessage());
            }
        }
    }

    @FXML
    public void onRefreshData() {
        muatData();
        if (onDataChangedCallback != null) {
            onDataChangedCallback.run();
        }
    }
}

