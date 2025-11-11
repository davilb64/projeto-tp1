package app.humanize.controller;

import app.humanize.model.FolhaPag; // NOVO
import app.humanize.model.Funcionario;
import app.humanize.model.Usuario;
import app.humanize.repository.FolhaPagRepository; // NOVO
import app.humanize.util.UserSession;
import javafx.beans.property.SimpleStringProperty; // NOVO
import javafx.fxml.FXML;
import javafx.geometry.Pos; // NOVO
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*; // NOVO
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List; // NOVO
import java.util.ResourceBundle;
import java.util.stream.Collectors; // NOVO

public class FinanceiroUsuarioController {

    @FXML private Label lblTitulo;
    @FXML private ImageView fotoPerfil;
    @FXML private Label lblNome;
    @FXML private Label lblCargo;
    @FXML private Label lblMatricula;
    @FXML private Label lblRegime;

    // MODIFICADO: Adicionado <FolhaPag> e os tipos das colunas
    @FXML private TableView<FolhaPag> tblContracheques;
    @FXML private TableColumn<FolhaPag, String> colMesAno;
    @FXML private TableColumn<FolhaPag, String> colSalarioBruto;
    @FXML private TableColumn<FolhaPag, String> colDescontos;
    @FXML private TableColumn<FolhaPag, String> colSalarioLiquido;
    @FXML private TableColumn<FolhaPag, Void> colAcoesContracheque; // 'Void' é usado para colunas de ação

    @FXML private VBox beneficiosContainer;
    @FXML private Label lblVT;
    @FXML private Label lblVA;
    @FXML private Label lblPlanoSaude;
    @FXML private Label lblOutrosBeneficios;
    @FXML private LineChart chartHistorico;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // --- Repositórios e Variáveis ---
    private Usuario usuarioLogado = UserSession.getInstance().getUsuarioLogado();
    private FolhaPagRepository folhaRepo; // NOVO
    private static final String FOTO_PADRAO = "src/main/resources/fotos_perfil/default_avatar.png";
    private ResourceBundle bundle;

    @FXML // MODIFICADO: método initialize() atualizado
    public void initialize(){
        this.bundle = UserSession.getInstance().getBundle();
        this.folhaRepo = FolhaPagRepository.getInstance(); // NOVO

        carregarFotoPerfil();

        // Configurações do perfil
        lblTitulo.setText(bundle.getString("userFinancial.title") + " " + usuarioLogado.getNome());
        lblNome.setText(usuarioLogado.getNome());
        Funcionario funcionarioLogado = (Funcionario)usuarioLogado;
        lblMatricula.setText(String.valueOf(funcionarioLogado.getMatricula()));
        lblCargo.setText(funcionarioLogado.getCargo());
        lblRegime.setText(String.valueOf(funcionarioLogado.getRegime()));

        // NOVO: Chamada para configurar e carregar a tabela
        configurarTabela();
        carregarContracheques();
    }

