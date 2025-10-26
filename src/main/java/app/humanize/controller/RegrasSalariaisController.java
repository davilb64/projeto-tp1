package app.humanize.controller;

import app.humanize.repository.VagaRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import java.util.HashMap;
import java.util.Map;

public class RegrasSalariaisController {

    @FXML private TextField txtCargo;
    @FXML private TextField txtNivel;
    @FXML private TextField txtSalarioBase;
    @FXML private TextField txtBeneficios;
    @FXML private Button btnSalvar;

    private VagaRepository vagaRepository = VagaRepository.getInstance();
    private Map<String, String> regrasSalvas = new HashMap<>();
    private ObservableList<String> cargosValidos = FXCollections.observableArrayList();


    private final Map<String, Double> adicionaisPorNivel = Map.of(
            "Júnior", 0.0,
            "Pleno", 100.0,
            "Sênior", 250.0,
            "Especialista", 400.0,
            "Líder", 600.0
    );

    private final double VALOR_VALE_REFEICAO = 350.0;
    private final double VALOR_PLANO_SAUDE = 200.0;

    @FXML
    private void initialize() {
        carregarCargosDoRepository();
        configurarBotoes();
        configurarValidacoes();
    }

    private void carregarCargosDoRepository() {
        cargosValidos.clear();
        cargosValidos.addAll(vagaRepository.getTodosCargos());
        System.out.println("Cargos carregados: " + cargosValidos.size());
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
                StringBuilder cargosDisponiveis = new StringBuilder("Cargos válidos:\n");
                for (String cargoValido : cargosValidos) {
                    cargosDisponiveis.append("• ").append(cargoValido).append("\n");
                }

                mostrarAlerta("Cargo Inválido",
                        "O cargo '" + cargo + "' não está cadastrado no sistema.\n\n" +
                                cargosDisponiveis.toString(),
                        Alert.AlertType.ERROR);
                return;
            }

            String nivel = txtNivel.getText().trim();
            Double adicionalNivel = adicionaisPorNivel.get(nivel);
            if (adicionalNivel == null) {
                mostrarAlerta("Nível Inválido",
                        "Nível '" + nivel + "' não reconhecido.\n" +
                                "Níveis válidos: Júnior, Pleno, Sênior, Especialista, Líder",
                        Alert.AlertType.ERROR);
                return;
            }

            double salarioBase = validarEConverterDouble(txtSalarioBase.getText(), "Salário Base");

            if (salarioBase < 0) {
                mostrarAlerta("Erro", "Salário base não pode ser negativo.", Alert.AlertType.ERROR);
                return;
            }

            String textoBeneficios = txtBeneficios.getText().trim();
            double valorBeneficios = calcularValorBeneficios(textoBeneficios);
            double salarioTotal = salarioBase + adicionalNivel + valorBeneficios;

            String mensagemSucesso =
                    " **REGRA SALARIAL SALVA COM SUCESSO!**\n\n" +
                            " **DETALHAMENTO DOS VALORES:**\n" +
                            "────────────────────────────\n" +
                            "• Cargo: " + cargo + "\n" +
                            "• Nível: " + nivel + "\n" +
                            "• Salário Base: R$ " + String.format("%.2f", salarioBase) + "\n" +
                            "• Adicional do Nível: R$ " + String.format("%.2f", adicionalNivel) + "\n" +
                            "• Benefícios: R$ " + String.format("%.2f", valorBeneficios) + "\n" +
                            "────────────────────────────\n" +
                            " **SALÁRIO TOTAL: R$ " + String.format("%.2f", salarioTotal) + "**";

            String chave = cargo + "_" + nivel;
            String valor = String.format("Base: R$ %.2f | Adicional: R$ %.2f | Benefícios: R$ %.2f | TOTAL: R$ %.2f",
                    salarioBase, adicionalNivel, valorBeneficios, salarioTotal);
            regrasSalvas.put(chave, valor);

            mostrarAlerta("✅ Sucesso", mensagemSucesso, Alert.AlertType.INFORMATION);

            limparCampos();

        } catch (NumberFormatException e) {
            mostrarAlerta("Erro de Formato", "Verifique os valores numéricos digitados.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao salvar regra: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validarCampos() {
        return txtCargo != null && !txtCargo.getText().trim().isEmpty() &&
                txtNivel != null && !txtNivel.getText().trim().isEmpty() &&
                txtSalarioBase != null && !txtSalarioBase.getText().trim().isEmpty() &&
                txtBeneficios != null && !txtBeneficios.getText().trim().isEmpty();
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