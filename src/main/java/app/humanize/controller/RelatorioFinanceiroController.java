package app.humanize.controller;

import app.humanize.model.factories.RelatorioFinanceiro;
import app.humanize.repository.FolhaPagRepository;
import app.humanize.repository.RelatorioFinanceiroRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.IOException;

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
    @FXML private TableColumn<RelatorioFinanceiro, String> colData2;
    @FXML private TableColumn<RelatorioFinanceiro, String> colData3;

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
        btnSalvarRelatorio.setOnAction(event -> salvarRelatorioCompleto());

        carregarRelatorioSalvo();
        carregarDespesasFolhaPagamento();
    }

    private void configurarColunas() {
        colData.setCellValueFactory(cellData -> cellData.getValue().dataProperty());
        colDescricao.setCellValueFactory(cellData -> cellData.getValue().descricaoProperty());
        colValor.setCellValueFactory(cellData -> cellData.getValue().receitaProperty());
        colTipo.setCellValueFactory(cellData -> cellData.getValue().despesasProperty());
        colData1.setCellValueFactory(cellData -> cellData.getValue().valorProperty());
        colData2.setCellValueFactory(cellData -> cellData.getValue().saldoProperty());
        colData3.setCellValueFactory(cellData -> cellData.getValue().categoriaProperty());
    }

    private void carregarRelatorioSalvo() {
        var transacoesSalvas = relatorioRepository.carregarTransacoes();
        transacoes.addAll(transacoesSalvas);
    }

    private void carregarDespesasFolhaPagamento() {
        var folhas = folhaRepository.carregarTodasFolhas();
        double totalFolha = folhas.stream().mapToDouble(f -> f.getSalarioLiquido()).sum();

        if (totalFolha > 0) {
            RelatorioFinanceiro folhaTransacao = new RelatorioFinanceiro(
                    LocalDate.now().format(dateFormatter),
                    "Folha de Pagamento",
                    "",
                    String.format("R$ %.2f", totalFolha),
                    String.format("R$ %.2f", totalFolha),
                    "",
                    "Folha de Pagamento"
            );
            transacoes.add(folhaTransacao);
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
            salvarTransacoesNoRepository();
            limparCampos();
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

    private void salvarRelatorioCompleto() {
        if (transacoes.isEmpty()) {
            mostrarAlerta("Aviso", "Não há dados para salvar");
            return;
        }

        try {
            relatorioRepository.salvarTransacoes(transacoes);
            mostrarAlerta("Sucesso", "Relatório salvo com sucesso!");
        } catch (IOException e) {
            mostrarAlerta("Erro", "Erro ao salvar relatório: " + e.getMessage());
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