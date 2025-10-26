package app.humanize.controller;

import app.humanize.repository.VagaRepository;
import app.humanize.util.ScreenController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.security.Principal;

public class DashboardGestorController {
    

    private final VagaRepository vagaRepository = VagaRepository.getInstance();
    public Button btnAutorizar;
    public Button btnAtribuir;
    public Button btnCriarVaga;
    public LineChart chartEvolucao;
    public BarChart chartFolha;
    public PieChart chartRegime;
    public Label lblUltimaFolha;
    public Label lblFuncionarios;
    public Label lblEntrevistas;
    public Label lblSolicitacoes;
    public Label lblTotalCandidatos;
    public Label lblVagasAbertas;

    private PrincipalGestorController mainController;

    public void setMainController(PrincipalGestorController mainController) {
        this.mainController = mainController;
    }

    @FXML public void initialize() {
        lblVagasAbertas.setText(Integer.toString(vagaRepository.getQtdVaga()));
    }

    @FXML public void criarVaga() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CriarVaga.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Cadastrar Vaga");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        mainController.showVagas();
    }


}
