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
    @FXML private Button btnSalvar;

    @FXML private Label lblAdicionalNivel;
    @FXML private Label lblTotalBeneficios;
    @FXML private Label lblValeRefeicao;
    @FXML private Label lblPlanoSaude;

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

    // Benefícios fixos
    private final double VALE_REFEICAO = 350.00;
    private final double PLANO_SAUDE = 200.00;

    @FXML
    private void initialize() {
        carregarCargosDoRepository();  // Carrega cargos do VagaRepository
        configurarBotoes();
        configurarValidacoes();
        configurarBeneficiosFixos();
    }

    private void carregarCargosDoRepository() {
        cargosValidos.clear();
        cargosValidos.addAll(vagaRepository.getTodosCargos());

    }

    private void configurarBotoes() {
        btnSalvar.setOnAction(event -> salvarRegra());
    }

    private void configurarValidacoes() {
        txtCargo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                validarCargo(newValue);
            } else {
                txtCargo.setStyle("");
            }
        });

        txtNivel.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                mostrarAdicionalNivel(newValue);
            } else {
                lblAdicionalNivel.setText("");
            }
        });
    }

    private void configurarBeneficiosFixos() {
        lblValeRefeicao.setText("Vale Refeição: R$ " + VALE_REFEICAO);
        lblPlanoSaude.setText("Plano de Saúde: R$ " + PLANO_SAUDE);

        double totalBeneficios = VALE_REFEICAO + PLANO_SAUDE;
        lblTotalBeneficios.setText("Total Benefícios: R$ " + totalBeneficios);
        lblTotalBeneficios.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
    }

    private void mostrarAdicionalNivel(String nivel) {
        Double adicional = adicionaisPorNivel.get(nivel);
        if (adicional != null) {
            lblAdicionalNivel.setText("Adicional " + nivel + ": + R$ " + adicional);
            lblAdicionalNivel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else {
            lblAdicionalNivel.setText("Nível inválido");
            lblAdicionalNivel.setStyle("-fx-text-fill: red;");
        }
    }

    private void validarCargo(String cargo) {

        // verifica se o cargo existe na lista do ricardo
        boolean cargoValido = cargosValidos.stream()
                .anyMatch(cargoVAlido -> cargoVAlido.equalsIgnoreCase(cargo.trim()));

        if (cargoValido) {
            txtCargo.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
        } else {
            txtCargo.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        }
    }

    private void salvarRegra() {
        try {
            if (!validarCampos()) {
                mostrarAlerta("Erro", "Preencha todos os campos obrigatórios.", Alert.AlertType.ERROR);
                return;
            }

            String cargo = txtCargo.getText().trim();

            boolean cargoValido = cargosValidos.stream()
                    .anyMatch(cargoVAlido -> cargoVAlido.equalsIgnoreCase(cargo));

            if (!cargoValido) {
                StringBuilder cargosDisponiveis = new StringBuilder("Cargos válidos:\n");
                for (String cargoVAlido : cargosValidos) {
                    cargosDisponiveis.append("• ").append(cargoVAlido).append("\n");
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

            // Cálculos
            double totalBeneficios = VALE_REFEICAO + PLANO_SAUDE;
            double salarioTotal = salarioBase + adicionalNivel + totalBeneficios;

            // Salvar a regra
            String chave = cargo + "_" + nivel;
            String valor = String.format("Salário Base: R$ %.2f | Adicional: R$ %.2f | Benefícios: R$ %.2f | TOTAL: R$ %.2f",
                    salarioBase, adicionalNivel, totalBeneficios, salarioTotal);
            regrasSalvas.put(chave, valor);

            mostrarAlerta("Sucesso",
                    "Regra salarial salva com sucesso!\n" +
                            "Cargo: " + cargo + "\n" +
                            "Nível: " + nivel + " (+ R$ " + adicionalNivel + ")\n" +
                            "Salário Base: R$ " + salarioBase + "\n" +
                            "Vale Refeição: R$ " + VALE_REFEICAO + "\n" +
                            "Plano de Saúde: R$ " + PLANO_SAUDE + "\n" +
                            "Total Benefícios: R$ " + totalBeneficios + "\n" +
                            "TOTAL: R$ " + salarioTotal,
                    Alert.AlertType.INFORMATION);

            limparCampos();

        } catch (NumberFormatException e) {
            mostrarAlerta("Erro de Formato", "Verifique os valores numéricos digitados.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao salvar regra: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validarCampos() {
        return !txtCargo.getText().trim().isEmpty() &&
                !txtNivel.getText().trim().isEmpty() &&
                !txtSalarioBase.getText().trim().isEmpty();
    }

    private double validarEConverterDouble(String valor, String campo) throws NumberFormatException {
        try {
            return Double.parseDouble(valor.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Campo '" + campo + "' deve conter um valor numérico válido.");
        }
    }

    private void limparCampos() {
        txtCargo.clear();
        txtNivel.clear();
        txtSalarioBase.clear();
        txtCargo.setStyle("");
        lblAdicionalNivel.setText("");
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