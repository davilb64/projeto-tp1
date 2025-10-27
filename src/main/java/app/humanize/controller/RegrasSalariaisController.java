package app.humanize.controller;

import app.humanize.repository.SalarioRepository;
import app.humanize.repository.VagaRepository;
import app.humanize.model.RegraSalarial;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import java.io.IOException;
import java.util.List;

public class RegrasSalariaisController {

    @FXML private TextField txtCargo;
    @FXML private TextField txtNivel;
    @FXML private TextField txtSalarioBase;
    @FXML private TextField txtBeneficios;

    @FXML private Button btnSalvar;

    @FXML private TableView<RegraSalarial> tabelaRegras;
    @FXML private TableColumn<RegraSalarial, String> colunaCargo;
    @FXML private TableColumn<RegraSalarial, String> colunaNivel;
    @FXML private TableColumn<RegraSalarial, Double> colunaSalarioTotal;

    private final SalarioRepository salarioRepository = SalarioRepository.getInstance();
    private final VagaRepository vagaRepository = VagaRepository.getInstance();
    private final ObservableList<RegraSalarial> regrasSalariais = FXCollections.observableArrayList();
    private final ObservableList<String> cargosValidos = FXCollections.observableArrayList();

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

    private static final double VALOR_VALE_REFEICAO = 350.0;
    private static final double VALOR_PLANO_SAUDE = 200.0;

    @FXML
    private void initialize() {
        carregarCargosDoRepository();
        configurarTabela();
        configurarBotoes();
        configurarValidacoes();
        carregarRegrasExistentes();
    }

    private void configurarTabela() {
        if (tabelaRegras != null) {
            colunaCargo.setCellValueFactory(cellData -> cellData.getValue().cargoProperty());
            colunaNivel.setCellValueFactory(cellData -> cellData.getValue().nivelProperty());
            colunaSalarioTotal.setCellValueFactory(cellData -> cellData.getValue().salarioTotalProperty().asObject());

            tabelaRegras.setItems(regrasSalariais);
        }
    }

    private void carregarCargosDoRepository() {
        cargosValidos.clear();
        cargosValidos.addAll(vagaRepository.getTodosCargos());
        System.out.println("Cargos carregados: " + cargosValidos.size());
    }

    private void carregarRegrasExistentes() {
        try {
            List<RegraSalarial> regras = salarioRepository.carregarTodasRegras();
            regrasSalariais.clear();
            regrasSalariais.addAll(regras);
        } catch (Exception e) {
            System.err.println("Erro ao carregar regras: " + e.getMessage());
        }
    }

    private void configurarBotoes() {
        btnSalvar.setOnAction(event -> salvarRegra());
    }

