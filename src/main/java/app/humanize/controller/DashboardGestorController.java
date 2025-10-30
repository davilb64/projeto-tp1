package app.humanize.controller;

import app.humanize.model.Funcionario;
import app.humanize.model.Regime;
import app.humanize.model.Usuario;
import app.humanize.repository.CandidaturaRepository;
import app.humanize.repository.EntrevistaRepository;
import app.humanize.repository.UsuarioRepository;
import app.humanize.repository.VagaRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardGestorController {
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

    private final VagaRepository vagaRepository = VagaRepository.getInstance();
    private final UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();
    private final EntrevistaRepository entrevistaRepository = EntrevistaRepository.getInstance();
    private final CandidaturaRepository candidaturaRepository = CandidaturaRepository.getInstance();

    private PrincipalGestorController mainController;

    public void setMainController(PrincipalGestorController mainController) {
        this.mainController = mainController;
    }

    @FXML public void initialize() {

        lblFuncionarios.setText(Integer.toString(usuarioRepository.getQtdUsuarios()));
        lblVagasAbertas.setText(Integer.toString(vagaRepository.getVagasAbertas().size()));
        lblEntrevistas.setText(String.valueOf(entrevistaRepository.getEntrevistasHoje().size()));
        lblTotalCandidatos.setText(String.valueOf(candidaturaRepository.getCandidaturasEmAnalise().size()));
        lblSolicitacoes.setText(String.valueOf(entrevistaRepository.buscarCandidatosAprovados().size()));
        carregarGraficoRegime();
    }

    private void carregarGraficoRegime(){
        // busca apenas os usuários que são Funcionários
        List<Usuario> totalFuncionarios = usuarioRepository.getTodosUsuarios();

        // agrupa funcionarios por regime
        Map<Regime,Long> contagemPorRegime = totalFuncionarios.stream()
                .map(usuario -> (Funcionario) usuario)
                .filter(funcionario -> funcionario.getRegime() != null)
                .collect(Collectors.groupingBy(Funcionario::getRegime, Collectors.counting()));

        //cria a lista de dados para o gráfico
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        //popula o gráfico
        for (Map.Entry<Regime,Long> entry : contagemPorRegime.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey().toString(), entry.getValue()));
        }

        //define os dados no gráfico
        chartRegime.setData(pieChartData);

        //percentuais nas fatias
        pieChartData.forEach(data -> {
            String percentual = String.format("%.1f%%", (data.getPieValue() / totalFuncionarios.size()) * 100);
            Tooltip.install(data.getNode(), new Tooltip(
                    data.getName() + ": " + (int)data.getPieValue() + " (" + percentual + ")"
            ));
        });
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

    @FXML public void atribuirRecrutador() throws IOException {
        mainController.showRecrutadores();
    }

    @FXML public void autorizarContratacao() throws IOException {
        mainController.showFuncionarios();
    }


}
