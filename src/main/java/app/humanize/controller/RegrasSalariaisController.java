package app.humanize.controller;

import app.humanize.model.RegraSalarial;
import app.humanize.repository.SalarioRepository;
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
import java.util.stream.Collectors;

public class RegrasSalariaisController {

    @FXML private ChoiceBox<String> CBcargo;
    @FXML private ChoiceBox<String> CBnivel;
    @FXML private TextField txtSalarioBase;
    @FXML private ChoiceBox<String> CBbeneficios;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;
    @FXML private Label lblAdicionalValor;
    @FXML private Label lblBeneficiosValor;
    @FXML private TableView<RegraSalarial> tableViewRegras;
    @FXML private TableColumn<RegraSalarial, String> colCargo;
    @FXML private TableColumn<RegraSalarial, String> colNivel;
    @FXML private TableColumn<RegraSalarial, Double> colSalarioBase;
    @FXML private TableColumn<RegraSalarial, Double> colAdicionalNivel;
    @FXML private TableColumn<RegraSalarial, Double> colBeneficios;
    @FXML private TableColumn<RegraSalarial, Double> colSalarioTotal;
    @FXML private Label lblTotalBase;
    @FXML private Label lblTotalAdicional;
    @FXML private Label lblTotalBeneficios;


    private final SalarioRepository salarioRepository = SalarioRepository.getInstance();
    private final ObservableList<String> cargosValidos = FXCollections.observableArrayList();
    private final ObservableList<String> niveisValidos = FXCollections.observableArrayList();
    private final ObservableList<String> beneficiosValidos = FXCollections.observableArrayList();
    private final Map<String, String> beneficioMap = new HashMap<>();

    private final ObservableList<RegraSalarial> regrasList = FXCollections.observableArrayList();
    private ResourceBundle bundle;
    private String currencyFormat;

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
        this.currencyFormat = bundle.getString("salaryRules.table.currencyFormat");

        carregarCargosValidos();
        carregarNiveis();
        carregarBeneficios();
        configurarTabela();
        carregarRegrasExistentes();
        configurarBotoes();
        configurarValidacoes();
        configurarListenersDeCalculo();

