package app.humanize.controller;

import app.humanize.model.FolhaPag;
import app.humanize.model.RelatorioFinanceiro;
import app.humanize.repository.FolhaPagRepository;
import app.humanize.repository.RelatorioFinanceiroRepository;
import app.humanize.service.formatters.IReportFormatter;
import app.humanize.service.formatters.PdfFormatter;
import app.humanize.service.relatorios.IGeradorRelatorio;
import app.humanize.service.relatorios.RelatorioFinanceiroGeral;
import app.humanize.service.relatorios.ReportData;
import app.humanize.util.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

public class RelatorioFinanceiroController {

    @FXML private DatePicker dateTransacao;
    @FXML private TextField txtValor;
    @FXML private TextField txtDescricao;

    @FXML private RadioButton radioReceita;
    @FXML private RadioButton radioDespesa;

    @FXML private Button btnSalvar;
    @FXML private Button btnLimpar;
    @FXML private Button btnSalvarRelatorio;
    @FXML private Button btnExportarPDF;

    @FXML private TableView<RelatorioFinanceiro> tabelaRelatorio;
    @FXML private TableColumn<RelatorioFinanceiro, String> colData;
    @FXML private TableColumn<RelatorioFinanceiro, String> colDescricao;
    @FXML private TableColumn<RelatorioFinanceiro, String> colreceita;
    @FXML private TableColumn<RelatorioFinanceiro, String> coldespesa;
    @FXML private TableColumn<RelatorioFinanceiro, String> colCategoria;
    @FXML private TableColumn<RelatorioFinanceiro, String> colSaldoFinal;

    private final ObservableList<RelatorioFinanceiro> transacoes = FXCollections.observableArrayList();
    private final FolhaPagRepository folhaRepository = FolhaPagRepository.getInstance();
    private final RelatorioFinanceiroRepository relatorioRepository = RelatorioFinanceiroRepository.getInstance();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private ResourceBundle bundle;
    private String currencySymbol;
    private String currencyFormat;

    private final IReportFormatter formatadorPdf = new PdfFormatter();

    @FXML
    public void initialize() {
        this.bundle = UserSession.getInstance().getBundle();
        this.currencySymbol = bundle.getString("financialReport.currencySymbol");
        this.currencyFormat = bundle.getString("financialReport.currencyFormat");

        radioReceita.setText(bundle.getString("financialReport.type.revenue"));
        radioDespesa.setText(bundle.getString("financialReport.type.expense"));

        tabelaRelatorio.setItems(transacoes);
        configurarColunas();
        configurarListeners();

        btnSalvar.setOnAction(event -> salvarTransacao());
        btnLimpar.setOnAction(event -> limparFormulario());
        btnSalvarRelatorio.setOnAction(event -> carregarRelatorioCompleto());
        btnExportarPDF.setOnAction(event -> exportarRelatorioPDF());

        inicializarRelatorio();
    }

    private void inicializarRelatorio() {
        try {
            relatorioRepository.criarArquivoSeNaoExiste();
            carregarRelatorioSalvo();
            carregarDespesasFolhaPagamento();
            calcularEAdicionarSaldoFinal();
            salvarTransacoesNoRepository();
        } catch (IOException e) {
            mostrarAlerta(bundle.getString("alert.error.title"),
                    bundle.getString("financialReport.alert.initError") + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void configurarColunas() {
        colData.setCellValueFactory(cellData -> cellData.getValue().dataProperty());
        colDescricao.setCellValueFactory(cellData -> cellData.getValue().descricaoProperty());
        colreceita.setCellValueFactory(cellData -> cellData.getValue().receitaProperty());
        coldespesa.setCellValueFactory(cellData -> cellData.getValue().despesasProperty());
        colCategoria.setCellValueFactory(cellData -> cellData.getValue().categoriaProperty());
        colSaldoFinal.setCellValueFactory(cellData -> cellData.getValue().saldoProperty());
    }

    private void configurarListeners() {
        txtValor.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[\\d,.]*")) {
                txtValor.setText(newValue.replaceAll("[^\\d,.]", ""));
            }
        });
    }

