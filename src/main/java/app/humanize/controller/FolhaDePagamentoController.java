package app.humanize.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import app.humanize.repository.SalarioRepository;
import app.humanize.repository.FolhaPagRepository;
import app.humanize.repository.VagaRepository;
import app.humanize.model.RegraSalarial;
import app.humanize.model.FolhaPag;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import java.io.IOException;

public class FolhaDePagamentoController {

    @FXML private TextField txtNome;
    @FXML private TextField txtCargo;
    @FXML private TextField txtNivel;

    @FXML private TableView<Map<String, Object>> tabelaFolhaPagamento;
    @FXML private TableColumn<Map<String, Object>, String> colunaNome;
    @FXML private TableColumn<Map<String, Object>, String> colunaCargo;
    @FXML private TableColumn<Map<String, Object>, String> colunaSalario;
    @FXML private TableColumn<Map<String, Object>, String> colunaDescon;
    @FXML private TableColumn<Map<String, Object>, String> colunaLiquid;

    @FXML private MenuButton menuAdic;
    @FXML private MenuButton menuDesc;

    @FXML private TableView<Double> tabelinhaSalario;

    @FXML private Button btnEmitir;

    private SalarioRepository salarioRepo = SalarioRepository.getInstance();
    private FolhaPagRepository folhaRepo = FolhaPagRepository.getInstance();
    private VagaRepository vagaRepo = VagaRepository.getInstance();
    private ObservableList<Map<String, Object>> funcionarios;

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
        funcionarios = FXCollections.observableArrayList();
        configurarTabela();
        configurarEventos();
    }

    private void configurarTabela() {
        colunaNome.setCellValueFactory(cellData ->
                new SimpleStringProperty((String) cellData.getValue().get("nome")));
        colunaCargo.setCellValueFactory(cellData ->
                new SimpleStringProperty((String) cellData.getValue().get("cargo")));
        colunaSalario.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("R$ %.2f", cellData.getValue().get("salarioBase"))));
        colunaDescon.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("R$ %.2f", cellData.getValue().get("descontos"))));
        colunaLiquid.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("R$ %.2f", cellData.getValue().get("salarioLiquido"))));

        tabelaFolhaPagamento.setItems(funcionarios);
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
        String nome = txtNome.getText();
        String cargoNome = txtCargo.getText();
        String nivelNome = txtNivel.getText();

        if (nome.isEmpty() || cargoNome.isEmpty() || nivelNome.isEmpty()) {
            mostrarAlerta("Campos Vazios", "Preencha todos os campos: Nome, Cargo e Nível!");
            return;
        }

        if (!validarCargo(cargoNome)) {
            mostrarAlerta("Cargo Inválido",
                    "O cargo '" + cargoNome + "' não existe!\n\nCargos disponíveis: " + listarCargosDisponiveis());
            return;
        }

        NivelExperiencia nivel = NivelExperiencia.fromString(nivelNome);
        if (nivel == null) {
            mostrarAlerta("Nível Inválido",
                    "Nível '" + nivelNome + "' não reconhecido.\n\n" +
                            "Níveis válidos: Júnior, Pleno, Sênior, Especialista, Líder");
            return;
        }

        RegraSalarial regra = buscarRegraSalarial(cargoNome, nivel.getDescricao());
        if (regra == null) {
            mostrarAlerta("Regra Salarial Não Encontrada",
                    "Não foi encontrada uma regra salarial para:\n" +
                            "Cargo: " + cargoNome + "\n" +
                            "Nível: " + nivel.getDescricao() + "\n\n" +
                            "Cadastre primeiro a regra salarial no sistema de Regras Salariais.");
            return;
        }


        double salarioBase = regra.getSalarioBase();
        double adicionalNivel = nivel.getAdicional();
        double beneficios = regra.getBeneficios();
        double salarioTotal = salarioBase + adicionalNivel + beneficios + adicionaisAtuais - descontosAtuais;

        Map<String, Object> funcionario = new HashMap<>();
        funcionario.put("nome", nome);
        funcionario.put("cargo", cargoNome);
        funcionario.put("nivel", nivel.getDescricao());
        funcionario.put("salarioBase", salarioBase);
        funcionario.put("adicionalNivel", adicionalNivel);
        funcionario.put("beneficios", beneficios);
        funcionario.put("adicionais", adicionaisAtuais);
        funcionario.put("descontos", descontosAtuais);
        funcionario.put("salarioLiquido", salarioTotal);

        funcionarios.add(funcionario);

        try {

            FolhaPag folha = new FolhaPag(nome, cargoNome, nivel.getDescricao(),
                    salarioBase, adicionalNivel, beneficios, adicionaisAtuais, descontosAtuais, salarioTotal);
            folhaRepo.salvarFolha(folha);
        } catch (IOException e) {
            mostrarAlerta("Erro", "Erro ao salvar folha de pagamento: " + e.getMessage());
        }

        atualizarTabelaSalario(salarioTotal);

        mostrarAlerta("Sucesso", "Folha de pagamento emitida com sucesso para " + nome +
                "\nAdicionais: R$ " + String.format("%.2f", adicionaisAtuais) +
                "\nDescontos: R$ " + String.format("%.2f", descontosAtuais) +
                "\nSalário Líquido: R$ " + String.format("%.2f", salarioTotal));


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
        String mensagem = "Seleções atuais:\n" +
                "Adicionais: R$ " + String.format("%.2f", adicionaisAtuais) + "\n" +
                "Descontos: R$ " + String.format("%.2f", descontosAtuais) + "\n\n" +
                "Clique em EMITIR para finalizar.";

        mostrarAlerta("Seleções", mensagem);
    }

    private boolean validarCargo(String cargoNome) {
        return vagaRepo.getTodosCargos().stream()
                .anyMatch(cargo -> cargo.equalsIgnoreCase(cargoNome.trim()));
    }

    private String listarCargosDisponiveis() {
        StringBuilder cargos = new StringBuilder();
        for (String cargo : vagaRepo.getTodosCargos()) {
            cargos.append("\n- ").append(cargo);
        }
        return cargos.toString();
    }

    private void atualizarTabelaSalario(double salario) {
        ObservableList<Double> salarios = FXCollections.observableArrayList(salario);
        tabelinhaSalario.setItems(salarios);
    }

    private void limparCampos() {
        txtNome.clear();
        txtCargo.clear();
        txtNivel.clear();
        tabelinhaSalario.getItems().clear();
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}