    /**
     * NOVO: Configura as colunas da tabela de contracheques.
     */
    private void configurarTabela() {
        // Pega o formato de moeda do bundle (ex: "R$ %.2f")
        // Assumindo que a chave existe, conforme FolhaDePagamentoController
        String currencyFormat = bundle.getString("payroll.table.currencyFormat");

        // --- Configuração das colunas de dados ---

        // Assumindo que seu FolhaPag tem um método getData() ou similar
        // Se o FXML tem "colMesAno", seu modelo 'FolhaPag' precisa ter esse dado.
        colMesAno.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMesAno()) // Mude .getData() se o método for outro
        );

        colSalarioBruto.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format(currencyFormat, cellData.getValue().getSalarioBase()))
        );

        colDescontos.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format(currencyFormat, cellData.getValue().getDescontos()))
        );

        colSalarioLiquido.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format(currencyFormat, cellData.getValue().getSalarioLiquido()))
        );

        // --- Configuração da Coluna de Ações (Botões) ---
        colAcoesContracheque.setCellFactory(param -> new TableCell<FolhaPag, Void>() {
            // Usando uma chave de bundle para o texto do botão
            private final Button btnVisualizar = new Button(bundle.getString("financialProfile.payslips.button.view"));

            {
                // Ação do botão
                btnVisualizar.setOnAction(event -> {
                    // Pega o objeto FolhaPag da linha clicada
                    FolhaPag folha = getTableView().getItems().get(getIndex());

                    // Ação de exemplo: mostrar um alerta com os detalhes
                    mostrarAlertaDetalhes(folha);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnVisualizar);
                    setAlignment(Pos.CENTER); // Centraliza o botão na célula
                }
            }
        });
    }

    private void carregarContracheques() {
        String nomeUsuarioLogado = usuarioLogado.getNome();

        // 1. Carrega TODAS as folhas salvas
        List<FolhaPag> todasFolhas = folhaRepo.carregarTodasFolhas();

        // 2. Filtra a lista para incluir apenas as do usuário logado
        // MODIFICADO: Adicionado .sorted() para ordenar por data
        List<FolhaPag> folhasDoUsuario = todasFolhas.stream()
                .filter(folha -> folha.getNome().equalsIgnoreCase(nomeUsuarioLogado))
                .sorted(Comparator.comparing(folha -> {
                    try {
                        // Tenta parsear a data para ordenar corretamente
                        return LocalDate.parse(folha.getMesAno(), dateFormatter);
                    } catch (Exception e) {
                        // Se falhar, coloca no início da lista
                        return LocalDate.MIN;
                    }
                }))
                .collect(Collectors.toList());

        // 3. Define os itens na tabela
        tblContracheques.getItems().setAll(folhasDoUsuario);

        // 4. NOVO: Chama o método para preencher o gráfico
        carregarHistoricoFinanceiro(folhasDoUsuario);
    }

    private void carregarHistoricoFinanceiro(List<FolhaPag> folhas) {
        // Limpa dados antigos do gráfico
        chartHistorico.getData().clear();

        // Cria as "séries" (as linhas do gráfico)
        // O eixo X (Categorias) é String, o eixo Y (Valores) é Number
        XYChart.Series<String, Number> seriesLiquido = new XYChart.Series<>();
        seriesLiquido.setName(bundle.getString("financialProfile.history.series.net")); // Salário Líquido

        XYChart.Series<String, Number> seriesBruto = new XYChart.Series<>();
        seriesBruto.setName(bundle.getString("financialProfile.history.series.gross")); // Salário Bruto

        // Itera sobre as folhas de pagamento (já ordenadas)
        for (FolhaPag folha : folhas) {
            String mesAno = folha.getMesAno(); // O valor do Eixo X (ex: "28/02/2025")

            // Os valores do Eixo Y
            double liquido = folha.getSalarioLiquido();
            double bruto = folha.getSalarioBase(); // Usando Salário Base como "Bruto"

            // Adiciona os pontos de dados em cada série
            seriesLiquido.getData().add(new XYChart.Data<>(mesAno, liquido));
            seriesBruto.getData().add(new XYChart.Data<>(mesAno, bruto));
        }

        // Adiciona as séries (linhas) prontas ao gráfico
        chartHistorico.getData().addAll(seriesLiquido, seriesBruto);
    }

    /**
     * NOVO: Método de exemplo para o botão de ação.
     */
    private void mostrarAlertaDetalhes(FolhaPag folha) {
        // Reutilizando a formatação de moeda
        String currencyFormat = bundle.getString("payroll.table.currencyFormat");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(bundle.getString("financialProfile.payslips.alert.title"));
        alert.setHeaderText(bundle.getString("financialProfile.payslips.alert.header") + " " + folha.getData());

        String conteudo = String.format(bundle.getString("financialProfile.payslips.alert.content.base"), String.format(currencyFormat, folha.getSalarioBase())) + "\n" +
                String.format(bundle.getString("financialProfile.payslips.alert.content.levelBonus"), String.format(currencyFormat, folha.getAdicionalNivel())) + "\n" +
                String.format(bundle.getString("financialProfile.payslips.alert.content.benefits"), String.format(currencyFormat, folha.getBeneficios())) + "\n" +
                String.format(bundle.getString("financialProfile.payslips.alert.content.additions"), String.format(currencyFormat, folha.getAdicionais())) + "\n" +
                String.format(bundle.getString("financialProfile.payslips.alert.content.deductions"), String.format(currencyFormat, folha.getDescontos())) + "\n\n" +
                String.format(bundle.getString("financialProfile.payslips.alert.content.netTotal"), String.format(currencyFormat, folha.getSalarioLiquido()));

        alert.setContentText(conteudo);
        alert.showAndWait();
    }

    /**
     * Carrega a foto do perfil do usuário.
     */
    private void carregarFotoPerfil() {
        Usuario usuario = UserSession.getInstance().getUsuarioLogado();
        String caminhoFoto = null;

        if (usuario instanceof Funcionario) {
            caminhoFoto = ((Funcionario) usuario).getCaminhoFoto();
        }

        try {
            if (caminhoFoto != null && !caminhoFoto.isEmpty()) {
                fotoPerfil.setImage(new Image(new FileInputStream(caminhoFoto)));
            } else {
                fotoPerfil.setImage(new Image(new FileInputStream(FOTO_PADRAO)));
            }
        } catch (FileNotFoundException e) {
            System.err.println(bundle.getString("log.error.photoNotFound") + (caminhoFoto != null ? caminhoFoto : FOTO_PADRAO));
            try {
                fotoPerfil.setImage(new Image(new FileInputStream(FOTO_PADRAO)));
            } catch (FileNotFoundException ex) {
                System.err.println(bundle.getString("log.error.photoDefaultNotFound") + FOTO_PADRAO);
            }
        }
    }
}