    private void configurarValidacoes() {
        txtCargo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                validarCargo(newValue);
            } else {
                if (txtCargo != null) txtCargo.setStyle("");
            }
        });
    }

    private void validarCargo(String cargo) {
        if (txtCargo == null) return;

        boolean encontrado = cargosValidos.stream()
                .anyMatch(cargoLista -> cargoLista.equalsIgnoreCase(cargo.trim()));

        if (encontrado) {
            txtCargo.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
        } else {
            txtCargo.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        }
    }

    private double calcularValorBeneficios(String textoBeneficios) {
        if (textoBeneficios == null || textoBeneficios.trim().isEmpty()) {
            return 0.0;
        }

        double totalBeneficios = 0.0;
        String texto = textoBeneficios.toLowerCase().trim();

        if (texto.contains("vale refeicao") || texto.contains("vr") || texto.contains("vale refeição")) {
            totalBeneficios += VALOR_VALE_REFEICAO;
        }

        if (texto.contains("plano de saude") || texto.contains("saude") || texto.contains("plano saúde") || texto.contains("saúde")) {
            totalBeneficios += VALOR_PLANO_SAUDE;
        }

        return totalBeneficios;
    }

    private void salvarRegra() {
        try {
            if (!validarCampos()) {
                mostrarAlerta("Erro", "Preencha todos os campos obrigatórios.", Alert.AlertType.ERROR);
                return;
            }

            String cargo = txtCargo.getText().trim();

            boolean cargoEncontrado = cargosValidos.stream()
                    .anyMatch(cargoLista -> cargoLista.equalsIgnoreCase(cargo));

            if (!cargoEncontrado) {
                mostrarCargosDisponiveis(cargo);
                return;
            }

            String nivelTexto = txtNivel.getText().trim();
            NivelExperiencia nivel = NivelExperiencia.fromString(nivelTexto);

            if (nivel == null) {
                mostrarNiveisDisponiveis(nivelTexto);
                return;
            }

            double salarioBase = validarEConverterDouble(txtSalarioBase.getText(), "Salário Base");
            if (salarioBase < 0) {
                mostrarAlerta("Erro", "Salário base não pode ser negativo.", Alert.AlertType.ERROR);
                return;
            }

            String textoBeneficios = txtBeneficios.getText().trim();
            double valorBeneficios = calcularValorBeneficios(textoBeneficios);
            double salarioTotal = salarioBase + nivel.getAdicional() + valorBeneficios;

            // Criar objeto RegraSalarial
            RegraSalarial novaRegra = new RegraSalarial(cargo, nivel.getDescricao(), salarioBase, nivel.getAdicional(), valorBeneficios, salarioTotal);

            // Salvar no repositório
            try {
                salarioRepository.salvarRegra(novaRegra);
                regrasSalariais.add(novaRegra);

                mostrarMensagemSucesso(novaRegra);
                limparCampos();

            } catch (IOException e) {
                mostrarAlerta("Erro", "Erro ao salvar no repositório: " + e.getMessage(), Alert.AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            mostrarAlerta("Erro de Formato", "Verifique os valores numéricos digitados.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao salvar regra: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void mostrarCargosDisponiveis(String cargoDigitado) {
        StringBuilder cargosDisponiveis = new StringBuilder("Cargos válidos:\n");
        for (String cargoValido : cargosValidos) {
            cargosDisponiveis.append("• ").append(cargoValido).append("\n");
        }

        mostrarAlerta("Cargo Inválido",
                "O cargo '" + cargoDigitado + "' não está cadastrado no sistema.\n\n" +
                        cargosDisponiveis.toString(),
                Alert.AlertType.ERROR);
    }

    private void mostrarNiveisDisponiveis(String nivelDigitado) {
        StringBuilder niveisDisponiveis = new StringBuilder("Níveis válidos:\n");
        for (NivelExperiencia nivel : NivelExperiencia.values()) {
            niveisDisponiveis.append("• ").append(nivel.getDescricao()).append("\n");
        }

        mostrarAlerta("Nível Inválido",
                "Nível '" + nivelDigitado + "' não reconhecido.\n\n" +
                        niveisDisponiveis.toString(),
                Alert.AlertType.ERROR);
    }

    private void mostrarMensagemSucesso(RegraSalarial regra) {
        String mensagemSucesso =
                " **REGRA SALARIAL SALVA COM SUCESSO!**\n\n" +
                        " **DETALHAMENTO DOS VALORES:**\n" +
                        "────────────────────────────\n" +
                        "• Cargo: " + regra.getCargo() + "\n" +
                        "• Nível: " + regra.getNivel() + "\n" +
                        "• Salário Base: R$ " + String.format("%.2f", regra.getSalarioBase()) + "\n" +
                        "• Adicional do Nível: R$ " + String.format("%.2f", regra.getAdicionalNivel()) + "\n" +
                        "• Benefícios: R$ " + String.format("%.2f", regra.getBeneficios()) + "\n" +
                        "────────────────────────────\n" +
                        " **SALÁRIO TOTAL: R$ " + String.format("%.2f", regra.getSalarioTotal()) + "**";

        mostrarAlerta(" Sucesso", mensagemSucesso, Alert.AlertType.INFORMATION);
    }

    private boolean validarCampos() {
        return txtCargo != null && !txtCargo.getText().trim().isEmpty() &&
                txtNivel != null && !txtNivel.getText().trim().isEmpty() &&
                txtSalarioBase != null && !txtSalarioBase.getText().trim().isEmpty();
    }

    private double validarEConverterDouble(String valor, String campo) throws NumberFormatException {
        try {
            return Double.parseDouble(valor.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Campo '" + campo + "' deve conter um valor numérico válido.");
        }
    }

    private void limparCampos() {
        if (txtCargo != null) {
            txtCargo.clear();
            txtCargo.setStyle("");
        }
        if (txtNivel != null) txtNivel.clear();
        if (txtSalarioBase != null) txtSalarioBase.clear();
        if (txtBeneficios != null) txtBeneficios.clear();
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    public void atualizarCargos() {
        carregarCargosDoRepository();
    }
}