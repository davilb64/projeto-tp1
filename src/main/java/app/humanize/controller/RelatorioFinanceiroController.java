package app.humanize.controller;

import app.humanize.model.RelatorioFinanceiro;
import app.humanize.model.FolhaPag;
import app.humanize.repository.FolhaPagRepository;
import app.humanize.repository.RelatorioFinanceiroRepository;
import app.humanize.util.UserSession;
import javafx.fxml.FXML;
import java.util.List;
import javafx.scene.control.*;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class RelatorioFinanceiroController {

    @FXML private DatePicker dateTransacao;
    @FXML private TextField txtValor;
    @FXML private TextField txtDescricao;

    @FXML private RadioButton radioReceita;
    @FXML private RadioButton radioDespesa;

    @FXML private Button btnSalvar;
    @FXML private Button btnSalvarRelatorio;

    @FXML private TableView<RelatorioFinanceiro> tabelaRelatorio;
    @FXML private TableColumn<RelatorioFinanceiro, String> colData;
    @FXML private TableColumn<RelatorioFinanceiro, String> colDescricao;
    @FXML private TableColumn<RelatorioFinanceiro, String> colreceita;
    @FXML private TableColumn<RelatorioFinanceiro, String> coldespesa;
    @FXML private TableColumn<RelatorioFinanceiro, String> colData3; // Categoria
    @FXML private TableColumn<RelatorioFinanceiro, String> colSaldoFinal;

    private ObservableList<RelatorioFinanceiro> transacoes = FXCollections.observableArrayList();
    private FolhaPagRepository folhaRepository = FolhaPagRepository.getInstance();
    private RelatorioFinanceiroRepository relatorioRepository = RelatorioFinanceiroRepository.getInstance();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private ResourceBundle bundle;

    @FXML
    public void initialize() {
        this.bundle = UserSession.getInstance().getBundle();

        ToggleGroup grupoTipo = new ToggleGroup();
        radioReceita.setToggleGroup(grupoTipo);
        radioDespesa.setToggleGroup(grupoTipo);

        // Atualiza os textos dos RadioButtons
        radioReceita.setText(bundle.getString("financialReport.type.revenue"));
        radioDespesa.setText(bundle.getString("financialReport.type.expense"));

        tabelaRelatorio.setItems(transacoes);
        configurarColunas();

        btnSalvar.setOnAction(event -> salvarTransacao());
        btnSalvarRelatorio.setOnAction(event -> carregarRelatorioCompleto());

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
        colData3.setCellValueFactory(cellData -> cellData.getValue().categoriaProperty()); // Categoria
        colSaldoFinal.setCellValueFactory(cellData -> cellData.getValue().saldoProperty());
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
                        String.format(bundle.getString("financialReport.currencyFormat"), folha.getSalarioLiquido()),
                        "",
                        bundle.getString("financialReport.payroll.category")
                );
                transacoes.add(folhaTransacao);
            }
        }
    }

    private void salvarTransacao() {
        if (validarCampos()) {
            String data = dateTransacao.getValue() != null ?
                    dateTransacao.getValue().format(dateFormatter) :
                    LocalDate.now().format(dateFormatter);

            String valor = txtValor.getText();
            String descricao = txtDescricao.getText();
            String tipo = radioReceita.isSelected() ?
                    bundle.getString("financialReport.type.revenue") :
                    bundle.getString("financialReport.type.expense");

            String currencyFormat = bundle.getString("financialReport.currencyFormat");
            String receita = radioReceita.isSelected() ? String.format(currencyFormat, Double.parseDouble(valor)) : "";
            String despesas = radioDespesa.isSelected() ? String.format(currencyFormat, Double.parseDouble(valor)) : "";

            RelatorioFinanceiro transacao = new RelatorioFinanceiro(
                    data,
                    descricao,
                    receita,
                    despesas,
                    "", // saldo será calculado depois
                    tipo
            );

            transacoes.add(transacao);
            calcularEAdicionarSaldoFinal();
            salvarTransacoesNoRepository();
            limparCampos();

            mostrarAlerta(bundle.getString("alert.success.title"),
                    bundle.getString("financialReport.alert.saveSuccess"), Alert.AlertType.INFORMATION);
        }
    }

    private void calcularEAdicionarSaldoFinal() {
        double saldoAcumulado = 0.0;
        String currencyFormat = bundle.getString("financialReport.currencyFormat");
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
                "",
                saldoFinalDesc,
                "",
                "",
                String.format(currencyFormat, saldoAcumulado),
                categoriaSaldo
        );
        transacoes.add(saldoFinalTransacao);
    }

    private double extrairValorNumerico(String valorComRS) {
        if (valorComRS == null || valorComRS.trim().isEmpty()) {
            return 0.0;
        }
        try {
            String currencySymbol = bundle.getString("financialReport.currencySymbol");
            String valorLimpo = valorComRS.replace(currencySymbol, "").replace(",", ".").trim();
            return Double.parseDouble(valorLimpo);
        } catch (NumberFormatException e) {
            return 0.0;
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

    private void limparCampos() {
        txtValor.clear();
        txtDescricao.clear();
        dateTransacao.setValue(null);
        radioReceita.setSelected(true);
    }

    private void salvarTransacoesNoRepository() {
        try {
            // Salva todas as transações, exceto a linha "SALDO FINAL"
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

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}