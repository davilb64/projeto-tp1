package app.humanize.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import app.humanize.model.Candidato;

import java.io.IOException;
import java.util.Objects;

public class CandidatosAdmController {
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

            // 🔹 Se o FXML carregado tiver um controller que aceite o pai, passa a referência
            Object childController = loader.getController();
            if (childController instanceof CadastroDeCandidatoController cadastroController) {
                cadastroController.setControllerPai(this);
            } else if (childController instanceof StatusDaCandidaturaController statusController) {
                statusController.setControllerPai(this);
            }

            contentArea.getChildren().setAll(view);
            //view.getProperties().put("controllerPai", this);

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
    public void showCadastro() {
        loadUI("/view/CadastroDeCandidato.fxml");
        setActiveButton(btnCadastro);
    }

    @FXML
    public void showCandidatura() {
        loadUI("/view/CandidaturaAVaga.fxml");
        setActiveButton(btnCandidatura);
    }

    @FXML
    public void showStatus() {
        loadUI("/view/StatusDaCandidatura.fxml");
        setActiveButton(btnStatus);
    }

    public void editarCandidatoExistente(Candidato candidato) {
        try {
            // Carrega a tela de cadastro
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CadastroDeCandidato.fxml"));
            Node view = loader.load();

            // Obtém o controller da tela de cadastro
            CadastroDeCandidatoController controller = loader.getController();

            // Preenche os campos com o candidato selecionado
            controller.prepararParaEdicao(candidato);

            // Troca o conteúdo do StackPane
            contentArea.getChildren().setAll(view);

            // Atualiza o botão ativo na lateral
            setActiveButton(btnCadastro);

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Erro ao abrir cadastro", "Falha ao carregar a tela de edição.", e.getMessage());
        }
    }



}
