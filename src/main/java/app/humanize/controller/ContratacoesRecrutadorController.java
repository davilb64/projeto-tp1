package app.humanize.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;

public class ContratacoesRecrutadorController {
    public BorderPane root;
    public VBox cabecalho;

    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnSolicitarContratacoes;

    @FXML
    private Button btnCadastrarAprovados;

    @FXML
    private Button btnConsultarContratacoes;



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
    private void showCadastrarAprovados() {
        loadUI("CadastroDeFuncionario");
        setActiveButton(btnCadastrarAprovados);
    }

    @FXML
    private void showSolicitarContratacoes() {
        loadUI("SolicitarContratacoes");
        setActiveButton(btnSolicitarContratacoes);
    }

    @FXML
    private void showConsultarContratacoes() {
        loadUI("ConsultarContratacoes");
        setActiveButton(btnConsultarContratacoes);
    }


}
