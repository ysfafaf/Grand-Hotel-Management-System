import dao.DatabaseHelper;
import javafx.application.Application;
import javafx.stage.Stage;
import model.DatabaseException;
import util.SceneNavigator;

public class MainApp extends Application {

    @Override
    public void init() throws Exception {
        super.init();
        try {

            DatabaseHelper.initDatabase();
        } catch (DatabaseException e) {
            System.err.println("Peringatan: Gagal menginisialisasi database saat startup: " + e.getMessage());
        }
    }

    @Override
    public void start(Stage primaryStage) {
        SceneNavigator.setPrimaryStage(primaryStage);
        SceneNavigator.bukaLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

