package app.humanize.controller;

import app.humanize.model.Candidatura;
import app.humanize.model.Perfil;
import app.humanize.model.StatusCandidatura;
import app.humanize.model.Usuario;
import app.humanize.repository.CandidatoRepository;
import app.humanize.repository.CandidaturaRepository;
import app.humanize.repository.UsuarioRepository;
import app.humanize.repository.VagaRepository;
import app.humanize.util.UserSession;
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
import java.util.ResourceBundle;
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
    private ResourceBundle bundle;

    public void initialize(){
        this.bundle = UserSession.getInstance().getBundle();
        lblVagasAbertas.setText(Integer.toString(vagaRepository.getQtdVaga()));
        lblTotalUsuarios.setText(Integer.toString(usuarioRepository.getQtdUsuarios()));
        lblTotalCandidatos.setText(Integer.toString(candidatoRepository.getQtdCandidatos()));
        carregarGraficoUsuariosPorPerfil();
        carregarGraficoCandidaturas();
    }

    // Método auxiliar para traduzir StatusCandidatura
    private String getTraducaoStatus(StatusCandidatura status) {
        String key = "statusCandidatura." + status.name();
        return bundle.containsKey(key) ? bundle.getString(key) : status.name();
    }

    // Método auxiliar para traduzir Perfil
    private String getTraducaoPerfil(Perfil perfil) {
        String key = "perfil." + perfil.name();
        return bundle.containsKey(key) ? bundle.getString(key) : perfil.name();
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
            //fatia nova com nome traduzido
            pieChartData.add(new PieChart.Data(getTraducaoStatus(entry.getKey()), entry.getValue()));
        }

        // define os dados no gráfico
        chartCandidaturas.setData(pieChartData);

        //ver os valores
        pieChartData.forEach(data -> {
            String percentual = String.format("%.1f%%", (data.getPieValue() / todasCandidaturas.size()) * 100);
            String tooltipText = data.getName() + ": " + (int)data.getPieValue() + " (" + percentual + ")";
            Tooltip.install(data.getNode(), new Tooltip(tooltipText));
        });
    }

    private void carregarGraficoUsuariosPorPerfil() {
        List<Usuario> todosUsuarios = usuarioRepository.getTodosUsuarios();

        Map<Perfil, Long> contagemPorPerfil = todosUsuarios.stream()
                .collect(Collectors.groupingBy(Usuario::getPerfil, Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(bundle.getString("dashboard.chart.users.seriesName"));

        for (Map.Entry<Perfil, Long> entry : contagemPorPerfil.entrySet()) {
            series.getData().add(new XYChart.Data<>(getTraducaoPerfil(entry.getKey()), entry.getValue()));
        }

        chartUsuarios.getData().clear();
        chartUsuarios.getData().add(series);
    }

    @FXML public void criarUsuarios() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CadastroUsuarioAdm.fxml"));
        loader.setResources(bundle); // Passa o bundle para a próxima tela
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle(bundle.getString("dashboard.window.createUser.title"));
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        mainController.showUsuarios();
    }

    @FXML public void relatorios(){
        mainController.showRelatorios();
    }
}