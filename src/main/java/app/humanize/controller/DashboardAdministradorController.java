package app.humanize.controller;

import app.humanize.model.Candidatura;
import app.humanize.model.Perfil;
import app.humanize.model.StatusCandidatura;
import app.humanize.model.Usuario;
import app.humanize.repository.CandidatoRepository;
import app.humanize.repository.CandidaturaRepository;
import app.humanize.repository.UsuarioRepository;
import app.humanize.repository.VagaRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardAdministradorController {

    @FXML private Label lblTotalUsuarios;
    @FXML private Label lblTotalCandidatos;
    @FXML private Label lblVagasAbertas;
    @FXML private PieChart chartCandidaturas;
    @FXML private BarChart<String,Number> chartUsuarios;

    private PrincipalAdministradorController mainController;

    public void setMainController(PrincipalAdministradorController mainController) {
        this.mainController = mainController;
    }

    private final UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();
    private final VagaRepository vagaRepository = VagaRepository.getInstance();
    private final CandidatoRepository  candidatoRepository = CandidatoRepository.getInstance();
    private final CandidaturaRepository candidaturaRepository = CandidaturaRepository.getInstance();

    public void initialize(){
        lblVagasAbertas.setText(Integer.toString(vagaRepository.getQtdVaga()));
        lblTotalUsuarios.setText(Integer.toString(usuarioRepository.getQtdUsuarios()));
        lblTotalCandidatos.setText(Integer.toString(candidatoRepository.getQtdCandidatos()));
        carregarGraficoUsuariosPorPerfil();
        carregarGraficoCandidaturas();
    }

    private void carregarGraficoCandidaturas(){
        //pega todas candidaturas
        List<Candidatura> todasCandidaturas = candidaturaRepository.getTodas();

        //agrupa por status
        Map<StatusCandidatura, Long> contagemStatus = todasCandidaturas.stream()
                .collect(Collectors.groupingBy(Candidatura::getStatus, Collectors.counting()));

        //lista para o gráfico
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        //popula gráfico
        for(Map.Entry<StatusCandidatura, Long> entry : contagemStatus.entrySet()){
            //fatia nova
            pieChartData.add(new PieChart.Data(entry.getKey().toString(), entry.getValue()));
        }

        // define os dados no gráfico
        chartCandidaturas.setData(pieChartData);

        //ver os valores
        pieChartData.forEach(data -> {
            String percentual = String.format("%.1f%%", (data.getPieValue() / todasCandidaturas.size()) * 100);
            Tooltip.install(data.getNode(), new Tooltip(
                    data.getName() + ": " + (int)data.getPieValue() + " (" + percentual + ")"
            ));
        });
    }

    private void carregarGraficoUsuariosPorPerfil() {
        List<Usuario> todosUsuarios = usuarioRepository.getTodosUsuarios();

        Map<Perfil, Long> contagemPorPerfil = todosUsuarios.stream()
                .collect(Collectors.groupingBy(Usuario::getPerfil, Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Quantidade de Usuários");

        for (Map.Entry<Perfil, Long> entry : contagemPorPerfil.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey().toString(), entry.getValue()));
        }

        chartUsuarios.getData().clear();
        chartUsuarios.getData().add(series);
    }

    @FXML public void criarUsuarios() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CadastroUsuarioAdm.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Cadastrar Usuário");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        mainController.showUsuarios();
    }

    @FXML public void relatorios(){
        mainController.showRelatorios();
    }
}
