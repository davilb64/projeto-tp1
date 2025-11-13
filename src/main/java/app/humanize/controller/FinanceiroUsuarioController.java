package app.humanize.controller;

import app.humanize.model.FolhaPag;
import app.humanize.model.Funcionario;
import app.humanize.model.Usuario;
import app.humanize.repository.FolhaPagRepository;
import app.humanize.util.UserSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class FinanceiroUsuarioController {

    @FXML private Label lblTitulo;
    @FXML private ImageView fotoPerfil;
    @FXML private Label lblNome;
    @FXML private Label lblCargo;
    @FXML private Label lblMatricula;
    @FXML private Label lblRegime;
    @FXML private TableView<FolhaPag> tblContracheques;
    @FXML private TableColumn<FolhaPag, String> colMesAno;
    @FXML private TableColumn<FolhaPag, String> colSalarioBruto;
    @FXML private TableColumn<FolhaPag, String> colDescontos;
    @FXML private TableColumn<FolhaPag, String> colSalarioLiquido;
    @FXML private TableColumn<FolhaPag, Void> colAcoesContracheque;

    @FXML private LineChart chartHistorico;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Usuario usuarioLogado = UserSession.getInstance().getUsuarioLogado();
    private FolhaPagRepository folhaRepo;
    private static final String FOTO_PADRAO = "src/main/resources/fotos_perfil/default_avatar.png";
    private ResourceBundle bundle;

    @FXML
    public void initialize(){
        this.bundle = UserSession.getInstance().getBundle();
        this.folhaRepo = FolhaPagRepository.getInstance();

        carregarFotoPerfil();

        // perfil
        lblTitulo.setText(bundle.getString("userFinancial.title") + " " + usuarioLogado.getNome());
        lblNome.setText(usuarioLogado.getNome());
        Funcionario funcionarioLogado = (Funcionario)usuarioLogado;
        lblMatricula.setText(String.valueOf(funcionarioLogado.getMatricula()));
        lblCargo.setText(funcionarioLogado.getCargo());
        lblRegime.setText(String.valueOf(funcionarioLogado.getRegime()));

        // carregar a tabela
        configurarTabela();
        carregarContracheques();
    }


    private void configurarTabela() {
        String currencyFormat = bundle.getString("payroll.table.currencyFormat");

        colMesAno.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMesAno())
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

        colAcoesContracheque.setCellFactory(param -> new TableCell<>() {
            private final Button btnVisualizar = new Button(bundle.getString("financialProfile.payslips.button.view"));

            {
                btnVisualizar.setOnAction(event -> {
                    FolhaPag folha = getTableView().getItems().get(getIndex());
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
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void carregarContracheques() {
        String nomeUsuarioLogado = usuarioLogado.getNome();
        List<FolhaPag> todasFolhas = folhaRepo.carregarTodasFolhas();
        List<FolhaPag> folhasDoUsuario = todasFolhas.stream()
                .filter(folha -> folha.getNome().equalsIgnoreCase(nomeUsuarioLogado))
                .sorted(Comparator.comparing(folha -> {
                    try {
                        return LocalDate.parse(folha.getMesAno(), dateFormatter);
                    } catch (Exception e) {
                        return LocalDate.MIN;
                    }
                }))
                .collect(Collectors.toList());

        tblContracheques.getItems().setAll(folhasDoUsuario);

        carregarHistoricoFinanceiro(folhasDoUsuario);
    }

    private void carregarHistoricoFinanceiro(List<FolhaPag> folhas) {
        chartHistorico.getData().clear();

        XYChart.Series<String, Number> seriesLiquido = new XYChart.Series<>();
        seriesLiquido.setName(bundle.getString("financialProfile.history.series.net"));

        XYChart.Series<String, Number> seriesBruto = new XYChart.Series<>();
        seriesBruto.setName(bundle.getString("financialProfile.history.series.gross"));

        for (FolhaPag folha : folhas) {
            String mesAno = folha.getMesAno();

            double liquido = folha.getSalarioLiquido();
            double bruto = folha.getSalarioBase();

            seriesLiquido.getData().add(new XYChart.Data<>(mesAno, liquido));
            seriesBruto.getData().add(new XYChart.Data<>(mesAno, bruto));
        }

        chartHistorico.getData().addAll(seriesLiquido, seriesBruto);
    }

    private void mostrarAlertaDetalhes(FolhaPag folha) {
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