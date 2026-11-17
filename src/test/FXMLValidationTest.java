package test;

import dao.DatabaseHelper;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class FXMLValidationTest {

    public static void main(String[] args) {
        System.out.println("--- Menguji Inisialisasi JavaFX & Validasi Berkas FXML ---");

        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {

        }

        try {
            DatabaseHelper.initDatabase();
            System.out.println("✓ Database & Seeder OK");

            String[] fxmlFiles = {
                "/view/MenuBarView.fxml",
                "/view/LoginView.fxml",
                "/view/KamarFormView.fxml",
                "/view/PelangganFormView.fxml",
                "/view/PaymentView.fxml",
                "/view/MainDashboardView.fxml"
            };

            for (String file : fxmlFiles) {
                FXMLLoader loader = new FXMLLoader(FXMLValidationTest.class.getResource(file));
                Parent root = loader.load();
                Object ctrl = loader.getController();
                System.out.printf("✓ Sukses memuat FXML: %-30s | Controller: %s\n", file, (ctrl != null ? ctrl.getClass().getSimpleName() : "None"));
            }

            System.out.println("\n✓ SEMUA FILE FXML & CONTROLLER TERVALIDASI 100% BEBAS DARI ERROR!");
            Platform.exit();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

