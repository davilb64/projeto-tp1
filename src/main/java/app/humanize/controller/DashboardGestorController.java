package app.humanize.controller;

import app.humanize.model.*;
import app.humanize.repository.CandidaturaRepository;
import app.humanize.repository.EntrevistaRepository;
import app.humanize.repository.UsuarioRepository;
import app.humanize.repository.VagaRepository;
import app.humanize.util.UserSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    @FXML private TableView<Candidatura> tblAprovados;
    @FXML private TableColumn<Candidatura, LocalDate> colDataAprovado;
    @FXML private TableColumn<Candidatura, String> colCandidatoAprovado;
    @FXML private TableColumn<Candidatura, Void> colAcaoAprovado;

    private final VagaRepository vagaRepository = VagaRepository.getInstance();
    private final UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();
    private final EntrevistaRepository entrevistaRepository = EntrevistaRepository.getInstance();
    private final CandidaturaRepository candidaturaRepository = CandidaturaRepository.getInstance();

    private PrincipalGestorController mainController;
    private ResourceBundle bundle;

    public void setMainController(PrincipalGestorController mainController) {
        this.mainController = mainController;
    }

    @FXML public void initialize() {
        this.bundle = UserSession.getInstance().getBundle();

        lblFuncionarios.setText(Integer.toString(usuarioRepository.getQtdUsuarios()));
        lblVagasAbertas.setText(Integer.toString(vagaRepository.getVagasAbertas().size()));
        lblEntrevistas.setText(String.valueOf(entrevistaRepository.getEntrevistasHoje().size()));
        lblTotalCandidatos.setText(String.valueOf(candidaturaRepository.getCandidaturasEmAnalise().size()));
        lblSolicitacoes.setText(String.valueOf(entrevistaRepository.buscarCandidatosAprovados().size()));
        carregarGraficoRegime();
        carregarGraficoEvolucao();
        carregarTabelaAprovados();
        configurarColunaAcao();
    }

    private void configurarColunaAcao() {
        Callback<TableColumn<Candidatura, Void>, TableCell<Candidatura, Void>> cellFactory = param -> {
            final TableCell<Candidatura, Void> cell = new TableCell<>() {

                private final String contratarText = bundle.containsKey("dashboard.gestor.btn.hire")
                        ? bundle.getString("dashboard.gestor.btn.hire") : "Contratar";
                private final String recusarText = bundle.containsKey("dashboard.gestor.btn.reject")
                        ? bundle.getString("dashboard.gestor.btn.reject") : "Recusar";

                private final Button btnContratar = new Button(contratarText);
                private final Button btnRecusar = new Button(recusarText);
                private final VBox pane = new VBox(5, btnContratar, btnRecusar);
                {
                    btnContratar.setOnAction(event -> {
                        Candidatura candidatura = getTableView().getItems().get(getIndex());
                        try {
                            handleContratar(candidatura);
                        } catch (IOException e) {
                            e.printStackTrace();
                            mostrarAlerta("Erro", "Falha ao abrir tela de contratação", e.getMessage());
                        }
                    });

                    btnRecusar.setOnAction(event -> {
                        Candidatura candidatura = getTableView().getItems().get(getIndex());
                        handleRecusar(candidatura);
                    });

                    btnContratar.getStyleClass().add("primary-action-button");
                    btnRecusar.getStyleClass().add("danger-action-button");
                }

                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(pane);
                    }
                }
            };
            return cell;
        };

        colAcaoAprovado.setCellFactory(cellFactory);
    }

    private void handleContratar(Candidatura candidatura) throws IOException {
        URL resource = getClass().getResource("/view/ContratarFuncionario.fxml");
        if (resource == null) {
            mostrarAlerta(bundle.getString("employeeManagement.alert.fxmlHireNotFound"));
            return;
        }

        FXMLLoader loader = new FXMLLoader(resource, bundle);
        Parent root = loader.load();
        ContratarFuncionarioController controller = loader.getController();
        controller.iniciarComCandidato(candidatura.getCandidato());
        Stage stage = new Stage();
        stage.setTitle(bundle.getString("employeeHire.title"));
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        carregarTabelaAprovados();
        lblSolicitacoes.setText(String.valueOf(candidaturaRepository.getCandidaturasAprovadas().size()));
    }


