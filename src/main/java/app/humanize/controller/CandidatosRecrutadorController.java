package app.humanize.controller;

import app.humanize.util.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CandidatosRecrutadorController {
    public BorderPane root;

    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnCadastro;
    @FXML
    private Button btnCandidatura;
    @FXML
    private Button btnStatus;
    private Button activeButton;
    private ResourceBundle bundle;

    @FXML
    public void initialize() {
        this.bundle = UserSession.getInstance().getBundle();
        showCadastro(); // Carrega a tela de cadastro por padrão
    }

    private void loadUI(String fxml) {
        try {
            URL resource = getClass().getResource("/view/" + fxml + ".fxml");
            System.out.println(bundle.getString("log.info.fxmlLoading") + resource);

            if (resource == null) {
                throw new IllegalStateException(bundle.getString("exception.fxmlNotFound.generic") + fxml);
            }

            FXMLLoader loader = new FXMLLoader(resource, bundle);
            Pane pane = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(pane);

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(
                    bundle.getString("alert.error.reload.title"),
                    bundle.getString("alert.error.reload.header"),
                    e.getMessage()
            );
        } catch (IllegalStateException e) {
            e.printStackTrace();
            mostrarAlerta(
                    bundle.getString("alert.error.critical.title"),
                    bundle.getString("alert.error.fxmlNotFound.header"),
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
    private void showCadastro() {
        loadUI("ListaDeCandidatos");
        setActiveButton(btnCadastro);
    }

    @FXML
    private void showCandidatura() {
        loadUI("RealizarCandidatura");
        setActiveButton(btnCandidatura);
    }

    @FXML
    private void showStatus() {
        loadUI("StatusDaCandidatura");
        setActiveButton(btnStatus);
    }
}