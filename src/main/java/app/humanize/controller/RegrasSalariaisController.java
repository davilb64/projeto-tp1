package app.humanize.controller;

import app.humanize.model.Funcionario;
import app.humanize.repository.SalarioRepository;
import app.humanize.repository.UsuarioRepository;
import app.humanize.model.RegraSalarial;
import app.humanize.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class RegrasSalariaisController {

    @FXML private ChoiceBox<String> CBcargo;
    @FXML private ChoiceBox<String> CBnivel;
    @FXML private TextField txtSalarioBase;
    @FXML private ChoiceBox<String> CBbeneficios;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;

    @FXML private TableView<RegraSalarial> tableViewRegras;
    @FXML private TableColumn<RegraSalarial, String> colCargo;
    @FXML private TableColumn<RegraSalarial, String> colNivel;
    @FXML private TableColumn<RegraSalarial, Double> colSalarioBase;
    @FXML private TableColumn<RegraSalarial, Double> colAdicionalNivel;
    @FXML private TableColumn<RegraSalarial, Double> colBeneficios;
    @FXML private TableColumn<RegraSalarial, Double> colSalarioTotal;

    private final SalarioRepository salarioRepository = SalarioRepository.getInstance();
    private final UsuarioRepository usuarioRepo = UsuarioRepository.getInstance();
    private final ObservableList<String> cargosValidos = FXCollections.observableArrayList();
    private final ObservableList<String> niveisValidos = FXCollections.observableArrayList();
    private final ObservableList<String> beneficiosValidos = FXCollections.observableArrayList();
    private final Map<String, String> beneficioMap = new HashMap<>(); // Mapeia Texto Traduzido -> Chave interna

    private final ObservableList<RegraSalarial> regrasList = FXCollections.observableArrayList();
    private ResourceBundle bundle;

    // Este enum é mantido como está ("Júnior", "Pleno") para não quebrar o método estático fromString()
    // que é usado em outros controllers (ex: FolhaDePagamentoController).
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
        this.bundle = UserSession.getInstance().getBundle();
        carregarCargosDoRepository();
        carregarNiveis();
        carregarBeneficios();
        configurarTabela();
        carregarRegrasExistentes();
        configurarBotoes();
        configurarValidacoes();
    }

    private void configurarTabela() {
        colCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        colNivel.setCellValueFactory(new PropertyValueFactory<>("nivel"));
        colSalarioBase.setCellValueFactory(new PropertyValueFactory<>("salarioBase"));
        colAdicionalNivel.setCellValueFactory(new PropertyValueFactory<>("adicionalNivel"));
        colBeneficios.setCellValueFactory(new PropertyValueFactory<>("beneficios"));
        colSalarioTotal.setCellValueFactory(new PropertyValueFactory<>("salarioTotal"));

        configurarFormatacaoNumerica(colSalarioBase);
        configurarFormatacaoNumerica(colAdicionalNivel);
        configurarFormatacaoNumerica(colBeneficios);
        configurarFormatacaoNumerica(colSalarioTotal);

        tableViewRegras.setItems(regrasList);
    }

    private void configurarFormatacaoNumerica(TableColumn<RegraSalarial, Double> coluna) {
        coluna.setCellFactory(tc -> new TableCell<RegraSalarial, Double>() {
            @Override
            protected void updateItem(Double valor, boolean vazio) {
                super.updateItem(valor, vazio);
                if (vazio || valor == null) {
                    setText(null);
                } else {
                    setText(String.format(bundle.getString("salaryRules.table.currencyFormat"), valor));
                }
            }
        });
    }

    private void carregarRegrasExistentes() {
        try {
            regrasList.clear();

            List<RegraSalarial> regras = salarioRepository.carregarTodasRegras();
            regrasList.addAll(regras);

            System.out.println(bundle.getString("log.info.salaryRulesLoaded") + regrasList.size());

        } catch (Exception e) {
            mostrarAlerta(bundle.getString("alert.error.title"),
                    bundle.getString("salaryRules.alert.loadError") + e.getMessage(), Alert.AlertType.ERROR);
        }
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
        // Logs
        System.out.println(bundle.getString("log.info.positionsLoaded") + cargosValidos.size());
        System.out.println(bundle.getString("log.info.positionsList") + cargosValidos);
    }

    private void carregarNiveis() {
        // Como o Enum NivelExperiencia é usado em outro controller (FolhaPagamento),
        // mantemos as descrições hardcoded ("Júnior", "Pleno") para garantir que
        // NivelExperiencia.fromString() funcione.
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
        beneficioMap.clear();

        // Mapeia a chave de tradução para a chave de lógica interna
        addBeneficio(bundle.getString("salaryRules.benefits.none"), "NONE");
        addBeneficio(bundle.getString("salaryRules.benefits.vr"), "VR");
        addBeneficio(bundle.getString("salaryRules.benefits.vt"), "VT");
        addBeneficio(bundle.getString("salaryRules.benefits.healthPlan"), "HEALTH");
        addBeneficio(bundle.getString("salaryRules.benefits.vr_vt"), "VR_VT");
        addBeneficio(bundle.getString("salaryRules.benefits.vr_health"), "VR_HEALTH");
        addBeneficio(bundle.getString("salaryRules.benefits.vt_health"), "VT_HEALTH");
        addBeneficio(bundle.getString("salaryRules.benefits.all"), "ALL");

        if (CBbeneficios != null) {
            CBbeneficios.setItems(beneficiosValidos);
            CBbeneficios.setValue(bundle.getString("salaryRules.benefits.none")); // Padrão
        }
    }

    // Helper para popular ChoiceBox e Mapa
    private void addBeneficio(String translatedText, String internalKey) {
        beneficiosValidos.add(translatedText);
        beneficioMap.put(translatedText, internalKey);
    }

    private void configurarBotoes() {
        btnSalvar.setOnAction(event -> salvarRegra());
        if (btnCancelar != null) {
            btnCancelar.setOnAction(event -> limparCampos());
        }
    }

    private void configurarValidacoes() {
        txtSalarioBase.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtSalarioBase.setText(newValue.replaceAll("[^\\d.]", ""));
            }
        });
    }

    private double calcularValorBeneficios(String beneficioSelecionadoTraduzido) {
        String key = beneficioMap.getOrDefault(beneficioSelecionadoTraduzido, "NONE");

        switch (key) {
            case "NONE":
                return 0.0;
            case "VR":
                return VALOR_VALE_REFEICAO;
            case "VT":
                return VALOR_VALE_TRANSPORTE;
            case "HEALTH":
                return VALOR_PLANO_SAUDE;
            case "VR_VT":
                return VALOR_VALE_REFEICAO + VALOR_VALE_TRANSPORTE;
            case "VR_HEALTH":
                return VALOR_VALE_REFEICAO + VALOR_PLANO_SAUDE;
            case "VT_HEALTH":
                return VALOR_VALE_TRANSPORTE + VALOR_PLANO_SAUDE;
            case "ALL":
                return VALOR_VALE_REFEICAO + VALOR_VALE_TRANSPORTE + VALOR_PLANO_SAUDE;
            default:
                return 0.0;
        }
    }

    private void salvarRegra() {
        try {
            if (!validarCampos()) {
                mostrarAlerta(bundle.getString("alert.error.title"),
                        bundle.getString("salaryRules.alert.requiredFields"), Alert.AlertType.ERROR);
                return;
            }

            String cargo = CBcargo.getValue();
            String nivelDescricao = CBnivel.getValue(); // "Júnior", "Pleno", etc.
            String beneficioTraduzido = CBbeneficios.getValue();

            NivelExperiencia nivel = NivelExperiencia.fromString(nivelDescricao);
            if (nivel == null) {
                // O usuário digitou algo inválido no ChoiceBox? Isso não deveria acontecer.
                // Mas se o enum falhar, mostramos o alerta.
                mostrarNiveisDisponiveis(nivelDescricao);
                return;
            }

            double salarioBase = validarEConverterDouble(txtSalarioBase.getText(),
                    bundle.getString("salaryRules.field.baseSalary"));
            if (salarioBase < 0) {
                mostrarAlerta(bundle.getString("alert.error.title"),
                        bundle.getString("salaryRules.alert.negativeSalary"), Alert.AlertType.ERROR);
                return;
            }

            double adicionalNivel = nivel.getAdicional();
            double valorBeneficios = calcularValorBeneficios(beneficioTraduzido);
            double salarioTotal = salarioBase + adicionalNivel + valorBeneficios;

            RegraSalarial novaRegra = new RegraSalarial(cargo, nivel.getDescricao(), salarioBase, adicionalNivel, valorBeneficios, salarioTotal);

            try {
                salarioRepository.salvarRegra(novaRegra);
                regrasList.add(novaRegra);
                mostrarMensagemSucesso(novaRegra);
                limparCampos();

            } catch (IOException e) {
                mostrarAlerta(bundle.getString("alert.error.title"),
                        bundle.getString("salaryRules.alert.saveError") + e.getMessage(), Alert.AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            mostrarAlerta(bundle.getString("alert.error.format.title"),
                    e.getMessage(), Alert.AlertType.ERROR); // A mensagem já está formatada
        } catch (Exception e) {
            mostrarAlerta(bundle.getString("alert.error.title"),
                    bundle.getString("salaryRules.alert.genericSaveError") + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void mostrarNiveisDisponiveis(String nivelDigitado) {
        StringBuilder niveisDisponiveis = new StringBuilder(bundle.getString("salaryRules.alert.invalidLevel.valid") + "\n");
        for (NivelExperiencia nivel : NivelExperiencia.values()) {
            niveisDisponiveis.append("• ").append(nivel.getDescricao()).append("\n"); // Mostra "Júnior", "Pleno"
        }

        mostrarAlerta(bundle.getString("salaryRules.alert.invalidLevel.title"),
                String.format(bundle.getString("salaryRules.alert.invalidLevel.header"), nivelDigitado) + "\n\n" +
                        niveisDisponiveis.toString(),
                Alert.AlertType.ERROR);
    }

    private void mostrarMensagemSucesso(RegraSalarial regra) {
        String currencyFormat = bundle.getString("salaryRules.table.currencyFormat");

        String mensagemSucesso =
                " **" + bundle.getString("salaryRules.alert.success.header") + "**\n\n" +
                        " **" + bundle.getString("salaryRules.alert.success.details") + "**\n" +
                        "────────────────────────────\n" +
                        "• " + bundle.getString("salaryRules.alert.success.position") + ": " + regra.getCargo() + "\n" +
                        "• " + bundle.getString("salaryRules.alert.success.level") + ": " + regra.getNivel() + "\n" +
                        "• " + bundle.getString("salaryRules.alert.success.baseSalary") + ": " + String.format(currencyFormat, regra.getSalarioBase()) + "\n" +
                        "• " + bundle.getString("salaryRules.alert.success.levelBonus") + ": " + String.format(currencyFormat, regra.getAdicionalNivel()) + "\n" +
                        "• " + bundle.getString("salaryRules.alert.success.benefits") + ": " + String.format(currencyFormat, regra.getBeneficios()) + "\n" +
                        "────────────────────────────\n" +
                        " **" + bundle.getString("salaryRules.alert.success.totalSalary") + ": " + String.format(currencyFormat, regra.getSalarioTotal()) + "**";

        mostrarAlerta(bundle.getString("alert.success.title"), mensagemSucesso, Alert.AlertType.INFORMATION);
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
            throw new NumberFormatException(String.format(bundle.getString("salaryRules.alert.validation.mustBeNumeric"), campo));
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
            CBbeneficios.setValue(bundle.getString("salaryRules.benefits.none")); // Padrão
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

}