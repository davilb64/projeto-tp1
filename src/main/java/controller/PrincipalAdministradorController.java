package controller;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import util.ScreenController;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;

public class PrincipalAdministradorController {

    public BorderPane root;
    @FXML
    private StackPane contentArea;

    private void loadUI(String fxml) {
        try {
            URL resource = getClass().getResource("/view/" + fxml + ".fxml");
            System.out.println(">> Tentando carregar: " + resource);

            if (resource == null) {
                throw new IllegalStateException("FXML não encontrado: " + fxml);
            }

            Pane pane = FXMLLoader.load(resource); // <- agora funciona
            contentArea.getChildren().clear();
            contentArea.getChildren().add(pane);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showDashboard() {
        loadUI("Dashboard");
    }

    @FXML
    private void showUsuarios() {
        loadUI("Usuarios");
    }

    @FXML
    private void showRelatorios() {
        loadUI("Relatorios");
    }

    @FXML
    private void showConfiguracoes() {
        loadUI("Configuracoes");
    }

    @FXML
    private void sair() {
        ScreenController.changeScene("/view/LoginView.fxml");
    }


}
