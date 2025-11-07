package app.humanize.controller;

import app.humanize.model.Funcionario;
import app.humanize.repository.SalarioRepository;
import app.humanize.repository.UsuarioRepository;
import app.humanize.model.RegraSalarial;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import java.io.IOException;
import java.util.List;

public class RegrasSalariaisController {

    @FXML private ChoiceBox<String> CBcargo;
    @FXML private ChoiceBox<String> CBnivel;
    @FXML private TextField txtSalarioBase;
    @FXML private ChoiceBox<String> CBbeneficios;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;

    private final SalarioRepository salarioRepository = SalarioRepository.getInstance();
    private final UsuarioRepository usuarioRepo = UsuarioRepository.getInstance();
    private final ObservableList<String> cargosValidos = FXCollections.observableArrayList();
    private final ObservableList<String> niveisValidos = FXCollections.observableArrayList();
    private final ObservableList<String> beneficiosValidos = FXCollections.observableArrayList();

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
    private static final double VALOR_VALE_TRANSPORTE = 150.0;

    @FXML
    private void initialize() {
        carregarCargosDoRepository();
        carregarNiveis();
        carregarBeneficios();
        configurarBotoes();
        configurarValidacoes();
    }

    private void carregarCargosDoRepository() {
        cargosValidos.clear();
        cargosValidos.addAll(
                usuarioRepo.getFuncionarios().stream()
                        .filter(usuario -> usuario instanceof Funcionario)
                        .map(usuario -> (Funcionario) usuario)
                        .map(Funcionario::getCargo)
                        .filter(cargo -> cargo != null && !cargo.trim().isEmpty())
                        .distinct()
                        .toList()
        );

        if (CBcargo != null) {
            CBcargo.setItems(cargosValidos);
            if (!cargosValidos.isEmpty()) {
                CBcargo.setValue(cargosValidos.get(0));
            }
        }

        System.out.println("Cargos carregados: " + cargosValidos.size());
        System.out.println("Cargos: " + cargosValidos);
    }

    private void carregarNiveis() {
        niveisValidos.clear();

        for (NivelExperiencia nivel : NivelExperiencia.values()) {
            niveisValidos.add(nivel.getDescricao());
        }

        if (CBnivel != null) {
            CBnivel.setItems(niveisValidos);
            if (!niveisValidos.isEmpty()) {
                CBnivel.setValue(niveisValidos.get(0));
            }
        }
    }

    private void carregarBeneficios() {
        beneficiosValidos.clear();
        beneficiosValidos.addAll("Nenhum", "VR", "VT", "Plano Saúde", "VR + VT", "VR + Plano Saúde", "VT + Plano Saúde", "VR + VT + Plano Saúde");

        if (CBbeneficios != null) {
            CBbeneficios.setItems(beneficiosValidos);
            if (!beneficiosValidos.isEmpty()) {
                CBbeneficios.setValue(beneficiosValidos.get(0));
            }
        }
    }

    private void configurarBotoes() {
        btnSalvar.setOnAction(event -> salvarRegra());
        if (btnCancelar != null) {
            btnCancelar.setOnAction(event -> limparCampos());
        }
    }

    private void configurarValidacoes() {
        // Validação do salário base (apenas números)
        txtSalarioBase.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtSalarioBase.setText(newValue.replaceAll("[^\\d.]", ""));
            }
        });
    }

    private double calcularValorBeneficios(String beneficioSelecionado) {
        if (beneficioSelecionado == null || beneficioSelecionado.equals("Nenhum")) {
            return 0.0;
        }

        double totalBeneficios = 0.0;

        if (beneficioSelecionado.contains("VR")) {
            totalBeneficios += VALOR_VALE_REFEICAO;
        }
        if (beneficioSelecionado.contains("VT")) {
            totalBeneficios += VALOR_VALE_TRANSPORTE;
        }
        if (beneficioSelecionado.contains("Plano Saúde")) {
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

            String cargo = CBcargo.getValue();
            String nivelDescricao = CBnivel.getValue();
            String beneficio = CBbeneficios.getValue();

            NivelExperiencia nivel = NivelExperiencia.fromString(nivelDescricao);
            if (nivel == null) {
                mostrarNiveisDisponiveis(nivelDescricao);
                return;
            }

            double salarioBase = validarEConverterDouble(txtSalarioBase.getText(), "Salário Base");
            if (salarioBase < 0) {
                mostrarAlerta("Erro", "Salário base não pode ser negativo.", Alert.AlertType.ERROR);
                return;
            }

            double adicionalNivel = nivel.getAdicional();
            double valorBeneficios = calcularValorBeneficios(beneficio);
            double salarioTotal = salarioBase + adicionalNivel + valorBeneficios;

            RegraSalarial novaRegra = new RegraSalarial(cargo, nivel.getDescricao(), salarioBase, adicionalNivel, valorBeneficios, salarioTotal);

            try {
                salarioRepository.salvarRegra(novaRegra);
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

        mostrarAlerta("Sucesso", mensagemSucesso, Alert.AlertType.INFORMATION);
    }

    private boolean validarCampos() {
        return CBcargo.getValue() != null &&
                CBnivel.getValue() != null &&
                txtSalarioBase.getText() != null && !txtSalarioBase.getText().trim().isEmpty();
    }

    private double validarEConverterDouble(String valor, String campo) throws NumberFormatException {
        try {
            return Double.parseDouble(valor.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Campo '" + campo + "' deve conter um valor numérico válido.");
        }
    }

    private void limparCampos() {
        if (CBcargo != null && !cargosValidos.isEmpty()) {
            CBcargo.setValue(cargosValidos.get(0));
        }
        if (CBnivel != null && !niveisValidos.isEmpty()) {
            CBnivel.setValue(niveisValidos.get(0));
        }
        if (CBbeneficios != null && !beneficiosValidos.isEmpty()) {
            CBbeneficios.setValue(beneficiosValidos.get(0));
        }
        if (txtSalarioBase != null) {
            txtSalarioBase.clear();
        }
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