package controller;

import dao.AdminDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.Admin;
import model.DatabaseException;
import util.SceneNavigator;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblError;

    private final AdminDAO adminDAO = new AdminDAO();

    @FXML
    private void initialize() {
        lblError.setText("");
        txtPassword.setOnAction(e -> onLogin());
        txtUsername.setOnAction(e -> onLogin());
    }

    @FXML
    private void onLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            lblError.setText("Username dan password wajib diisi!");
            return;
        }

        try {
            Admin admin = adminDAO.validasiLogin(username, password);
            if (admin != null) {
                lblError.setText("");
                SceneNavigator.setCurrentAdmin(admin);
                SceneNavigator.bukaDashboard();
            } else {
                lblError.setText("Username atau password salah!");
                txtPassword.clear();
                txtPassword.requestFocus();
            }
        } catch (DatabaseException e) {
            lblError.setText("Kesalahan database: " + e.getMessage());
        }
    }
}

