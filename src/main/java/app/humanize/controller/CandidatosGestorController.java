package app.humanize.controller;

import app.humanize.model.Candidato;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

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

    private void loadUI(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();


            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            System.err.println("Erro de IO ao carregar FXML: " + fxmlPath);
            e.printStackTrace();
            mostrarAlerta("Erro ao Carregar Tela", "Não foi possível carregar a interface.", e.getMessage());
        } catch (NullPointerException e) {
            System.err.println("Erro: Recurso FXML não encontrado: " + fxmlPath);
            e.printStackTrace();
            mostrarAlerta("Erro Crítico", "Arquivo da interface não encontrado.", "Caminho: " + fxmlPath);
        } catch (Exception e) {
            System.err.println("Erro inesperado ao carregar FXML: " + fxmlPath);
            e.printStackTrace();
            mostrarAlerta("Erro Inesperado", "Ocorreu um erro ao tentar carregar a tela.", e.getMessage());
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
