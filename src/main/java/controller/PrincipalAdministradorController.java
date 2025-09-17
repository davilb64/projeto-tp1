package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import util.ScreenController;

import java.io.IOException;
import java.net.URL;

public class PrincipalAdministradorController {

    public BorderPane root;

    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnUsuarios;

    @FXML
    private Button btnRelatorios;

    @FXML
    private Button btnConfig;

    private Button activeButton;

    @FXML
    public void initialize() {
        // Carrega Dashboard por padrão
        showDashboard();
    }

    private void loadUI(String fxml) {
        try {
            URL resource = getClass().getResource("/view/" + fxml + ".fxml");
            System.out.println(">> Tentando carregar: " + resource);

            if (resource == null) {
                throw new IllegalStateException("FXML não encontrado: " + fxml);
            }

            Pane pane = FXMLLoader.load(resource);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(pane);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.setId("buttonLateral"); // volta ao normal
        }
        button.setId("buttonLateralActive"); // destaca o atual
        activeButton = button;
    }

    @FXML
    private void showDashboard() {
        loadUI("DashboardAdm");
        setActiveButton(btnDashboard);
    }

    @FXML
    private void showUsuarios() {
        loadUI("UsuariosAdm");
        setActiveButton(btnUsuarios);
    }

    @FXML
    private void showRelatorios() {
        loadUI("RelatoriosAdm");
        setActiveButton(btnRelatorios);
    }

    @FXML
    private void showConfiguracoes() {
        loadUI("ConfiguracoesAdm");
        setActiveButton(btnConfig);
    }

    @FXML
    private void sair() {
        ScreenController.changeScene("/view/LoginView.fxml");
    }
}
