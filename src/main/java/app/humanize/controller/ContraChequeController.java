package app.humanize.controller;

import app.humanize.model.ContraCheque;
import app.humanize.model.Funcionario;
import app.humanize.model.Usuario;
import app.humanize.repository.ContrachequeRepository;
import app.humanize.repository.UsuarioRepository;
// --- NOVOS IMPORTS ---
import app.humanize.service.formatters.IReportFormatter;
import app.humanize.service.formatters.PdfFormatter;
import app.humanize.service.relatorios.ReportData;
// --- FIM NOVOS IMPORTS ---
import app.humanize.util.UserSession;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser; // NOVO
import javafx.stage.Stage; // NOVO

import java.io.File; // NOVO
import java.io.FileOutputStream; // NOVO
import java.io.IOException; // NOVO
import java.time.format.DateTimeFormatter; // NOVO
import java.util.ArrayList; // NOVO
import java.util.Comparator; // NOVO
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors; // NOVO

public class ContraChequeController {

    // --- FXML (Atualizado) ---
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
    private ResourceBundle bundle;

    // --- NOVOS ATRIBUTOS ---
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

        // Configura os botões de ação
        btnBuscar.setOnAction(e -> buscarContraCheques());
        btnImprimir.setOnAction(e -> imprimirSelecionado());
        btnExportarTabela.setOnAction(e -> exportarTabela());
    }

    private void configurarColunasTabela() {
        colunaEmissao.setCellValueFactory(new PropertyValueFactory<>("dataEmissao"));
        colunaProventos.setCellValueFactory(new PropertyValueFactory<>("totalProventos"));
        colunaDescon.setCellValueFactory(new PropertyValueFactory<>("totalDescontos"));
        colunaSald.setCellValueFactory(new PropertyValueFactory<>("saldo"));

        // Formata as colunas de moeda
        formatarColunaMoeda(colunaProventos);
        formatarColunaMoeda(colunaDescon);
        formatarColunaMoeda(colunaSald);
    }

    // NOVO: Popula o ComboBox de Funcionários
    private void carregarFuncionariosComboBox() {
        listaFuncionarios.clear();

        List<Funcionario> funcionarios = usuarioRepository.getTodosUsuarios().stream()
                .filter(usuario -> usuario instanceof Funcionario)
                .map(usuario -> (Funcionario) usuario)
                .sorted(Comparator.comparing(Usuario::getNome)) // Ordena por nome
                .collect(Collectors.toList());

        listaFuncionarios.addAll(funcionarios);
        cbFuncionario.setItems(listaFuncionarios);

        // Define como exibir o Funcionario no ComboBox (mostrar apenas o nome)
        cbFuncionario.setCellFactory(lv -> new ListCell<Funcionario>() {
            @Override
            protected void updateItem(Funcionario func, boolean empty) {
                super.updateItem(func, empty);
                setText(empty || func == null ? null : func.getNome());
            }
        });

        // Define como exibir o Funcionario quando selecionado
        cbFuncionario.setButtonCell(new ListCell<Funcionario>() {
            @Override
            protected void updateItem(Funcionario func, boolean empty) {
                super.updateItem(func, empty);
                setText(empty || func == null ? null : func.getNome());
            }
        });
    }

    // NOVO: Configura listeners para automação da UI
    private void configurarListenersEBindings() {
        // Listener para o ComboBox
        cbFuncionario.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, func) -> {
            if (func != null) {
                txtCargo.setText(func.getCargo());
                txtDepartamento.setText(func.getDepartamento());
                tabelaContracheque.getItems().clear(); // Limpa tabela antiga
            } else {
                txtCargo.clear();
                txtDepartamento.clear();
                tabelaContracheque.getItems().clear();
            }
        });

        // Binds dos botões
        btnBuscar.disableProperty().bind(cbFuncionario.getSelectionModel().selectedItemProperty().isNull());
        btnImprimir.disableProperty().bind(tabelaContracheque.getSelectionModel().selectedItemProperty().isNull());

        // --- LINHA CORRIGIDA ---
        // Troca .emptyProperty() por Bindings.isEmpty()
        btnExportarTabela.disableProperty().bind(
                Bindings.isEmpty(tabelaContracheque.getItems())
        );
        // --- FIM DA CORREÇÃO ---
    }

    // RENOMEADO E ATUALIZADO (era exportarContraCheque)
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

        carregarContraCheques(funcionario.getNome());
    }

    private void carregarContraCheques(String nomeFuncionario) {
        List<ContraCheque> contraChequesDoFuncionario = contraChequeRepository.carregarContraChequesPorFuncionario(nomeFuncionario);
        ObservableList<ContraCheque> dados = FXCollections.observableArrayList(contraChequesDoFuncionario);
        tabelaContracheque.setItems(dados);

        if (contraChequesDoFuncionario.isEmpty()) {
            mostrarAlerta(
                    bundle.getString("payslip.alert.noPayslips.title"),
                    bundle.getString("payslip.alert.noPayslips.content") + nomeFuncionario,
                    Alert.AlertType.INFORMATION
            );
        }
    }

    // RENOMEADO E ATUALIZADO (era imprimirContraCheque)
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

    // NOVO: Exporta a tabela inteira (histórico)
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

    // --- NOVOS MÉTODOS HELPER PARA EXPORTAÇÃO ---

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

    private File salvarArquivoFisico(ReportData dados, String nomeBase, IReportFormatter formatador) throws IOException {
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
                return file;
            } catch (IOException e) {
                throw new IOException(bundle.getString("reportsAdmin.alert.saveError.header") + e.getMessage(), e);
            }
        } else {
            return null;
        }
    }

    // Helper para formatar moeda na tabela
    private void formatarColunaMoeda(TableColumn<ContraCheque, Double> coluna) {
        coluna.setCellFactory(tc -> new TableCell<ContraCheque, Double>() {
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