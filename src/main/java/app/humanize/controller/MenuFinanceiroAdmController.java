package app.humanize.controller;

import app.humanize.util.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

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

    private ResourceBundle bundle;

    @FXML
    public void initialize() {
        this.bundle = UserSession.getInstance().getBundle();
        showRegrasSalariais();
    }

    private void loadUI(String fxml) {
        try {
            String fxmlPath = "/view/" + fxml + ".fxml";
            URL resource = getClass().getResource(fxmlPath);
            System.out.println(bundle.getString("log.info.fxmlLoading") + resource);

            if (resource == null) {
                throw new IllegalStateException(bundle.getString("exception.fxmlNotFound.generic") + fxmlPath);
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
                    bundle.getString("alert.error.reload.title"),
                    bundle.getString("alert.error.fxmlNotFound.header"),
                    e.getMessage()
            );
        } catch (Exception e) {
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
    private void showRegrasSalariais() {
        loadUI("RegrasSalariais");
        setActiveButton(btnRegrasSalariais);
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