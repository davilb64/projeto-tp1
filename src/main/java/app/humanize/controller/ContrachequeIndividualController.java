package app.humanize.controller;

import app.humanize.model.FolhaPag;
import app.humanize.model.Funcionario;
import app.humanize.model.Usuario;
// --- NOVOS IMPORTS ---
import app.humanize.service.formatters.IReportFormatter;
import app.humanize.service.formatters.PdfFormatter;
import app.humanize.service.relatorios.ReportData;
// --- FIM DOS NOVOS IMPORTS ---
import app.humanize.util.UserSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser; // NOVO
import javafx.stage.Stage;

import java.io.File; // NOVO
import java.io.FileOutputStream; // NOVO
import java.io.IOException; // NOVO
import java.time.format.DateTimeFormatter;
import java.util.List; // NOVO
import java.util.ResourceBundle;

/**
 * Controlador para a tela de visualização de um contracheque individual.
 * Esta tela é aberta de forma modal pelo FolhaDePagamentoController.
 */
public class ContrachequeIndividualController {

    // --- Informações do Funcionário ---
    @FXML private Label lblNomeFuncionario;
    @FXML private Label lblCargo;
    @FXML private Label lblDepartamento;
    @FXML private Label lblMatricula;
    @FXML private Label lblPeriodoReferencia;

    // --- Tabela de Detalhes (Proventos/Descontos) ---
    @FXML private TableView<ItemContracheque> tblDetalhes;
    @FXML private TableColumn<ItemContracheque, String> colDescricao;
    @FXML private TableColumn<ItemContracheque, String> colProvento;
    @FXML private TableColumn<ItemContracheque, String> colDesconto;

    // --- Totais ---
    @FXML private Label lblTotalProventos;
    @FXML private Label lblTotalDescontos;
    @FXML private Label lblValorLiquido;

    // --- Botões de Ação ---
    @FXML private Button btnImprimir;
    @FXML private Button btnFechar;

    private ResourceBundle bundle;
    private String currencyFormat;
    private FolhaPag folhaAtual;

    // NOVO: Instância do Formatador de PDF
    private final IReportFormatter formatadorPdf = new PdfFormatter();
    private final String DATE_FORMAT_EXPORT = "dd/MM/yyyy"; // Formato para o título do PDF

    @FXML
    public void initialize() {
        bundle = UserSession.getInstance().getBundle();
        this.currencyFormat = bundle.getString("financialReport.currencyFormat");

        // Configurar Colunas da Tabela
        colDescricao.setCellValueFactory(cellData -> cellData.getValue().descricaoProperty());
        colProvento.setCellValueFactory(cellData -> cellData.getValue().proventoProperty());
        colDesconto.setCellValueFactory(cellData -> cellData.getValue().descontoProperty());

        // Ações dos botões
        btnFechar.setOnAction(e -> fecharJanela());
        btnImprimir.setOnAction(e -> imprimirRelatorio()); // Agora chama a lógica real
    }

    /**
     * Método público para injetar os dados do contracheque selecionado.
     * Deve ser chamado pelo controlador que abre esta janela.
     */
    public void initData(FolhaPag folha) {
        this.folhaAtual = folha;

        Usuario usuario = UserSession.getInstance().getUsuarioLogado();

        if (usuario == null || !(usuario instanceof Funcionario)) {
            lblNomeFuncionario.setText(folha.getNome());
            return;
        }

        Funcionario funcionario = (Funcionario) usuario;

        lblNomeFuncionario.setText(folha.getNome());
        lblCargo.setText(folha.getCargo() + " (" + folha.getNivel() + ")");
        lblDepartamento.setText(funcionario.getDepartamento());
        lblMatricula.setText(String.valueOf(funcionario.getMatricula()));

        // Formata a data (de yyyy-MM-dd) para dd/MM/yyyy para exibição
        lblPeriodoReferencia.setText(folha.getData().format(DateTimeFormatter.ofPattern(DATE_FORMAT_EXPORT)));

        preencherTabelaDetalhes(folha);

        double totalProventos = folha.getSalarioBase() + folha.getAdicionalNivel()
                + folha.getBeneficios() + folha.getAdicionais();

        lblTotalProventos.setText(formatCurrency(totalProventos));
        lblTotalDescontos.setText(formatCurrency(folha.getDescontos()));
        lblValorLiquido.setText(formatCurrency(folha.getSalarioLiquido()));
    }

    private void preencherTabelaDetalhes(FolhaPag folha) {
        ObservableList<ItemContracheque> items = FXCollections.observableArrayList();

        // --- Proventos (Ganhos) ---
        items.add(new ItemContracheque(bundle.getString("payslip.detail.baseSalary"), formatCurrency(folha.getSalarioBase()), ""));
        items.add(new ItemContracheque(bundle.getString("payslip.detail.levelBonus"), formatCurrency(folha.getAdicionalNivel()), ""));
        items.add(new ItemContracheque(bundle.getString("payslip.detail.benefits"), formatCurrency(folha.getBeneficios()), ""));
        items.add(new ItemContracheque(bundle.getString("payslip.detail.additions"), formatCurrency(folha.getAdicionais()), ""));
        items.add(new ItemContracheque(bundle.getString("payslip.detail.deductions"), "", formatCurrency(folha.getDescontos())));

        tblDetalhes.setItems(items);
    }

