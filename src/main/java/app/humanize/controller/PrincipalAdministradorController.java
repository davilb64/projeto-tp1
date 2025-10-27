package app.humanize.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import app.humanize.util.ScreenController;

import java.io.IOException;
import java.net.URL;

public class PrincipalAdministradorController {

    public BorderPane root;

    @FXML
    private StackPane contentArea;

    @FXML private Button btnDashboard;
    @FXML private Button btnUsuarios;
    @FXML private Button btnRelatorios;
    @FXML private Button btnConfig;
    @FXML private Button btnFinanceiro;
    @FXML private Button btnRecrutadores;
    @FXML private Button btnCandidatos;
    @FXML private Button btnVagas;
    @FXML private Button btnEntrevistas;
    @FXML private Button btnContratacoes;

    private Button activeButton;

    @FXML
    public void initialize() throws IOException {
        if (btnDashboard != null) {
            btnDashboard.getStyleClass().add("buttonLateral-active");
            activeButton = btnDashboard;
        }
        showDashboard();
    }

    private boolean isDarkThemeActive() {
        if (root != null && root.getStyleClass().contains("dark")) {
            return true;
        }
        return contentArea != null && contentArea.getScene() != null && contentArea.getScene().getRoot().getStyleClass().contains("dark");
    }

    private void applyCurrentTheme(Node node) {
        if (isDarkThemeActive()) {
            node.getStyleClass().add("dark");
        } else {
            node.getStyleClass().remove("dark");
        }
    }

    private void loadUI(String fxml) {
        try {
            URL resource = getClass().getResource("/view/" + fxml + ".fxml");
            System.out.println(">> Tentando carregar: " + resource);

            if (resource == null) {
                throw new IllegalStateException("FXML não encontrado: " + fxml);
            }

            Node view = FXMLLoader.load(resource);

            applyCurrentTheme(view);

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Erro de IO ao carregar FXML: " + fxml);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erro inesperado ao carregar UI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("buttonLateral-active");
        }
        button.getStyleClass().add("buttonLateral-active");
        activeButton = button;
    }

    @FXML
    private void showDashboard(){
        loadUI("DashboardAdm");
        setActiveButton(btnDashboard);
    }


    @FXML
    public void showVagas() {
        loadUI("Vagas");
        setActiveButton(btnVagas);
    }

    @FXML
    public void showUsuarios() {
        loadUI("UsuariosAdm");
        setActiveButton(btnUsuarios);
    }

    @FXML
    void showRelatorios() {
        loadUI("RelatoriosAdm");
        setActiveButton(btnRelatorios);
    }

    @FXML
    private void showConfiguracoes() {
        loadUI("ConfiguracoesAdm");
        setActiveButton(btnConfig);
    }

    @FXML
    private void showFinanceiro() {
        loadUI("MenuFinanceiroAdm");
        setActiveButton(btnFinanceiro);
    }

    @FXML
    private void showContratacoes() {
        loadUI("ContratacoesRecrutador");
        setActiveButton(btnContratacoes);
    }

    @FXML
    private void showRecrutadores() {
        loadUI("AtribuirRecrutadorAVaga");
        setActiveButton(btnRecrutadores);
    }

    @FXML
    private void showEntrevistas() {
        loadUI("GestaoEntrevista");
        setActiveButton(btnEntrevistas);
    }

    @FXML
    private void showCandidatos() {
        loadUI("CandidatosAdm");
        setActiveButton(btnCandidatos);
    }

    @FXML
    private void sair() {
        ScreenController.changeScene("/view/LoginView.fxml");
    }
}