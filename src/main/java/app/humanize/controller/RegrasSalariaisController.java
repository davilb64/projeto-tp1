package app.humanize.controller;

import app.humanize.model.RegraSalarial;
import app.humanize.repository.SalarioRepository;
import app.humanize.repository.UsuarioRepository;
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

    // --- Controles de Formulário ---
    @FXML private ChoiceBox<String> CBcargo;
    @FXML private ChoiceBox<String> CBnivel;
    @FXML private TextField txtSalarioBase;
    @FXML private ChoiceBox<String> CBbeneficios;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;

    // --- Labels de Feedback (Novas) ---
    @FXML private Label lblAdicionalValor;
    @FXML private Label lblBeneficiosValor;

    // --- Tabela de Regras ---
    @FXML private TableView<RegraSalarial> tableViewRegras;
    @FXML private TableColumn<RegraSalarial, String> colCargo;
    @FXML private TableColumn<RegraSalarial, String> colNivel;
    @FXML private TableColumn<RegraSalarial, Double> colSalarioBase;
    @FXML private TableColumn<RegraSalarial, Double> colAdicionalNivel;
    @FXML private TableColumn<RegraSalarial, Double> colBeneficios;
    @FXML private TableColumn<RegraSalarial, Double> colSalarioTotal;

    // --- Labels de Totais da Tabela (Novas) ---
    @FXML private Label lblTotalBase;
    @FXML private Label lblTotalAdicional;
    @FXML private Label lblTotalBeneficios;


    private final SalarioRepository salarioRepository = SalarioRepository.getInstance();
    private final ObservableList<String> cargosValidos = FXCollections.observableArrayList();
    private final ObservableList<String> niveisValidos = FXCollections.observableArrayList();
    private final ObservableList<String> beneficiosValidos = FXCollections.observableArrayList();
    private final Map<String, String> beneficioMap = new HashMap<>(); // Mapeia Texto Traduzido -> Chave interna

    private final ObservableList<RegraSalarial> regrasList = FXCollections.observableArrayList();
    private ResourceBundle bundle;
    private String currencyFormat;


    // Enum movido para o topo, conforme solicitado. Mantido por compatibilidade.
    public enum NivelExperiencia {
        JUNIOR("Júnior", 0.0),
        PLENO("Pleno", 100.0),
        SENIOR("Sênior", 250.0),
        ESPECIALISTA("Especialista", 400.0),
        LIDER("Líder", 600.0);

        // ... (resto do enum) ...
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

        carregarCargosValidos(); // Usando lista de cargos fixos/lógicos
        carregarNiveis();
        carregarBeneficios();
        configurarTabela();
        carregarRegrasExistentes();
        configurarBotoes();
        configurarValidacoes();
        configurarListenersDeCalculo();

        // Recalcula totais da tabela após carregamento
        calcularTotaisTabela();
    }

    // --- NOVO: Configura Listeners para atualizar os valores de feedback ---
    private void configurarListenersDeCalculo() {
        // Listener para Nível: Atualiza Adicional
        CBnivel.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                NivelExperiencia nivel = NivelExperiencia.fromString(newVal);
                double adicional = (nivel != null) ? nivel.getAdicional() : 0.0;
                lblAdicionalValor.setText(String.format(currencyFormat, adicional));
            } else {
                lblAdicionalValor.setText(String.format(currencyFormat, 0.0));
            }
        });

        // Listener para Benefícios: Atualiza Valor de Benefício
        CBbeneficios.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                double valor = calcularValorBeneficios(newVal);
                lblBeneficiosValor.setText(String.format(currencyFormat, valor));
            } else {
                lblBeneficiosValor.setText(String.format(currencyFormat, 0.0));
            }
        });

        // Inicializa os valores (dispara os listeners)
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

        // Aplica a formatação de moeda em todas as colunas de valor
        configurarFormatacaoNumerica(colSalarioBase);
        configurarFormatacaoNumerica(colAdicionalNivel);
        configurarFormatacaoNumerica(colBeneficios);
        configurarFormatacaoNumerica(colSalarioTotal);

        tableViewRegras.setItems(regrasList);

        // NOVO: Adiciona listener para duplo clique (para edição futura ou visualização)
        tableViewRegras.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                RegraSalarial regra = tableViewRegras.getSelectionModel().getSelectedItem();
                if (regra != null) {
                    mostrarAlerta("Detalhes da Regra",
                            "Você clicou duas vezes na regra para " + regra.getCargo() + " - " + regra.getNivel(),
                            Alert.AlertType.INFORMATION);
                    // Aqui você implementaria a lógica de edição/visualização
                }
            }
        });
    }

    private void configurarFormatacaoNumerica(TableColumn<RegraSalarial, Double> coluna) {
        coluna.setCellFactory(tc -> new TableCell<RegraSalarial, Double>() {
            @Override
            protected void updateItem(Double valor, boolean vazio) {
                super.updateItem(valor, vazio);
                if (vazio || valor == null) {
                    setText(null);
                } else {
                    // Usa o formato de moeda da sessão
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

    // --- NOVO: Lógica para calcular e exibir os totais da tabela ---
    private void calcularTotaisTabela() {
        double totalBase = regrasList.stream().mapToDouble(RegraSalarial::getSalarioBase).sum();
        double totalAdicional = regrasList.stream().mapToDouble(RegraSalarial::getAdicionalNivel).sum();
        double totalBeneficios = regrasList.stream().mapToDouble(RegraSalarial::getBeneficios).sum();

        // Se as Labels de totais existirem no FXML, atualiza
        if (lblTotalBase != null) lblTotalBase.setText(String.format(currencyFormat, totalBase));
        if (lblTotalAdicional != null) lblTotalAdicional.setText(String.format(currencyFormat, totalAdicional));
        if (lblTotalBeneficios != null) lblTotalBeneficios.setText(String.format(currencyFormat, totalBeneficios));
    }


    private void carregarCargosValidos() {
        // Simulação de cargos válidos para regras. (Pode vir de um CSV ou ser fixo)
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
            // Usa a descrição hardcoded do Enum para o ChoiceBox
            niveisValidos.add(nivel.getDescricao());
        }

        if (CBnivel != null) {
            CBnivel.setItems(niveisValidos);
        }
    }

    private void carregarBeneficios() {
        beneficiosValidos.clear();
        beneficioMap.clear();

        // Mapeia a chave de tradução para a chave de lógica interna (melhor para internacionalização)
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

        // NOVO: Permite limpar campos com duplo clique no cabeçalho da tabela (para edição)
        tableViewRegras.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tableViewRegras.getSelectionModel().isEmpty()) {
                limparCampos();
            }
        });
    }

    private void configurarValidacoes() {
        txtSalarioBase.textProperty().addListener((observable, oldValue, newValue) -> {
            // Permite dígitos, vírgula ou ponto (o parser lidará com isso)
            if (!newValue.matches("[\\d,.]*")) {
                txtSalarioBase.setText(newValue.replaceAll("[^\\d,.]", ""));
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
            String nivelDescricao = CBnivel.getValue();
            String beneficioTraduzido = CBbeneficios.getValue();

            NivelExperiencia nivel = NivelExperiencia.fromString(nivelDescricao);

            // Centraliza o parsing e validação de base salarial
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

            // Verifica se a regra já existe (para evitar duplicidade)
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
                calcularTotaisTabela(); // Atualiza os totais
                mostrarMensagemSucesso(novaRegra);
                limparCampos();

            } catch (IOException e) {
                mostrarAlerta(bundle.getString("alert.error.title"),
                        bundle.getString("salaryRules.alert.saveError") + e.getMessage(), Alert.AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            // A exceção já carrega a mensagem formatada
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

    /**
     * Valida se o texto é numérico e converte, tratando "," como decimal.
     * Lança NumberFormatException com mensagem formatada se falhar.
     */
    private double validarEConverterDouble(String valor, String campo) throws NumberFormatException {
        try {
            // Tenta converter, substituindo vírgula por ponto (padrão Double.parseDouble)
            return Double.parseDouble(valor.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new NumberFormatException(String.format(bundle.getString("salaryRules.alert.validation.mustBeNumeric"), campo));
        }
    }

    private void limparCampos() {
        // Reseta para o primeiro item/padrão
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