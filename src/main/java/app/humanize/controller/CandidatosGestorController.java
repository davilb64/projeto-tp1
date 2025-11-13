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

public class CandidatosGestorController {
    public BorderPane root;

    @FXML
    private StackPane contentArea;
    @FXML
    private Button btnStatus;
    @FXML
    private Button activeButton;
    private ResourceBundle bundle;

    @FXML
    public void initialize() {
        this.bundle = UserSession.getInstance().getBundle();
        showStatus(); // carrega a tela de status por padrão
    }

    private void loadUI() {
        try {
            URL resource = getClass().getResource("/view/StatusDaCandidaturaGestor.fxml");
            if (resource == null) {
                throw new NullPointerException(bundle.getString("exception.fxmlNotFound.generic") + "/view/StatusDaCandidaturaGestor.fxml");
            }

            FXMLLoader loader = new FXMLLoader(resource, bundle);
            Node view = loader.load();

            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            System.err.println(bundle.getString("log.error.fxmlLoad.io") + "/view/StatusDaCandidaturaGestor.fxml");
            e.printStackTrace();
            mostrarAlerta(
                    bundle.getString("alert.error.reload.title"),
                    bundle.getString("candidatesManager.alert.error.load.header"),
                    e.getMessage()
            );
        } catch (NullPointerException e) {
            System.err.println(bundle.getString("log.error.fxmlNotFound") + "/view/StatusDaCandidaturaGestor.fxml");
            e.printStackTrace();
            mostrarAlerta(
                    bundle.getString("alert.error.critical.title"),
                    bundle.getString("alert.error.fxmlNotFound.header"),
                    bundle.getString("alert.error.fxmlNotFound.content.path") + "/view/StatusDaCandidaturaGestor.fxml"
            );
        } catch (Exception e) {
            System.err.println(bundle.getString("log.error.fxmlLoad.unexpected") + "/view/StatusDaCandidaturaGestor.fxml");
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
        loadUI();
        setActiveButton(btnStatus);
    }
}