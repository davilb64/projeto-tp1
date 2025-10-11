package app.humanize.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import app.humanize.util.ScreenController;

import java.io.IOException;
import java.net.URL;

public class PrincipalRecrutadorController {
    public BorderPane root;

    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnCandidatos;

    @FXML
    private Button btnEntrevistas;

    @FXML
    private Button btnContratacoes;

    @FXML
    private Button btnConfig;

    private Button activeButton;

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
            activeButton.setId("buttonLateral");
        }
        button.setId("buttonLateralActive");
        activeButton = button;
    }

    @FXML
    private void showCandidatos() {
        loadUI("CandidatosRecrutador");
        setActiveButton(btnCandidatos);
    }

    @FXML
    private void showEntrevistas() {
        loadUI("MarcarEntrevista");
        setActiveButton(btnEntrevistas);
    }

    @FXML
    private void showContratacoes() {
        loadUI("ContratacoesRecrutador");
        setActiveButton(btnContratacoes);
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