private void mostrarAlerta(String mensagem) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(bundle.getString("userManagement.alert.attention"));
    alert.setHeaderText(null);
    alert.setContentText(mensagem);
    alert.showAndWait();
}
    private void handleRecusar(Candidatura candidatura) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Recusa");
        alert.setHeaderText("Recusar " + candidatura.getCandidato().getNome() + "?");
        alert.setContentText("Esta ação irá alterar o status do candidato para REPROVADO. Deseja continuar?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                candidatura.setStatus(StatusCandidatura.REPROVADO);
                candidaturaRepository.salvarOuAtualizar(candidatura);

                carregarTabelaAprovados();
                lblSolicitacoes.setText(String.valueOf(candidaturaRepository.getCandidaturasAprovadas().size()));

            } catch (IOException e) {
                mostrarAlerta("Erro", "Erro ao salvar alteração", e.getMessage());
            }
        }
    }

    private void mostrarAlerta(String titulo, String cabecalho, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(conteudo);
        alert.showAndWait();
    }

    private void carregarTabelaAprovados() {
        colDataAprovado.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("dataCandidatura"));

        colCandidatoAprovado.setCellValueFactory(cellData -> {
            Candidato candidato = cellData.getValue().getCandidato();
            return new SimpleStringProperty(candidato.getNome());
        });

        List<Candidatura> aprovados = candidaturaRepository.getCandidaturasAprovadas();

        tblAprovados.setItems(FXCollections.observableArrayList(aprovados));
    }

    private List<LocalDate> getDatasContratacao() {
        return this.usuarioRepository.getTodosUsuarios().stream()
                .filter(u -> u instanceof Funcionario)
                .map(u -> (Funcionario) u)
                .map(Funcionario::getDataAdmissao)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void carregarGraficoEvolucao() {
        List<LocalDate> datasContratacao = getDatasContratacao();

        Map<YearMonth, Long> contratacoesPorMes = datasContratacao.stream()
                .collect(Collectors.groupingBy(
                        YearMonth::from,
                        TreeMap::new,
                        Collectors.counting()
                ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        series.setName(bundle.getString("dashboard.gestor.chart.hires.legend"));

        long totalAcumulado = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM/yy");

        for (Map.Entry<YearMonth, Long> entry : contratacoesPorMes.entrySet()) {
            totalAcumulado += entry.getValue();
            String labelMes = entry.getKey().format(formatter);
            XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(labelMes, totalAcumulado);
            series.getData().add(dataPoint);

            String tooltipText = String.format(
                    bundle.getString("dashboard.gestor.chart.hires.tooltip"),
                    labelMes,
                    totalAcumulado
            );
            Tooltip.install(dataPoint.getNode(), new Tooltip(tooltipText));
        }

        chartEvolucao.getData().add(series);
        chartEvolucao.setLegendVisible(false);
    }

    private String getTraducaoRegime(Regime regime) {
        if (regime == null) return "";
        String key = "regime." + regime.name();
        return bundle.containsKey(key) ? bundle.getString(key) : regime.name();
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
            pieChartData.add(new PieChart.Data(getTraducaoRegime(entry.getKey()), entry.getValue()));
        }

        //define os dados no gráfico
        chartRegime.setData(pieChartData);

        //percentuais nas fatias
        pieChartData.forEach(data -> {
            String percentual = String.format("%.1f%%", (data.getPieValue() / totalFuncionarios.size()) * 100);
            String tooltipText = data.getName() + ": " + (int)data.getPieValue() + " (" + percentual + ")";
            Tooltip.install(data.getNode(), new Tooltip(tooltipText));
        });
    }

    @FXML public void criarVaga() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CriarVaga.fxml"));
        loader.setResources(bundle);
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle(bundle.getString("dashboard.gestor.window.createJob.title"));
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        mainController.showVagas();
    }


    @FXML public void autorizarContratacao() {
        mainController.showFuncionarios();
    }


}