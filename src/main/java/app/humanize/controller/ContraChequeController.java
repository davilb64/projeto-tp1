package app.humanize.controller;

import app.humanize.model.ContraCheque;
import app.humanize.model.FolhaPag;
import app.humanize.model.Funcionario;
import app.humanize.model.Usuario;
import app.humanize.repository.ContrachequeRepository;
import app.humanize.repository.FolhaPagRepository;
import app.humanize.repository.UsuarioRepository;
import app.humanize.service.formatters.IReportFormatter;
import app.humanize.service.formatters.PdfFormatter;
import app.humanize.service.relatorios.ReportData;
import app.humanize.util.UserSession;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class ContraChequeController {

    @FXML private ComboBox<Funcionario> cbFuncionario;
    @FXML private TextField txtCargo;
    @FXML private TextField txtDepartamento;
    @FXML private Button btnBuscar;
    @FXML private Button btnImprimir;
    @FXML private Button btnExportarTabela;

    @FXML private TableView<ContraCheque> tabelaContracheque;
    @FXML private TableColumn<ContraCheque, String> colunaEmissao;
    @FXML private TableColumn<ContraCheque, Double> colunaProventos;
    @FXML private TableColumn<ContraCheque, Double> colunaDescon;
    @FXML private TableColumn<ContraCheque, Double> colunaSald;

    private final UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();
    private final ContrachequeRepository contraChequeRepository = ContrachequeRepository.getInstance();
    private final FolhaPagRepository folhaPagRepository = FolhaPagRepository.getInstance();

    private ResourceBundle bundle;

    private final ObservableList<Funcionario> listaFuncionarios = FXCollections.observableArrayList();
    private final IReportFormatter formatadorPdf = new PdfFormatter();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private String currencyFormat;

    @FXML
    public void initialize() {
        this.bundle = UserSession.getInstance().getBundle();
        this.currencyFormat = bundle.getString("financialReport.currencyFormat");

        configurarColunasTabela();
        carregarFuncionariosComboBox();
        configurarListenersEBindings();

        btnBuscar.setOnAction(e -> buscarContraCheques());
        btnImprimir.setOnAction(e -> imprimirSelecionado());
        btnExportarTabela.setOnAction(e -> exportarTabela());
    }

    private void configurarColunasTabela() {
        colunaEmissao.setCellValueFactory(new PropertyValueFactory<>("dataEmissao"));
        colunaProventos.setCellValueFactory(new PropertyValueFactory<>("totalProventos"));
        colunaDescon.setCellValueFactory(new PropertyValueFactory<>("totalDescontos"));
        colunaSald.setCellValueFactory(new PropertyValueFactory<>("saldo"));

        formatarColunaMoeda(colunaProventos);
        formatarColunaMoeda(colunaDescon);
        formatarColunaMoeda(colunaSald);
    }

    private void carregarFuncionariosComboBox() {
        listaFuncionarios.clear();

        List<Funcionario> funcionarios = usuarioRepository.getTodosUsuarios().stream()
                .filter(usuario -> usuario instanceof Funcionario)
                .map(usuario -> (Funcionario) usuario)
                .sorted(Comparator.comparing(Usuario::getNome))
                .toList();

        listaFuncionarios.addAll(funcionarios);
        cbFuncionario.setItems(listaFuncionarios);

        cbFuncionario.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Funcionario func, boolean empty) {
                super.updateItem(func, empty);
                setText(empty || func == null ? null : func.getNome());
            }
        });

        cbFuncionario.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Funcionario func, boolean empty) {
                super.updateItem(func, empty);
                setText(empty || func == null ? null : func.getNome());
            }
        });
    }

    private void configurarListenersEBindings() {
        cbFuncionario.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, func) -> {
            if (func != null) {
                txtCargo.setText(func.getCargo());
                txtDepartamento.setText(func.getDepartamento());
                tabelaContracheque.getItems().clear();

                buscarDadosFolhaPorNome(func.getNome());
            } else {
                txtCargo.clear();
                txtDepartamento.clear();
                tabelaContracheque.getItems().clear();
            }
        });

        btnBuscar.disableProperty().bind(cbFuncionario.getSelectionModel().selectedItemProperty().isNull());
        btnImprimir.disableProperty().bind(tabelaContracheque.getSelectionModel().selectedItemProperty().isNull());
        btnExportarTabela.disableProperty().bind(
                Bindings.isEmpty(tabelaContracheque.getItems())
        );
    }


    private void buscarDadosFolhaPorNome(String nomeFuncionario) {
        try {
            List<FolhaPag> folhas = folhaPagRepository.carregarTodasFolhas();
            List<FolhaPag> folhasDoFuncionario = folhas.stream()
                    .filter(folha -> folha.getNome().equalsIgnoreCase(nomeFuncionario))
                    .toList();

            ObservableList<ContraCheque> contraCheques = FXCollections.observableArrayList();

            for (FolhaPag folha : folhasDoFuncionario) {
                ContraCheque contraCheque = new ContraCheque();
                contraCheque.setNomeFuncionario(folha.getNome());
                contraCheque.setDataEmissao(folha.getData());
                contraCheque.setTotalProventos(folha.getSalarioBase());
                contraCheque.setTotalDescontos(folha.getDescontos());
                contraCheque.setSaldo(folha.getSalarioLiquido());

                contraCheques.add(contraCheque);
            }

            tabelaContracheque.setItems(contraCheques);

        } catch (Exception e) {
            System.err.println("Erro ao buscar dados da folha: " + e.getMessage());
        }
    }

    @FXML
    private void buscarContraCheques() {
        Funcionario funcionario = cbFuncionario.getSelectionModel().getSelectedItem();
        if (funcionario == null) {
            mostrarAlerta(
                    bundle.getString("payslip.alert.emptyFields.title"),
                    bundle.getString("payslip.alert.emptyFields.content"),
                    Alert.AlertType.WARNING
            );
            return;
        }

        // AGORA USA A BUSCA AUTOMÁTICA DA FOLHA
        buscarDadosFolhaPorNome(funcionario.getNome());
    }

    // O resto do código permanece igual...
    @FXML
    private void imprimirSelecionado() {
        ContraCheque cheque = tabelaContracheque.getSelectionModel().getSelectedItem();
        if (cheque == null) {
            mostrarAlerta(bundle.getString("payslip.alert.noSelection.title"),
                    bundle.getString("payslip.alert.noSelection.contentPrint"),
                    Alert.AlertType.WARNING);
            return;
        }

        try {
            ReportData dados = criarReportDataParaUmCheque(cheque);
            String nomeBase = "Contracheque_" + cheque.getNomeFuncionario().replace(" ", "_") + "_" + cheque.getDataEmissao().toString();
            salvarArquivoFisico(dados, nomeBase, formatadorPdf);
        } catch (Exception e) {
            mostrarAlerta(bundle.getString("reportsAdmin.alert.exportError.title"), e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void exportarTabela() {
        ObservableList<ContraCheque> lista = tabelaContracheque.getItems();
        if (lista.isEmpty()) {
            mostrarAlerta(bundle.getString("payslip.alert.noData.title"),
                    bundle.getString("payslip.alert.noData.contentExport"),
                    Alert.AlertType.WARNING);
            return;
        }

        try {
            ReportData dados = criarReportDataParaTabela(lista, cbFuncionario.getValue());
            String nomeBase = "Historico_" + cbFuncionario.getValue().getNome().replace(" ", "_");
            salvarArquivoFisico(dados, nomeBase, formatadorPdf);
        } catch (Exception e) {
            mostrarAlerta(bundle.getString("reportsAdmin.alert.exportError.title"), e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private ReportData criarReportDataParaUmCheque(ContraCheque cheque) {
        String dataFormatada = cheque.getDataEmissao().format(dateFormatter);
        String titulo = String.format(bundle.getString("report.payslip.title"), dataFormatada);

        List<String> headers = List.of(
                bundle.getString("report.payslip.header.field"),
                bundle.getString("report.payslip.header.value")
        );
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of(bundle.getString("report.payslip.field.name"), cheque.getNomeFuncionario()));
        rows.add(List.of(bundle.getString("report.payslip.field.date"), dataFormatada));
        rows.add(List.of("---", "---"));
        rows.add(List.of(bundle.getString("payslip.detail.additions"), String.format(currencyFormat, cheque.getTotalProventos())));
        rows.add(List.of(bundle.getString("payslip.detail.deductions"), String.format(currencyFormat, cheque.getTotalDescontos())));
        rows.add(List.of("---", "---"));
        rows.add(List.of(bundle.getString("report.payslip.field.netTotal"), String.format(currencyFormat, cheque.getSaldo())));

        return new ReportData(titulo, headers, rows);
    }

    private ReportData criarReportDataParaTabela(ObservableList<ContraCheque> cheques, Funcionario func) {
        String titulo = String.format(bundle.getString("report.financialHistory.title"), func.getNome());

        List<String> headers = List.of(
                bundle.getString("report.financialHistory.header.date"),
                bundle.getString("report.financialHistory.header.gross"),
                bundle.getString("report.financialHistory.header.deductions"),
                bundle.getString("report.financialHistory.header.net")
        );

        List<List<String>> rows = new ArrayList<>();
        for (ContraCheque cheque : cheques) {
            rows.add(List.of(
                    cheque.getDataEmissao().format(dateFormatter),
                    String.format(currencyFormat, cheque.getTotalProventos()),
                    String.format(currencyFormat, cheque.getTotalDescontos()),
                    String.format(currencyFormat, cheque.getSaldo())
            ));
        }
        return new ReportData(titulo, headers, rows);
    }

    private void salvarArquivoFisico(ReportData dados, String nomeBase, IReportFormatter formatador) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("reportsAdmin.saveDialog.title"));
        fileChooser.setInitialFileName(nomeBase + formatador.getExtensao());

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                formatador.getDescricaoFiltro(), "*" + formatador.getExtensao());
        fileChooser.getExtensionFilters().add(extFilter);

        Stage currentStage = (Stage) tabelaContracheque.getScene().getWindow();
        File file = fileChooser.showSaveDialog(currentStage);

        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(formatador.formatar(dados));
                mostrarAlerta(
                        bundle.getString("alert.success.title"),
                        bundle.getString("payroll.alert.exportSuccess.header"),
                        Alert.AlertType.INFORMATION
                );
            } catch (IOException e) {
                throw new IOException(bundle.getString("reportsAdmin.alert.saveError.header") + e.getMessage(), e);
            }
        }
    }

    private void formatarColunaMoeda(TableColumn<ContraCheque, Double> coluna) {
        coluna.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double valor, boolean vazio) {
                super.updateItem(valor, vazio);
                if (vazio || valor == null) {
                    setText(null);
                } else {
                    setText(String.format(currencyFormat, valor));
                }
            }
        });
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}