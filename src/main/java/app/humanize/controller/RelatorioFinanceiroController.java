package app.humanize.controller;

import app.humanize.model.factories.RelatorioFinanceiro;
import app.humanize.model.FolhaPag;
import app.humanize.repository.FolhaPagRepository;
import app.humanize.repository.RelatorioFinanceiroRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.util.HashSet;
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
    @FXML private TableColumn<RelatorioFinanceiro, String> colValor;
    @FXML private TableColumn<RelatorioFinanceiro, String> colTipo;
    @FXML private TableColumn<RelatorioFinanceiro, String> colData1;
    @FXML private TableColumn<RelatorioFinanceiro, String> colData3;
    @FXML private TableColumn<RelatorioFinanceiro, String> colSaldoFinal;

    private ObservableList<RelatorioFinanceiro> transacoes = FXCollections.observableArrayList();
    private FolhaPagRepository folhaRepository = FolhaPagRepository.getInstance();
    private RelatorioFinanceiroRepository relatorioRepository = RelatorioFinanceiroRepository.getInstance();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        ToggleGroup grupoTipo = new ToggleGroup();
        radioReceita.setToggleGroup(grupoTipo);
        radioDespesa.setToggleGroup(grupoTipo);

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
            mostrarAlerta("Erro", "Erro ao inicializar relatório: " + e.getMessage());
        }
    }

    private void configurarColunas() {
        colData.setCellValueFactory(cellData -> cellData.getValue().dataProperty());
        colDescricao.setCellValueFactory(cellData -> cellData.getValue().descricaoProperty());
        colValor.setCellValueFactory(cellData -> cellData.getValue().receitaProperty());
        colTipo.setCellValueFactory(cellData -> cellData.getValue().despesasProperty());
        colData1.setCellValueFactory(cellData -> cellData.getValue().valorProperty());
        colData3.setCellValueFactory(cellData -> cellData.getValue().categoriaProperty());
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
            String descricaoFolha = "Folha de Pagamento - " + folha.getNome();

            if (!descricoesExistentes.contains(descricaoFolha)) {
                String dataFolha = folha.getData() != null ?
                        folha.getData().format(dateFormatter) :
                        LocalDate.now().format(dateFormatter);

                RelatorioFinanceiro folhaTransacao = new RelatorioFinanceiro(
                        dataFolha,
                        descricaoFolha,
                        "",
                        String.format("R$ %.2f", folha.getSalarioLiquido()),
                        String.format("R$ %.2f", folha.getSalarioLiquido()),
                        "",
                        "Folha de Pagamento"
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
            String tipo = radioReceita.isSelected() ? "Receita" : "Despesa";

            String receita = tipo.equals("Receita") ? String.format("R$ %s", valor) : "";
            String despesas = tipo.equals("Despesa") ? String.format("R$ %s", valor) : "";

            RelatorioFinanceiro transacao = new RelatorioFinanceiro(
                    data, descricao, receita, despesas,
                    String.format("R$ %s", valor), "", tipo
            );

            transacoes.add(transacao);
            calcularEAdicionarSaldoFinal();
            salvarTransacoesNoRepository();
            limparCampos();

            mostrarAlerta("Sucesso", "Transação salva com sucesso!");
        }
    }

    private void calcularEAdicionarSaldoFinal() {
        double saldoFinal = 0.0;

        transacoes.removeIf(t -> "SALDO FINAL".equals(t.getDescricao()));

        for (RelatorioFinanceiro transacao : transacoes) {
            double receita = extrairValorNumerico(transacao.getReceita());
            double despesa = extrairValorNumerico(transacao.getDespesas());
            saldoFinal += (receita - despesa);
        }

        RelatorioFinanceiro saldoFinalTransacao = new RelatorioFinanceiro(
                "",
                "SALDO FINAL",
                "",
                "",
                String.format("R$ %.2f", saldoFinal),
                String.format("R$ %.2f", saldoFinal),
                saldoFinal >= 0 ? "Lucro" : "Prejuízo"
        );
        transacoes.add(saldoFinalTransacao);
    }

    private double extrairValorNumerico(String valorComRS) {
        if (valorComRS == null || valorComRS.trim().isEmpty()) {
            return 0.0;
        }
        try {
            String valorLimpo = valorComRS.replace("R$", "").replace(",", ".").trim();
            return Double.parseDouble(valorLimpo);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private boolean validarCampos() {
        if (txtValor.getText().isEmpty()) {
            mostrarAlerta("Erro", "Valor é obrigatório");
            return false;
        }
        if (txtDescricao.getText().isEmpty()) {
            mostrarAlerta("Erro", "Descrição é obrigatória");
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
            relatorioRepository.salvarTransacoes(transacoes);
        } catch (IOException e) {
            mostrarAlerta("Erro", "Erro ao salvar transações: " + e.getMessage());
        }
    }

    private void carregarRelatorioCompleto() {
        try {
            transacoes.clear();
            carregarRelatorioSalvo();
            carregarDespesasFolhaPagamento();
            calcularEAdicionarSaldoFinal();
            salvarTransacoesNoRepository();

            mostrarAlerta("Sucesso", "Relatório recarregado e salvo com sucesso!\n" +
                    "Total de " + (transacoes.size() - 1) + " transações.");

        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao carregar relatório: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}