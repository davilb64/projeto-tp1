package app.humanize.controller;

import app.humanize.model.Candidato;
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

public class CandidatosGestorController {
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
        showStatus(); // Carrega a tela de status por padrão
    }

    private void loadUI(String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                throw new NullPointerException(bundle.getString("exception.fxmlNotFound.generic") + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resource, bundle);
            Node view = loader.load();

            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            System.err.println(bundle.getString("log.error.fxmlLoad.io") + fxmlPath);
            e.printStackTrace();
            mostrarAlerta(
                    bundle.getString("alert.error.reload.title"),
                    bundle.getString("candidatesManager.alert.error.load.header"),
                    e.getMessage()
            );
        } catch (NullPointerException e) {
            System.err.println(bundle.getString("log.error.fxmlNotFound") + fxmlPath);
            e.printStackTrace();
            mostrarAlerta(
                    bundle.getString("alert.error.critical.title"),
                    bundle.getString("alert.error.fxmlNotFound.header"),
                    bundle.getString("alert.error.fxmlNotFound.content.path") + fxmlPath
            );
        } catch (Exception e) {
            System.err.println(bundle.getString("log.error.fxmlLoad.unexpected") + fxmlPath);
            e.printStackTrace();
            mostrarAlerta(
                    bundle.getString("alert.error.unexpected.title"),
                    bundle.getString("candidatesManager.alert.error.load.header.unexpected"),
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
    public void showStatus() {
        loadUI("/view/StatusDaCandidaturaGestor.fxml");
        setActiveButton(btnStatus);
    }
}