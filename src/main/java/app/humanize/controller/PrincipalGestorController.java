package app.humanize.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import app.humanize.util.ScreenController;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;

public class PrincipalGestorController {
    public BorderPane root;

    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnContratacoes;

    @FXML
    private Button btnRecrutadores;

    @FXML
    private Button btnRelatorios;

    @FXML
    private Button btnVagas;

    @FXML
    private Button btnFinanceiro;

    @FXML
    private Button btnConfig;

    private Button activeButton;

    @FXML
    public void initialize() {
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
            activeButton.setId("buttonLateral");
        }
        button.setId("buttonLateralActive");
        activeButton = button;
    }

    @FXML
    private void showDashboard() {
        loadUI("DashboardGestor");
        setActiveButton(btnDashboard);
    }

    @FXML
    private void showRecrutadores() {
        loadUI("AtribuirRecrutadorAVaga");
        setActiveButton(btnRecrutadores);
    }

    @FXML
    private void showVagas() {
        loadUI("Vagas");
        setActiveButton(btnVagas);
    }


    @FXML
    private void showRelatorios() {
        loadUI("RelatoriosAdm");
        setActiveButton(btnRelatorios);
    }

    @FXML
    private void showContratacoes() {
        loadUI("ContratacoesRecrutador");
        setActiveButton(btnContratacoes);
    }

    @FXML
    private void showFinanceiro() {
        loadUI("MenuFinanceiroAdm");
        setActiveButton(btnFinanceiro);
    }

    @FXML
    private void showConfig() {
        loadUI("ConfiguracoesAdm");
        setActiveButton(btnConfig);
    }




    @FXML
    private void sair() {
        ScreenController.changeScene("/view/LoginView.fxml");
    }
}
