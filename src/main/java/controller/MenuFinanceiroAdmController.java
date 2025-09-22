package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;



public class MenuFinanceiroAdmController {
    public BorderPane root;

    @FXML
    private Pane contentArea;

    @FXML
    private Button btnRegrasSalariais;

    @FXML
    private Button btnCadastroFuncionario;

    @FXML
    private Button btnFolhaPagamento;

    @FXML
    private Button btnRelatorioFinanceiro;

    @FXML
    private Button btnContracheques;

    private Button activeButton;

    @FXML
    public void initialize() {

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
    private void showRegrasSalariais() {
        loadUI("RegrasSalariais"); 
        setActiveButton(btnRegrasSalariais);
    }

    @FXML
    private void showCadastroFuncionario() {
        loadUI("CadastroDeFuncionario");
        setActiveButton(btnCadastroFuncionario);
    }

    @FXML
    private void showFolhaPagamento() {
        loadUI("FolhaDePagamento");
        setActiveButton(btnFolhaPagamento);
    }

    @FXML
    private void showRelatorioFinanceiro() {
        loadUI("RelatorioFinanceiro");
        setActiveButton(btnRelatorioFinanceiro);
    }

    @FXML
    private void showContraCheques() {
        loadUI("Contracheque");
        setActiveButton(btnContracheques);
    }

}