    // -----------------------------------------------------------------
    // --- MÉTODO ATUALIZADO ---
    // -----------------------------------------------------------------
    private void imprimirRelatorio() {
        if (this.folhaAtual == null) {
            mostrarAlerta(
                    bundle.getString("alert.error.title"),
                    bundle.getString("report.error.noPayslipFound"), // Reutilizando chave
                    null,
                    Alert.AlertType.ERROR
            );
            return;
        }

        try {
            // 1. Cria o DTO de dados (mesma lógica da tela anterior)
            ReportData dados = criarReportDataDaFolha(this.folhaAtual);

            // 2. Formata para PDF
            byte[] arquivoBytes = formatadorPdf.formatar(dados);

            // 3. Define o nome do arquivo
            String nomeBase = "Contracheque_" +
                    this.folhaAtual.getNome().replace(" ", "_") + "_" +
                    this.folhaAtual.getData().format(DateTimeFormatter.ofPattern("yyyyMM"));

            // 4. Salva o arquivo
            salvarArquivoFisico(arquivoBytes, nomeBase, formatadorPdf);

        } catch (Exception e) {
            mostrarAlerta(
                    bundle.getString("reportsAdmin.alert.exportError.title"),
                    bundle.getString("reportsAdmin.alert.exportError.header"),
                    e.getMessage(),
                    Alert.AlertType.ERROR
            );
            e.printStackTrace();
        }
    }

    // -----------------------------------------------------------------
    // --- NOVOS MÉTODOS (Copiados do FolhaDePagamentoController) ---
    // -----------------------------------------------------------------

    /**
     * Cria um DTO ReportData a partir de uma FolhaPag para uso do formatador.
     */
    private ReportData criarReportDataDaFolha(FolhaPag folha) {
        String titulo = String.format(bundle.getString("payroll.report.titleFormat"),
                folha.getNome(), folha.getData().format(DateTimeFormatter.ofPattern(DATE_FORMAT_EXPORT)));

        List<String> headers = List.of(
                bundle.getString("payroll.report.header.field"),
                bundle.getString("payroll.report.header.proventos"),
                bundle.getString("payroll.report.header.deductions")
        );

        double totalProventos = folha.getSalarioBase() + folha.getAdicionalNivel() + folha.getBeneficios() + folha.getAdicionais();

        List<List<String>> rows = List.of(
                List.of(bundle.getString("payroll.report.item.name"), folha.getNome(), ""),
                List.of(bundle.getString("payroll.report.item.position"), folha.getCargo(), ""),
                List.of(bundle.getString("payroll.report.item.baseSalary"), formatCurrency(folha.getSalarioBase()), ""),
                List.of(bundle.getString("payroll.report.item.levelBonus"), formatCurrency(folha.getAdicionalNivel()), ""),
                List.of(bundle.getString("payroll.report.item.benefits"), formatCurrency(folha.getBeneficios()), ""),
                List.of(bundle.getString("payroll.report.item.additions"), formatCurrency(folha.getAdicionais()), ""),
                List.of(bundle.getString("payroll.report.item.totalGross"), formatCurrency(totalProventos), ""),
                List.of(bundle.getString("payroll.report.item.totalDeductions"), "", formatCurrency(folha.getDescontos())),
                List.of(bundle.getString("payroll.report.item.netTotal"), formatCurrency(folha.getSalarioLiquido()), "")
        );

        return new ReportData(titulo, headers, rows);
    }

    /**
     * Salva os bytes do relatório em um arquivo físico, usando FileChooser.
     */
    private File salvarArquivoFisico(byte[] bytes, String nomeBase, IReportFormatter formatador) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("reportsAdmin.saveDialog.title"));
        fileChooser.setInitialFileName(nomeBase + formatador.getExtensao());

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                formatador.getDescricaoFiltro(), "*" + formatador.getExtensao());
        fileChooser.getExtensionFilters().add(extFilter);

        Stage currentStage = (Stage) btnFechar.getScene().getWindow(); // Pega o Stage atual
        File file = fileChooser.showSaveDialog(currentStage);

        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(bytes);

                mostrarAlerta(
                        bundle.getString("alert.success.title"),
                        bundle.getString("payroll.alert.exportSuccess.header"),
                        file.getAbsolutePath(),
                        Alert.AlertType.INFORMATION
                );
                return file;
            } catch (IOException e) {
                // Relança a exceção para ser tratada no método 'imprimirRelatorio'
                throw new IOException(bundle.getString("reportsAdmin.alert.saveError.header") + e.getMessage(), e);
            }
        } else {
            return null; // Usuário cancelou
        }
    }


    // -----------------------------------------------------------------
    // --- Métodos Existentes ---
    // -----------------------------------------------------------------

    private void fecharJanela() {
        Stage stage = (Stage) btnFechar.getScene().getWindow();
        stage.close();
    }

    private String formatCurrency(double value) {
        if (value == 0.0) {
            return "";
        }
        return String.format(this.currencyFormat, value);
    }

    public static class ItemContracheque {
        private final SimpleStringProperty descricao;
        private final SimpleStringProperty provento;
        private final SimpleStringProperty desconto;

        public ItemContracheque(String descricao, String provento, String desconto) {
            this.descricao = new SimpleStringProperty(descricao);
            this.provento = new SimpleStringProperty(provento);
            this.desconto = new SimpleStringProperty(desconto);
        }

        public SimpleStringProperty descricaoProperty() { return descricao; }
        public SimpleStringProperty proventoProperty() { return provento; }
        public SimpleStringProperty descontoProperty() { return desconto; }
    }

    private void mostrarAlerta(String titulo, String cabecalho, String conteudo, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(conteudo);
        alert.showAndWait();
    }
}