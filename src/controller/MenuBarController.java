package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import util.SceneNavigator;

public class MenuBarController {

    @FXML
    private void onDashboard() {
        SceneNavigator.bukaDashboard();
    }

    @FXML
    private void onDataKamar() {
        SceneNavigator.bukaDashboard(0);
    }

    @FXML
    private void onDataPelanggan() {
        SceneNavigator.bukaDashboard(1);
    }

    @FXML
    private void onReservasiBaru() {
        SceneNavigator.bukaDashboard(2);
    }

    @FXML
    private void onRiwayatReservasi() {
        SceneNavigator.bukaDashboard(3);
    }

    @FXML
    private void onTentang() {
        SceneNavigator.tampilkanAlert(
            AlertType.INFORMATION,
            "Tentang Aplikasi",
            "Grand Hotel Management System (GHMS) v1.0",
            "Aplikasi Desktop Pengelolaan Reservasi Hotel berbasis JavaFX, MVC, DAO, dan SQLite.\n" +
            "Dibuat untuk Tugas Besar Pemrograman Berorientasi Objek (PBO)."
        );
    }

    @FXML
    private void onLogOut() {
        SceneNavigator.logout();
    }

    @FXML
    private void onKeluar() {
        SceneNavigator.keluarAplikasi();
    }
}

