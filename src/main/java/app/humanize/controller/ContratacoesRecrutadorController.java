package app.humanize.controller;

import app.humanize.util.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ContratacoesRecrutadorController {
    public BorderPane root;

    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnSolicitarContratacoes;

    @FXML
    private Button btnCadastrarAprovados;

    @FXML
    private Button btnConsultarContratacoes;

    private Button activeButton;

    private ResourceBundle bundle;

    @FXML
    public void initialize() {
        this.bundle = UserSession.getInstance().getBundle();
        showSolicitarContratacoes();
    }

    private void loadUI(String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                throw new NullPointerException("Recurso FXML não encontrado: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resource, bundle);
            Node view = loader.load();

            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            System.err.println("Erro de IO ao carregar FXML: " + fxmlPath);
            e.printStackTrace();
            mostrarAlerta(
                    bundle.getString("alert.error.reload.title"),
                    bundle.getString("alert.error.reload.header"),
                    e.getMessage()
            );
        } catch (NullPointerException e) {
            System.err.println("Erro: Recurso FXML não encontrado: " + fxmlPath);
            e.printStackTrace();
            mostrarAlerta(
                    bundle.getString("alert.error.reload.title"),
                    bundle.getString("alert.error.fxmlNotFound.header"),
                    "Caminho: " + fxmlPath
            );
        } catch (Exception e) {
            System.err.println("Erro inesperado ao carregar FXML: " + fxmlPath);
            e.printStackTrace();
            mostrarAlerta(
                    bundle.getString("alert.error.unexpected.title"),
                    bundle.getString("alert.error.unexpected.header"),
                    e.getMessage()
            );
        }
    }

    private void mostrarAlerta(String titulo, String cabecalho, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(conteudo != null ? conteudo : "");
        alert.showAndWait();
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.setId("buttonLateral");
        }
        button.setId("buttonLateralActive");
        activeButton = button;
    }

    @FXML
    private void showCadastrarAprovados() {
        loadUI("/view/ContratacaoDeFuncionario.fxml");
        setActiveButton(btnCadastrarAprovados);
    }

    @FXML
    private void showSolicitarContratacoes() {
        loadUI("/view/SolicitarContratacoes.fxml");
        setActiveButton(btnSolicitarContratacoes);
    }

    @FXML
    private void showConsultarContratacoes() {
        loadUI("/view/ConsultarContratacoes.fxml");
        setActiveButton(btnConsultarContratacoes);
    }
}