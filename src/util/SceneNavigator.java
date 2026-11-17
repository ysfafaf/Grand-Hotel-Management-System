package util;

import dao.DatabaseHelper;
import java.io.IOException;
import java.util.Optional;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Admin;
import model.Kamar;
import model.Pelanggan;
import model.Reservasi;
import model.RincianBiaya;

public final class SceneNavigator {

    private static Stage primaryStage;
    private static Admin currentAdmin;

    private SceneNavigator() {

    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static Admin getCurrentAdmin() {
        return currentAdmin;
    }

    public static void setCurrentAdmin(Admin admin) {
        currentAdmin = admin;
    }

    public static void bukaLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource("/view/LoginView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(SceneNavigator.class.getResource("/view/style.css").toExternalForm());

            primaryStage.setTitle("Grand Hotel Management System (GHMS) - Login");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(500);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            tampilkanAlert(AlertType.ERROR, "Kesalahan Navigasi", "Gagal memuat tampilan Login", e.getMessage());
        }
    }

    public static void bukaDashboard() {
        bukaDashboard(0);
    }

    public static void bukaDashboard(int tabIndex) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource("/view/MainDashboardView.fxml"));
            Parent root = loader.load();

            controller.MainController controller = loader.getController();
            if (controller != null) {
                controller.pilihTab(tabIndex);
            }

            Scene scene = new Scene(root, 1100, 720);
            scene.getStylesheets().add(SceneNavigator.class.getResource("/view/style.css").toExternalForm());

            primaryStage.setTitle("Grand Hotel Management System (GHMS) - Dashboard");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(950);
            primaryStage.setMinHeight(650);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            tampilkanAlert(AlertType.ERROR, "Kesalahan Navigasi", "Gagal memuat Dashboard", e.getMessage());
        }
    }

    public static void bukaPembayaran(Reservasi draftReservasi, RincianBiaya rincianBiaya) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource("/view/PaymentView.fxml"));
            Parent root = loader.load();

            controller.PaymentController controller = loader.getController();
            if (controller != null) {
                controller.inisialisasiData(draftReservasi, rincianBiaya);
            }

            Scene scene = new Scene(root, 1050, 720);
            scene.getStylesheets().add(SceneNavigator.class.getResource("/view/style.css").toExternalForm());

            primaryStage.setTitle("GHMS - Pembayaran & Konfirmasi Reservasi");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            tampilkanAlert(AlertType.ERROR, "Kesalahan Navigasi", "Gagal memuat halaman Pembayaran", e.getMessage());
        }
    }

    public static void bukaModalKamar(Kamar kamarToEdit, Runnable onSavedCallback) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource("/view/KamarFormView.fxml"));
            Parent root = loader.load();

            controller.KamarFormController formCtrl = loader.getController();
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(primaryStage);
            modalStage.setTitle(kamarToEdit == null ? "Tambah Data Kamar" : "Ubah Data Kamar");

            Scene scene = new Scene(root, 460, 380);
            scene.getStylesheets().add(SceneNavigator.class.getResource("/view/style.css").toExternalForm());
            modalStage.setScene(scene);
            modalStage.setResizable(false);

            if (formCtrl != null) {
                formCtrl.setStage(modalStage);
                formCtrl.setKamar(kamarToEdit);
                formCtrl.setOnSavedCallback(onSavedCallback);
            }

            modalStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            tampilkanAlert(AlertType.ERROR, "Kesalahan", "Gagal membuka Form Kamar", e.getMessage());
        }
    }

    public static void bukaModalPelanggan(Pelanggan pelangganToEdit, Runnable onSavedCallback) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource("/view/PelangganFormView.fxml"));
            Parent root = loader.load();

            controller.PelangganFormController formCtrl = loader.getController();
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(primaryStage);
            modalStage.setTitle(pelangganToEdit == null ? "Tambah Data Pelanggan" : "Ubah Data Pelanggan");

            Scene scene = new Scene(root, 460, 380);
            scene.getStylesheets().add(SceneNavigator.class.getResource("/view/style.css").toExternalForm());
            modalStage.setScene(scene);
            modalStage.setResizable(false);

            if (formCtrl != null) {
                formCtrl.setStage(modalStage);
                formCtrl.setPelanggan(pelangganToEdit);
                formCtrl.setOnSavedCallback(onSavedCallback);
            }

            modalStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            tampilkanAlert(AlertType.ERROR, "Kesalahan", "Gagal membuka Form Pelanggan", e.getMessage());
        }
    }

    public static void logout() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Log Out");
        alert.setHeaderText("Keluar dari Sesi");
        alert.setContentText("Apakah Anda yakin ingin keluar dari akun ini?");
        alert.initOwner(primaryStage);

        Optional<ButtonType> hasil = alert.showAndWait();
        if (hasil.isPresent() && hasil.get() == ButtonType.OK) {
            currentAdmin = null;
            bukaLogin();
        }
    }

    public static void keluarAplikasi() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Tutup Aplikasi");
        alert.setHeaderText("Keluar dari GHMS");
        alert.setContentText("Apakah Anda yakin ingin menutup aplikasi?");
        alert.initOwner(primaryStage);

        Optional<ButtonType> hasil = alert.showAndWait();
        if (hasil.isPresent() && hasil.get() == ButtonType.OK) {
            primaryStage.close();
        }
    }

    public static void tampilkanAlert(AlertType tipe, String judul, String header, String pesan) {
        Alert alert = new Alert(tipe);
        alert.setTitle(judul);
        alert.setHeaderText(header);
        alert.setContentText(pesan);
        if (primaryStage != null && primaryStage.isShowing()) {
            alert.initOwner(primaryStage);
        }
        alert.showAndWait();
    }
}

