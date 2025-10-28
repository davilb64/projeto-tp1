package app.humanize.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.Objects;

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

    private void loadUI(String fxmlPath) {
        try {
            System.out.println(">> Tentando carregar: " + getClass().getResource(fxmlPath));

            Node view = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));

            if (view != null) {
                contentArea.getChildren().setAll(view);
            } else {
                System.err.println("Erro: FXML não carregado ou nulo: " + fxmlPath);
                mostrarAlerta("Erro Crítico", "Não foi possível carregar a tela: " + fxmlPath, "Verifique o console.");
            }

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
    private void showCadastrarAprovados() {
        loadUI("/view/CadastroDeFuncionario.fxml");
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