        calcularTotaisTabela();
    }

    private void configurarListenersDeCalculo() {
        // Listener
        CBnivel.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                NivelExperiencia nivel = NivelExperiencia.fromString(newVal);
                double adicional = (nivel != null) ? nivel.getAdicional() : 0.0;
                lblAdicionalValor.setText(String.format(currencyFormat, adicional));
            } else {
                lblAdicionalValor.setText(String.format(currencyFormat, 0.0));
            }
        });

        // Listener para Benefícios
        CBbeneficios.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                double valor = calcularValorBeneficios(newVal);
                lblBeneficiosValor.setText(String.format(currencyFormat, valor));
            } else {
                lblBeneficiosValor.setText(String.format(currencyFormat, 0.0));
            }
        });

        // dispara
        CBnivel.setValue(niveisValidos.get(0));
        CBbeneficios.setValue(beneficiosValidos.get(0));
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

        tableViewRegras.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                RegraSalarial regra = tableViewRegras.getSelectionModel().getSelectedItem();
                if (regra != null) {
                    mostrarAlerta("Detalhes da Regra",
                            "Você clicou duas vezes na regra para " + regra.getCargo() + " - " + regra.getNivel(),
                            Alert.AlertType.INFORMATION);
                }
            }
        });
    }

    private void configurarFormatacaoNumerica(TableColumn<RegraSalarial, Double> coluna) {
        coluna.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double valor, boolean vazio) {
                super.updateItem(valor, vazio);
                if (vazio || valor == null) {
                    setText(null);
                } else {
                    setText(String.format(currencyFormat, valor));
                }
            }
        });
    }

    private void carregarRegrasExistentes() {
        try {
            regrasList.clear();

            List<RegraSalarial> regras = salarioRepository.carregarTodasRegras();
            regrasList.addAll(regras);

            calcularTotaisTabela();

            System.out.println(bundle.getString("log.info.salaryRulesLoaded") + regrasList.size());

        } catch (Exception e) {
            mostrarAlerta(bundle.getString("alert.error.title"),
                    bundle.getString("salaryRules.alert.loadError") + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void calcularTotaisTabela() {
        double totalBase = regrasList.stream().mapToDouble(RegraSalarial::getSalarioBase).sum();
        double totalAdicional = regrasList.stream().mapToDouble(RegraSalarial::getAdicionalNivel).sum();
        double totalBeneficios = regrasList.stream().mapToDouble(RegraSalarial::getBeneficios).sum();

        if (lblTotalBase != null) lblTotalBase.setText(String.format(currencyFormat, totalBase));
        if (lblTotalAdicional != null) lblTotalAdicional.setText(String.format(currencyFormat, totalAdicional));
        if (lblTotalBeneficios != null) lblTotalBeneficios.setText(String.format(currencyFormat, totalBeneficios));
    }


    private void carregarCargosValidos() {
        List<String> cargosFixos = List.of("Analista de RH", "Recrutador Pleno", "Gerente de Contas", "Diretor");

        cargosValidos.setAll(cargosFixos.stream()
                .sorted()
                .collect(Collectors.toList()));

        if (CBcargo != null) {
            CBcargo.setItems(cargosValidos);
            if (!cargosValidos.isEmpty()) {
                CBcargo.setValue(cargosValidos.get(0));
            }
        }
    }

    private void carregarNiveis() {
        niveisValidos.clear();
        for (NivelExperiencia nivel : NivelExperiencia.values()) {
            niveisValidos.add(nivel.getDescricao());
        }

        if (CBnivel != null) {
            CBnivel.setItems(niveisValidos);
        }
    }

    private void carregarBeneficios() {
        beneficiosValidos.clear();
        beneficioMap.clear();

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
            CBbeneficios.setValue(bundle.getString("salaryRules.benefits.none"));
        }
    }

    private void addBeneficio(String translatedText, String internalKey) {
        beneficiosValidos.add(translatedText);
        beneficioMap.put(translatedText, internalKey);
    }

    private void configurarBotoes() {
        btnSalvar.setOnAction(event -> salvarRegra());
        if (btnCancelar != null) {
            btnCancelar.setOnAction(event -> limparCampos());
        }

        tableViewRegras.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tableViewRegras.getSelectionModel().isEmpty()) {
                limparCampos();
            }
        });
    }

    private void configurarValidacoes() {
        txtSalarioBase.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[\\d,.]*")) {
                txtSalarioBase.setText(newValue.replaceAll("[^\\d,.]", ""));
            }
        });
    }

    private double calcularValorBeneficios(String beneficioSelecionadoTraduzido) {
        String key = beneficioMap.getOrDefault(beneficioSelecionadoTraduzido, "NONE");

        return switch (key) {
            case "VR" -> VALOR_VALE_REFEICAO;
            case "VT" -> VALOR_VALE_TRANSPORTE;
            case "HEALTH" -> VALOR_PLANO_SAUDE;
            case "VR_VT" -> VALOR_VALE_REFEICAO + VALOR_VALE_TRANSPORTE;
            case "VR_HEALTH" -> VALOR_VALE_REFEICAO + VALOR_PLANO_SAUDE;
            case "VT_HEALTH" -> VALOR_VALE_TRANSPORTE + VALOR_PLANO_SAUDE;
            case "ALL" -> VALOR_VALE_REFEICAO + VALOR_VALE_TRANSPORTE + VALOR_PLANO_SAUDE;
            default -> 0.0;
        };
    }

    private void salvarRegra() {
        try {
            if (!validarCampos()) {
                mostrarAlerta(bundle.getString("alert.error.title"),
                        bundle.getString("salaryRules.alert.requiredFields"), Alert.AlertType.ERROR);
                return;
            }

            String cargo = CBcargo.getValue();
            String nivelDescricao = CBnivel.getValue();
            String beneficioTraduzido = CBbeneficios.getValue();

            NivelExperiencia nivel = NivelExperiencia.fromString(nivelDescricao);

            double salarioBase = validarEConverterDouble(txtSalarioBase.getText(),
                    bundle.getString("salaryRules.field.baseSalary"));

            if (salarioBase <= 0) {
                mostrarAlerta(bundle.getString("alert.error.title"),
                        bundle.getString("salaryRules.alert.nonPositiveSalary"), Alert.AlertType.ERROR);
                return;
            }

            double adicionalNivel = (nivel != null) ? nivel.getAdicional() : 0.0;
            double valorBeneficios = calcularValorBeneficios(beneficioTraduzido);
            double salarioTotal = salarioBase + adicionalNivel + valorBeneficios;

            RegraSalarial novaRegra = new RegraSalarial(cargo, nivel.getDescricao(), salarioBase, adicionalNivel, valorBeneficios, salarioTotal);

            boolean exists = regrasList.stream().anyMatch(r ->
                    r.getCargo().equalsIgnoreCase(cargo) && r.getNivel().equalsIgnoreCase(nivelDescricao));

            if (exists) {
                mostrarAlerta(bundle.getString("alert.error.title"),
                        bundle.getString("salaryRules.alert.ruleExists"), Alert.AlertType.WARNING);
                return;
            }

            try {
                salarioRepository.salvarRegra(novaRegra);
                regrasList.add(novaRegra);
                calcularTotaisTabela();
                mostrarMensagemSucesso(novaRegra);
                limparCampos();

            } catch (IOException e) {
                mostrarAlerta(bundle.getString("alert.error.title"),
                        bundle.getString("salaryRules.alert.saveError") + e.getMessage(), Alert.AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            mostrarAlerta(bundle.getString("alert.error.format.title"), e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarAlerta(bundle.getString("alert.error.title"),
                    bundle.getString("salaryRules.alert.genericSaveError") + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void mostrarMensagemSucesso(RegraSalarial regra) {
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
        if (CBnivel != null) {
            CBnivel.setValue(niveisValidos.get(0));
        }
        if (CBbeneficios != null) {
            CBbeneficios.setValue(bundle.getString("salaryRules.benefits.none"));
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