package app.humanize.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import app.humanize.repository.SalarioRepository;
import app.humanize.repository.VagaRepository;
import java.util.Map;
import java.util.HashMap;
import javafx.beans.property.SimpleStringProperty;

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
    private VagaRepository vagaRepo = VagaRepository.getInstance();
    private ObservableList<Map<String, Object>> funcionarios;


    private final double ADICIONAL_HORAS_EXTRAS = 100.0;
    private final double DESCONTO_ATRASO = 50.0;
    private final double DESCONTO_MULTA = 80.0;

    private final Map<String, Double> adicionaisPorNivel = Map.of(
            "Júnior", 0.0,
            "Pleno", 100.0,
            "Sênior", 250.0,
            "Especialista", 400.0,
            "Líder", 600.0
    );

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

        MenuItem horasExtras = menuAdic.getItems().get(0);
        horasExtras.setOnAction(e -> adicionarHorasExtras());

        MenuItem atraso = menuDesc.getItems().get(0);
        MenuItem multa = menuDesc.getItems().get(1);
        atraso.setOnAction(e -> aplicarDescontoAtraso());
        multa.setOnAction(e -> aplicarDescontoMulta());
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

        if (!validarNivel(nivelNome)) {
            mostrarAlerta("Nível Inválido",
                    "Nível '" + nivelNome + "' não reconhecido.\n\n" +
                            "Níveis válidos: Júnior, Pleno, Sênior, Especialista, Líder");
            return;
        }

        Double salarioBase = buscarSalarioBaseDoRepository(cargoNome, nivelNome);
        if (salarioBase == null) {
            mostrarAlerta("Regra Salarial Não Encontrada",
                    "Não foi encontrada uma regra salarial para:\n" +
                            "Cargo: " + cargoNome + "\n" +
                            "Nível: " + nivelNome + "\n\n" +
                            "Cadastre primeiro a regra salarial no sistema de Regras Salariais.");
            return;
        }

        double adicionalNivel = adicionaisPorNivel.get(nivelNome);
        double salarioTotal = salarioBase + adicionalNivel;


               // temporario enquanto nao tenho o funcioanrio
        Map<String, Object> funcionario = new HashMap<>();
        funcionario.put("nome", nome);
        funcionario.put("cargo", cargoNome);
        funcionario.put("nivel", nivelNome);
        funcionario.put("salarioBase", salarioBase);
        funcionario.put("adicionalNivel", adicionalNivel);
        funcionario.put("adicionais", 0.0);
        funcionario.put("descontos", 0.0);
        funcionario.put("salarioLiquido", salarioTotal);

        funcionarios.add(funcionario);

        atualizarTabelaSalario(salarioTotal);
        limparCampos();
    }

    private boolean validarCargo(String cargoNome) {
        return vagaRepo.getTodosCargos().stream()
                .anyMatch(cargo -> cargo.equalsIgnoreCase(cargoNome.trim()));
    }

    private boolean validarNivel(String nivelNome) {
        return adicionaisPorNivel.containsKey(nivelNome);
    }

    private Double buscarSalarioBaseDoRepository(String cargo, String nivel) {
        try {
            String chave = cargo + "_" + nivel;
            String regraSalarial = ""; ///salarioRepo.buscarRegra(chave);

            if (regraSalarial != null && !regraSalarial.isEmpty()) {

                String[] partes = regraSalarial.split("Base: R\\$");
                if (partes.length > 1) {
                    String valorParte = partes[1].split("\\|")[0].trim();
                    return Double.parseDouble(valorParte.replace(",", "."));
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Erro ao buscar salário base: " + e.getMessage());
            return null;
        }
    }

    private String listarCargosDisponiveis() {
        StringBuilder cargos = new StringBuilder();
        for (String cargo : vagaRepo.getTodosCargos()) {
            cargos.append("\n- ").append(cargo);
        }
        return cargos.toString();
    }

    private void adicionarHorasExtras() {
        Map<String, Object> selecionado = tabelaFolhaPagamento.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            double adicionaisAtuais = (double) selecionado.get("adicionais");
            double salarioLiquidoAtual = (double) selecionado.get("salarioLiquido");


            selecionado.put("adicionais", adicionaisAtuais + ADICIONAL_HORAS_EXTRAS);
            selecionado.put("salarioLiquido", salarioLiquidoAtual + ADICIONAL_HORAS_EXTRAS);

            atualizarTabela();
        } else {
            mostrarAlerta("Seleção", "Selecione um funcionário na tabela!");
        }
    }

    private void aplicarDescontoAtraso() {
        aplicarDesconto(DESCONTO_ATRASO);
    }

    private void aplicarDescontoMulta() {
        aplicarDesconto(DESCONTO_MULTA);
    }

    private void aplicarDesconto(double valor) {
        Map<String, Object> selecionado = tabelaFolhaPagamento.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            double descontosAtuais = (double) selecionado.get("descontos");
            double salarioLiquidoAtual = (double) selecionado.get("salarioLiquido");

            selecionado.put("descontos", descontosAtuais + valor);
            selecionado.put("salarioLiquido", salarioLiquidoAtual - valor);

            atualizarTabela();
        } else {
            mostrarAlerta("Seleção", "Selecione um funcionário na tabela!");
        }
    }

    private void atualizarTabelaSalario(double salario) {
        ObservableList<Double> salarios = FXCollections.observableArrayList(salario);
        tabelinhaSalario.setItems(salarios);
    }

    private void atualizarTabela() {
        tabelaFolhaPagamento.refresh();
    }

    private void limparCampos() {
        txtNome.clear();
        txtCargo.clear();
        txtNivel.clear();
        tabelinhaSalario.getItems().clear();
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}