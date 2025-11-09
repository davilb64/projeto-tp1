package app.humanize.controller;

import app.humanize.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import app.humanize.repository.SalarioRepository;
import app.humanize.repository.FolhaPagRepository;
import app.humanize.repository.UsuarioRepository;
import app.humanize.model.RegraSalarial;
import app.humanize.model.FolhaPag;
import app.humanize.model.Funcionario;
import app.humanize.model.Usuario;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import java.io.IOException;

public class FolhaDePagamentoController {

    @FXML private TextField txtNome;
    @FXML private TextField txtCargo;
    @FXML private TextField txtNivel;
    @FXML private TextField txtMesAno;

    @FXML private TableView<FolhaPag> tabelaFolhaPagamento;
    @FXML private TableColumn<FolhaPag, String> colunaNome;
    @FXML private TableColumn<FolhaPag, String> colunaCargo;
    @FXML private TableColumn<FolhaPag, String> colunaSalario;
    @FXML private TableColumn<FolhaPag, String> colunaDescon;
    @FXML private TableColumn<FolhaPag, String> colunaLiquid;

    @FXML private MenuButton menuAdic;
    @FXML private MenuButton menuDesc;

    @FXML private Button btnEmitir;

    private SalarioRepository salarioRepo = SalarioRepository.getInstance();
    private FolhaPagRepository folhaRepo = FolhaPagRepository.getInstance();
    private UsuarioRepository usuarioRepo = UsuarioRepository.getInstance();
    private ResourceBundle bundle;

    private double adicionaisAtuais = 0.0;
    private double descontosAtuais = 0.0;

    private final double ADICIONAL_HORAS_EXTRAS = 100.0;
    private final double DESCONTO_ATRASO = 50.0;
    private final double DESCONTO_MULTA = 80.0;

    public enum NivelExperiencia {
        JUNIOR("Júnior", 0.0),
        PLENO("Pleno", 100.0),
        SENIOR("Sênior", 250.0),
        ESPECIALISTA("Especialista", 400.0),
        LIDER("Líder", 600.0);

        private final String descricao;
        private final double adicional;

        NivelExperiencia(String descricao, double adicional) {
            this.descricao = descricao;
            this.adicional = adicional;
        }

        public String getDescricao() { return descricao; }
        public double getAdicional() { return adicional; }

        public static NivelExperiencia fromString(String texto) {
            for (NivelExperiencia nivel : values()) {
                if (nivel.descricao.equalsIgnoreCase(texto.trim())) {
                    return nivel;
                }
            }
            return null;
        }
    }

    @FXML
    public void initialize() {
        this.bundle = UserSession.getInstance().getBundle();
        configurarTabela();
        configurarEventos();
        carregarFolhasExistentes();

        txtNome.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.trim().isEmpty()) {
                buscarFuncionarioPorNome(newValue.trim());
            } else {
                txtCargo.clear();
            }
        });
    }

    private void buscarFuncionarioPorNome(String nome) {
        List<Usuario> todosUsuarios = usuarioRepo.getTodosUsuarios();

        for (Usuario usuario : todosUsuarios) {
            if (usuario instanceof Funcionario) {
                Funcionario funcionario = (Funcionario) usuario;
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
            if (usuario instanceof Funcionario) {
                Funcionario funcionario = (Funcionario) usuario;
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

    @FXML
    private void emitirFolhaPagamento() {
        String nome = txtNome.getText().trim();
        String cargoNome = txtCargo.getText().trim();
        String nivelNome = txtNivel.getText().trim();
        String mesAno = txtMesAno.getText().trim();

        if (nome.isEmpty() || cargoNome.isEmpty() || nivelNome.isEmpty()) {
            mostrarAlerta(
                    bundle.getString("payroll.alert.emptyFields.title"),
                    bundle.getString("payroll.alert.emptyFields.header")
            );
            return;
        }

        if (!funcionarioExiste(nome)) {
            mostrarAlerta(
                    bundle.getString("payroll.alert.employeeNotFound.title"),
                    String.format(bundle.getString("payroll.alert.employeeNotFound.header"), nome) + "\n" +
                            bundle.getString("payroll.alert.employeeNotFound.content")
            );
            return;
        }

        if (cargoNome.isEmpty()) {
            mostrarAlerta(
                    bundle.getString("payroll.alert.positionUndefined.title"),
                    String.format(bundle.getString("payroll.alert.positionUndefined.header"), nome) + "\n" +
                            bundle.getString("payroll.alert.positionUndefined.content")
            );
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
                            String.format(bundle.getString("payroll.alert.invalidLevel.content"), niveisValidos)
            );
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
                    content
            );
            return;
        }

        double salarioBase = regra.getSalarioBase();
        double adicionalNivel = nivel.getAdicional();
        double beneficios = regra.getBeneficios();
        double salarioTotal = salarioBase + adicionalNivel + beneficios + adicionaisAtuais - descontosAtuais;

        try {
            FolhaPag folha = new FolhaPag(nome, cargoNome, nivel.getDescricao(),
                    salarioBase, adicionalNivel, beneficios, adicionaisAtuais, descontosAtuais, salarioTotal);
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
        adicionaisAtuais += ADICIONAL_HORAS_EXTRAS;
        mostrarSelecoesAtuais();
    }

    private void aplicarDescontoAtraso() {
        descontosAtuais += DESCONTO_ATRASO;
        mostrarSelecoesAtuais();
    }

    private void aplicarDescontoMulta() {
        descontosAtuais += DESCONTO_MULTA;
        mostrarSelecoesAtuais();
    }

    private void mostrarSelecoesAtuais() {
        String currencyFormat = bundle.getString("payroll.table.currencyFormat");
        String mensagem = bundle.getString("payroll.alert.currentSelections.header") + "\n" +
                String.format(bundle.getString("payroll.alert.currentSelections.content.additions"), String.format(currencyFormat, adicionaisAtuais)) + "\n" +
                String.format(bundle.getString("payroll.alert.currentSelections.content.deductions"), String.format(currencyFormat, descontosAtuais)) + "\n\n" +
                bundle.getString("payroll.alert.currentSelections.content.footer");

        mostrarAlerta(bundle.getString("payroll.alert.currentSelections.title"), mensagem);
    }

    private void limparCampos() {
        txtNome.clear();
        txtCargo.clear();
        txtNivel.clear();
        txtMesAno.clear();
        txtCargo.setStyle("");
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING); // Alterado para WARNING
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