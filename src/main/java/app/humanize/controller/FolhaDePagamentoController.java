package app.humanize.controller;

import app.humanize.model.FolhaPag;
import app.humanize.model.Funcionario;
import app.humanize.model.RegraSalarial;
import app.humanize.model.Usuario;
import app.humanize.repository.FolhaPagRepository;
import app.humanize.repository.SalarioRepository;
import app.humanize.repository.UsuarioRepository;
import app.humanize.service.formatters.CsvFormatter;
import app.humanize.service.formatters.IReportFormatter;
import app.humanize.service.formatters.PdfFormatter;
import app.humanize.service.relatorios.ReportData;
import app.humanize.util.UserSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class FolhaDePagamentoController {

    @FXML private TextField txtNome;
    @FXML private TextField txtCargo;
    @FXML private TextField txtNivel;
    @FXML private TextField txtMesAno;

    @FXML private TableView<FolhaPag> tabelaFolhaPagamento;
    @FXML private TableColumn<FolhaPag, Void> colunaAcoes;
    @FXML private TableColumn<FolhaPag, String> colunaNome;
    @FXML private TableColumn<FolhaPag, String> colunaCargo;
    @FXML private TableColumn<FolhaPag, String> colunaSalario;
    @FXML private TableColumn<FolhaPag, String> colunaDescon;
    @FXML private TableColumn<FolhaPag, String> colunaLiquid;

    @FXML private MenuButton menuAdic;
    @FXML private MenuButton menuDesc;

    @FXML private Button btnEmitir;
    @FXML private Button btnExportarPDF;
    @FXML private Button btnExportarCSV;

    private final SalarioRepository salarioRepo = SalarioRepository.getInstance();
    private final FolhaPagRepository folhaRepo = FolhaPagRepository.getInstance();
    private final UsuarioRepository usuarioRepo = UsuarioRepository.getInstance();
    private ResourceBundle bundle;

    private double adicionaisAtuais = 0.0;
    private double descontosAtuais = 0.0;

    private final IReportFormatter formatadorCsv = new CsvFormatter();
    private final IReportFormatter formatadorPdf = new PdfFormatter();

    public enum NivelExperiencia {
        JUNIOR("Júnior", 0.0), PLENO("Pleno", 100.0), SENIOR("Sênior", 250.0),
        ESPECIALISTA("Especialista", 400.0), LIDER("Líder", 600.0);
        private final String descricao; private final double adicional;
        NivelExperiencia(String descricao, double adicional) { this.descricao = descricao; this.adicional = adicional; }
        public String getDescricao() { return descricao; } public double getAdicional() { return adicional; }
        public static NivelExperiencia fromString(String texto) {
            for (NivelExperiencia nivel : values()) { if (nivel.descricao.equalsIgnoreCase(texto.trim())) { return nivel; } } return null;
        }
    }

    @FXML
    public void initialize() {
        this.bundle = UserSession.getInstance().getBundle();
        configurarTabela();
        configurarEventos();
        carregarFolhasExistentes();
        configurarExportacaoBotoes();

        txtNome.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.trim().isEmpty()) {
                buscarFuncionarioPorNome(newValue.trim());
            } else {
                txtCargo.clear();
            }
        });

        tabelaFolhaPagamento.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean selected = newVal != null;
            btnExportarPDF.setDisable(!selected);
            btnExportarCSV.setDisable(!selected);
        });
        btnExportarPDF.setDisable(true);
        btnExportarCSV.setDisable(true);
    }

    private void buscarFuncionarioPorNome(String nome) {
        List<Usuario> todosUsuarios = usuarioRepo.getTodosUsuarios();

        for (Usuario usuario : todosUsuarios) {
            if (usuario instanceof Funcionario funcionario) {
                if (funcionario.getNome().equalsIgnoreCase(nome)) {

                    txtCargo.setText(funcionario.getCargo());
                    txtCargo.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
                    return;
                }
            }
        }

        txtCargo.clear();
        txtCargo.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
    }

    private boolean funcionarioExiste(String nome) {
        List<Usuario> todosUsuarios = usuarioRepo.getTodosUsuarios();

        for (Usuario usuario : todosUsuarios) {
            if (usuario instanceof Funcionario funcionario) {
                if (funcionario.getNome().equalsIgnoreCase(nome)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void carregarFolhasExistentes() {
        List<FolhaPag> folhas = folhaRepo.carregarTodasFolhas();
        tabelaFolhaPagamento.getItems().setAll(folhas);
    }

    private void configurarTabela() {
        String currencyFormat = bundle.getString("payroll.table.currencyFormat");
        colunaNome.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNome()));
        colunaCargo.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCargo()));
        colunaSalario.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format(currencyFormat, cellData.getValue().getSalarioBase())));
        colunaDescon.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format(currencyFormat, cellData.getValue().getDescontos())));
        colunaLiquid.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format(currencyFormat, cellData.getValue().getSalarioLiquido())));

        colunaAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btnVisualizar = new Button(bundle.getString("payroll.table.button.view"));

            {
                btnVisualizar.getStyleClass().add("table-action-button");
                btnVisualizar.setOnAction(event -> {
                    FolhaPag folha = getTableView().getItems().get(getIndex());
                    mostrarDetalhesFolha(folha);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnVisualizar);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void configurarEventos() {
        btnEmitir.setOnAction(e -> emitirFolhaPagamento());

        if (!menuAdic.getItems().isEmpty()) {
            MenuItem horasExtras = menuAdic.getItems().get(0);
            horasExtras.setOnAction(e -> adicionarHorasExtras());
        }

        if (!menuDesc.getItems().isEmpty()) {
            MenuItem atraso = menuDesc.getItems().get(0);
            MenuItem multa = menuDesc.getItems().size() > 1 ? menuDesc.getItems().get(1) : null;

            if (atraso != null) atraso.setOnAction(e -> aplicarDescontoAtraso());
            if (multa != null) multa.setOnAction(e -> aplicarDescontoMulta());
        }
    }


    private void configurarExportacaoBotoes() {
        btnExportarPDF.setOnAction(e -> exportarFolhaSelecionada(formatadorPdf));
        btnExportarCSV.setOnAction(e -> exportarFolhaSelecionada(formatadorCsv));
    }

    @FXML
    private void exportarFolhaPDF() {
        exportarFolhaSelecionada(formatadorPdf);
    }

    @FXML
    private void exportarFolhaCSV() {
        exportarFolhaSelecionada(formatadorCsv);
    }

    private void exportarFolhaSelecionada(IReportFormatter formatador) {
        FolhaPag folhaSelecionada = tabelaFolhaPagamento.getSelectionModel().getSelectedItem();
        if (folhaSelecionada == null) {
            mostrarAlerta(
                    bundle.getString("payroll.alert.noSelection.title"),
                    bundle.getString("payroll.alert.noSelection.headerExport"),
                    bundle.getString("payroll.alert.invalidDate.content"));
            return;
        }

        try {
            ReportData dados = criarReportDataDaFolha(folhaSelecionada);
            byte[] arquivoBytes = formatador.formatar(dados);

            salvarArquivoFisico(arquivoBytes,
                    "Folha_" + folhaSelecionada.getNome().replace(" ", "_") + "_" + folhaSelecionada.getData().format(DateTimeFormatter.ofPattern("yyyyMM")),
                    formatador);

        } catch (Exception e) {
            mostrarErro(
                    bundle.getString("reportsAdmin.alert.exportError.title"),
                    bundle.getString("reportsAdmin.alert.exportError.header") + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    private ReportData criarReportDataDaFolha(FolhaPag folha) {
        String currencyFormat = bundle.getString("payroll.table.currencyFormat");
        String DATE_FORMAT = "dd/MM/yyyy";
        String titulo = String.format(bundle.getString("payroll.report.titleFormat"),
                folha.getNome(), folha.getData().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));

        List<String> headers = List.of(
                bundle.getString("payroll.report.header.field"),
                bundle.getString("payroll.report.header.proventos"),
                bundle.getString("payroll.report.header.deductions")
        );

        double totalProventos = folha.getSalarioBase() + folha.getAdicionalNivel() + folha.getBeneficios() + folha.getAdicionais();

        List<List<String>> rows = List.of(
                List.of(bundle.getString("payroll.report.item.name"), folha.getNome(), ""),
                List.of(bundle.getString("payroll.report.item.position"), folha.getCargo(), ""),
                List.of(bundle.getString("payroll.report.item.baseSalary"), String.format(currencyFormat, folha.getSalarioBase()), ""),
                List.of(bundle.getString("payroll.report.item.levelBonus"), String.format(currencyFormat, folha.getAdicionalNivel()), ""),
                List.of(bundle.getString("payroll.report.item.benefits"), String.format(currencyFormat, folha.getBeneficios()), ""),
                List.of(bundle.getString("payroll.report.item.additions"), String.format(currencyFormat, folha.getAdicionais()), ""),
                List.of(bundle.getString("payroll.report.item.totalGross"), String.format(currencyFormat, totalProventos), ""),
                List.of(bundle.getString("payroll.report.item.totalDeductions"), "", String.format(currencyFormat, folha.getDescontos())),
                List.of(bundle.getString("payroll.report.item.netTotal"), String.format(currencyFormat, folha.getSalarioLiquido()), "")
        );

        return new ReportData(titulo, headers, rows);
    }

    private void salvarArquivoFisico(byte[] bytes, String nomeBase, IReportFormatter formatador) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("reportsAdmin.saveDialog.title"));
        fileChooser.setInitialFileName(nomeBase + formatador.getExtensao());

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                formatador.getDescricaoFiltro(), "*" + formatador.getExtensao());
        fileChooser.getExtensionFilters().add(extFilter);

        Stage currentStage = (Stage) tabelaFolhaPagamento.getScene().getWindow();
        File file = fileChooser.showSaveDialog(currentStage);

        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(bytes);

                mostrarAlerta(
                        bundle.getString("alert.success.title"),
                        bundle.getString("payroll.alert.exportSuccess.header"),
                        file.getAbsolutePath()
                );
            }
        }
    }

    private void mostrarDetalhesFolha(FolhaPag folha) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ContrachequeIndividual.fxml"), bundle);

            Parent root = loader.load();

            ContrachequeIndividualController controller = loader.getController();

            controller.initData(folha);

            Stage stage = new Stage();
            stage.setTitle(bundle.getString("payslip.details.title"));
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Torna a janela modal
            stage.initOwner(tabelaFolhaPagamento.getScene().getWindow());
            stage.showAndWait();

        } catch (IOException e) {
            mostrarErro(
                    bundle.getString("payroll.alert.saveError.title"),
                    "Falha ao carregar a tela de detalhes do contracheque: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    @FXML
    private void emitirFolhaPagamento() {
        String nome = txtNome.getText().trim();
        String cargoNome = txtCargo.getText().trim();
        String nivelNome = txtNivel.getText().trim();
        String mesAno = txtMesAno.getText().trim();

        if (nome.isEmpty() || cargoNome.isEmpty() || nivelNome.isEmpty()) {
            mostrarAlerta(
                    bundle.getString("payroll.alert.emptyFields.title"),
                    bundle.getString("payroll.alert.emptyFields.header"),
                    bundle.getString("payroll.alert.invalidDate.content"));
            return;
        }

        if (!funcionarioExiste(nome)) {
            mostrarAlerta(
                    bundle.getString("payroll.alert.employeeNotFound.title"),
                    String.format(bundle.getString("payroll.alert.employeeNotFound.header"), nome) + "\n" +
                            bundle.getString("payroll.alert.employeeNotFound.content"),
                    bundle.getString("payroll.alert.invalidDate.content"));
            return;
        }

        NivelExperiencia nivel = NivelExperiencia.fromString(nivelNome);
        if (nivel == null) {
            String niveisValidos = String.join(", ",
                    bundle.getString("payroll.level.junior"),
                    bundle.getString("payroll.level.pleno"),
                    bundle.getString("payroll.level.senior"),
                    bundle.getString("payroll.level.especialista"),
                    bundle.getString("payroll.level.lider")
            );
            mostrarAlerta(
                    bundle.getString("payroll.alert.invalidLevel.title"),
                    String.format(bundle.getString("payroll.alert.invalidLevel.header"), nivelNome) + "\n\n" +
                            String.format(bundle.getString("payroll.alert.invalidLevel.content"), niveisValidos),
                    bundle.getString("payroll.alert.invalidDate.content"));
            return;
        }

        RegraSalarial regra = buscarRegraSalarial(cargoNome, nivel.getDescricao());
        if (regra == null) {
            String content = bundle.getString("payroll.alert.ruleNotFound.header") + "\n" +
                    String.format(bundle.getString("payroll.alert.ruleNotFound.content.position"), cargoNome) + "\n" +
                    String.format(bundle.getString("payroll.alert.ruleNotFound.content.level"), nivel.getDescricao()) + "\n\n" +
                    bundle.getString("payroll.alert.ruleNotFound.content.footer");
            mostrarAlerta(
                    bundle.getString("payroll.alert.ruleNotFound.title"),
                    content,
                    bundle.getString("payroll.alert.invalidDate.content"));
            return;
        }

        double salarioBase = regra.getSalarioBase();
        double adicionalNivel = nivel.getAdicional();
        double beneficios = regra.getBeneficios();
        double salarioTotal = salarioBase + adicionalNivel + beneficios + adicionaisAtuais - descontosAtuais;

        LocalDate dataEmissao;
        try {
            dataEmissao = LocalDate.parse(mesAno, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            mostrarAlerta(
                    bundle.getString("payroll.alert.invalidDate.title"),
                    bundle.getString("payroll.alert.invalidDate.header"),
                    bundle.getString("payroll.alert.invalidDate.content")
            );
            return;
        }


        try {
            FolhaPag folha = new FolhaPag(nome, cargoNome, nivel.getDescricao(),
                    salarioBase, adicionalNivel, beneficios, adicionaisAtuais, descontosAtuais, salarioTotal, dataEmissao);
            folhaRepo.salvarFolha(folha);

            carregarFolhasExistentes();

        } catch (IOException e) {
            mostrarErro(
                    bundle.getString("payroll.alert.saveError.title"),
                    bundle.getString("payroll.alert.saveError.header") + e.getMessage()
            );
        }

        adicionaisAtuais = 0.0;
        descontosAtuais = 0.0;
        limparCampos();
    }

    // metodos auxiliares
    private RegraSalarial buscarRegraSalarial(String cargo, String nivel) {
        List<RegraSalarial> regras = salarioRepo.carregarTodasRegras();

        for (RegraSalarial regra : regras) {
            if (regra.getCargo().equalsIgnoreCase(cargo.trim()) &&
                    regra.getNivel().equalsIgnoreCase(nivel.trim())) {
                return regra;
            }
        }
        return null;
    }

    private void adicionarHorasExtras() {
        double ADICIONAL_HORAS_EXTRAS = 100.0;
        adicionaisAtuais += ADICIONAL_HORAS_EXTRAS;
        mostrarSelecoesAtuais();
    }

    private void aplicarDescontoAtraso() {
        double DESCONTO_ATRASO = 50.0;
        descontosAtuais += DESCONTO_ATRASO;
        mostrarSelecoesAtuais();
    }

    private void aplicarDescontoMulta() {
        double DESCONTO_MULTA = 80.0;
        descontosAtuais += DESCONTO_MULTA;
        mostrarSelecoesAtuais();
    }

    private void mostrarSelecoesAtuais() {
        String currencyFormat = bundle.getString("payroll.table.currencyFormat");
        String mensagem = bundle.getString("payroll.alert.currentSelections.header") + "\n" +
                String.format(bundle.getString("payroll.alert.currentSelections.content.additions"), String.format(currencyFormat, adicionaisAtuais)) + "\n" +
                String.format(bundle.getString("payroll.alert.currentSelections.content.deductions"), String.format(currencyFormat, descontosAtuais)) + "\n\n" +
                bundle.getString("payroll.alert.currentSelections.content.footer");

        mostrarAlerta(bundle.getString("payroll.alert.currentSelections.title"), mensagem, bundle.getString("payroll.alert.invalidDate.content"));
    }

    private void limparCampos() {
        txtNome.clear();
        txtCargo.clear();
        txtNivel.clear();
        txtMesAno.clear();
        txtCargo.setStyle("");
    }

    private void mostrarAlerta(String titulo, String mensagem, String string) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}