    private void carregarRelatorioSalvo() {
        var transacoesSalvas = relatorioRepository.carregarTransacoes();
        transacoes.addAll(transacoesSalvas);
    }

    private void carregarDespesasFolhaPagamento() {
        var folhas = folhaRepository.carregarTodasFolhas();

        Set<String> descricoesExistentes = new HashSet<>();
        for (RelatorioFinanceiro transacao : transacoes) {
            descricoesExistentes.add(transacao.getDescricao());
        }

        for (FolhaPag folha : folhas) {
            String descricaoFolha = String.format(
                    bundle.getString("financialReport.payroll.descriptionFormat"),
                    folha.getNome(), folha.getCargo(), folha.getNivel()
            );

            if (!descricoesExistentes.contains(descricaoFolha)) {
                String dataFolha = folha.getData() != null ?
                        folha.getData().format(dateFormatter) :
                        LocalDate.now().format(dateFormatter);

                RelatorioFinanceiro folhaTransacao = new RelatorioFinanceiro(
                        dataFolha,
                        descricaoFolha,
                        "",
                        String.format(currencyFormat, folha.getSalarioLiquido()),
                        "",
                        bundle.getString("financialReport.payroll.category")
                );
                transacoes.add(folhaTransacao);
            }
        }
    }

    private void salvarTransacao() {
        if (!validarCampos()) {
            return;
        }

        double valorDouble;
        try {
            valorDouble = extrairValorNumerico(txtValor.getText());
            if (valorDouble <= 0) {
                mostrarAlerta(bundle.getString("alert.error.title"),
                        bundle.getString("financialReport.alert.validation.positiveValue"), Alert.AlertType.WARNING);
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(bundle.getString("alert.error.format.title"),
                    String.format(bundle.getString("salaryRules.alert.validation.mustBeNumeric"), bundle.getString("financialReport.form.value")),
                    Alert.AlertType.ERROR);
            return;
        }

        String data = dateTransacao.getValue() != null ?
                dateTransacao.getValue().format(dateFormatter) :
                LocalDate.now().format(dateFormatter);

        String descricao = txtDescricao.getText();
        String tipo = radioReceita.isSelected() ?
                bundle.getString("financialReport.type.revenue") :
                bundle.getString("financialReport.type.expense");

        String receita = radioReceita.isSelected() ? String.format(currencyFormat, valorDouble) : "";
        String despesas = radioDespesa.isSelected() ? String.format(currencyFormat, valorDouble) : "";

        RelatorioFinanceiro transacao = new RelatorioFinanceiro(
                data, descricao, receita, despesas, "", tipo
        );

        transacoes.add(transacao);
        calcularEAdicionarSaldoFinal();
        salvarTransacoesNoRepository();
        limparFormulario();

        mostrarAlerta(bundle.getString("alert.success.title"),
                bundle.getString("financialReport.alert.saveSuccess"), Alert.AlertType.INFORMATION);
    }

    private void calcularEAdicionarSaldoFinal() {
        double saldoAcumulado = 0.0;
        final String saldoFinalDesc = bundle.getString("financialReport.finalBalance.description");

        transacoes.removeIf(t -> saldoFinalDesc.equals(t.getDescricao()));

        for (RelatorioFinanceiro transacao : transacoes) {
            double receita = extrairValorNumerico(transacao.getReceita());
            double despesa = extrairValorNumerico(transacao.getDespesas());
            saldoAcumulado += (receita - despesa);

            transacao.setSaldo(String.format(currencyFormat, saldoAcumulado));
        }

        String categoriaSaldo = saldoAcumulado >= 0 ?
                bundle.getString("financialReport.finalBalance.profit") :
                bundle.getString("financialReport.finalBalance.loss");

        RelatorioFinanceiro saldoFinalTransacao = new RelatorioFinanceiro(
                "", saldoFinalDesc, "", "", String.format(currencyFormat, saldoAcumulado), categoriaSaldo
        );
        transacoes.add(saldoFinalTransacao);
    }

    private double extrairValorNumerico(String valorString) throws NumberFormatException {
        if (valorString == null || valorString.trim().isEmpty()) {
            return 0.0;
        }
        try {
            String valorLimpo = valorString
                    .replace(currencySymbol, "")
                    .replace(".", "")
                    .replace(",", ".")
                    .trim();

            if (!valorLimpo.contains(".") && valorString.contains(".")) {
                valorLimpo = valorString.replace(currencySymbol, "").trim();
            }

            return Double.parseDouble(valorLimpo);
        } catch (NumberFormatException e) {
            throw e;
        }
    }

    private boolean validarCampos() {
        if (txtValor.getText().isEmpty()) {
            mostrarAlerta(bundle.getString("alert.error.title"),
                    bundle.getString("financialReport.alert.validation.valueRequired"), Alert.AlertType.WARNING);
            return false;
        }
        if (txtDescricao.getText().isEmpty()) {
            mostrarAlerta(bundle.getString("alert.error.title"),
                    bundle.getString("financialReport.alert.validation.descriptionRequired"), Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void limparFormulario() {
        txtValor.clear();
        txtDescricao.clear();
        dateTransacao.setValue(LocalDate.now());
        radioReceita.setSelected(true);
    }

    private void salvarTransacoesNoRepository() {
        try {
            List<RelatorioFinanceiro> paraSalvar = transacoes.stream()
                    .filter(t -> !t.getDescricao().equals(bundle.getString("financialReport.finalBalance.description")))
                    .toList();
            relatorioRepository.salvarTransacoes(paraSalvar);
        } catch (IOException e) {
            mostrarAlerta(bundle.getString("alert.error.title"),
                    bundle.getString("financialReport.alert.saveRepoError") + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void carregarRelatorioCompleto() {
        try {
            transacoes.clear();
            carregarRelatorioSalvo();
            carregarDespesasFolhaPagamento();
            calcularEAdicionarSaldoFinal();
            salvarTransacoesNoRepository();

            mostrarAlerta(bundle.getString("alert.success.title"),
                    String.format(bundle.getString("financialReport.alert.reloadSuccess"), (transacoes.size() - 1)),
                    Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            mostrarAlerta(bundle.getString("alert.error.title"),
                    bundle.getString("financialReport.alert.reloadError") + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void exportarRelatorioPDF() {
        IGeradorRelatorio gerador = new RelatorioFinanceiroGeral();

        if (!gerador.podeGerar(UserSession.getInstance().getUsuarioLogado())) {
            mostrarAlerta(bundle.getString("reportsAdmin.alert.accessDenied.title"),
                    bundle.getString("reportsAdmin.alert.accessDenied.header"), Alert.AlertType.WARNING);
            return;
        }

        try {
            ReportData dados = gerador.coletarDados();
            byte[] arquivoBytes = formatadorPdf.formatar(dados);
            String nomeBase = "Relatorio_Financeiro_Geral_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            salvarArquivoFisico(arquivoBytes, nomeBase, formatadorPdf);

        } catch (Exception e) {
            mostrarAlerta(bundle.getString("reportsAdmin.alert.exportError.title"),
                    bundle.getString("reportsAdmin.alert.exportError.header") + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void salvarArquivoFisico(byte[] bytes, String nomeBase, IReportFormatter formatador) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("reportsAdmin.saveDialog.title"));
        fileChooser.setInitialFileName(nomeBase + formatador.getExtensao());

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                formatador.getDescricaoFiltro(), "*" + formatador.getExtensao());
        fileChooser.getExtensionFilters().add(extFilter);

        Stage currentStage = (Stage) tabelaRelatorio.getScene().getWindow();
        File file = fileChooser.showSaveDialog(currentStage);

        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(bytes);
                mostrarAlerta(
                        bundle.getString("alert.success.title"),
                        bundle.getString("payroll.alert.exportSuccess.header"),
                         Alert.AlertType.INFORMATION
                );
            } catch (IOException e) {
                throw new IOException(bundle.getString("reportsAdmin.alert.saveError.header") + e.getMessage(), e);
            }
        } else {
        }
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}