package app.humanize.controller;

import app.humanize.model.Funcionario;
import app.humanize.model.Usuario;
import app.humanize.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ResourceBundle;

public class FinanceiroUsuarioController {


    @FXML private Label lblTitulo;
    @FXML private ImageView fotoPerfil;
    @FXML private Label lblNome;
    @FXML private Label lblCargo;
    @FXML private Label lblMatricula;
    @FXML private Label lblRegime;
    @FXML private TableView tblContracheques;
    @FXML private TableColumn colMesAno;
    @FXML private TableColumn colSalarioBruto;
    @FXML private TableColumn colDescontos;
    @FXML private TableColumn colSalarioLiquido;
    @FXML private TableColumn colAcoesContracheque;
    @FXML private VBox beneficiosContainer;
    @FXML private Label lblVT;
    @FXML private Label lblVA;
    @FXML private Label lblPlanoSaude;
    @FXML private Label lblOutrosBeneficios;
    @FXML private LineChart chartHistorico;

    Usuario usuarioLogado = UserSession.getInstance().getUsuarioLogado();
    private static final String FOTO_PADRAO = "src/main/resources/fotos_perfil/default_avatar.png";
    private ResourceBundle bundle;

    public void initialize(){
        this.bundle = UserSession.getInstance().getBundle();

        carregarFotoPerfil();
        lblTitulo.setText(bundle.getString("userFinancial.title") + " " + usuarioLogado.getNome());
        lblNome.setText(usuarioLogado.getNome());
        Funcionario funcionarioLogado = (Funcionario)usuarioLogado;
        lblMatricula.setText(String.valueOf(funcionarioLogado.getMatricula()));
        lblCargo.setText(funcionarioLogado.getCargo());
        lblRegime.setText(String.valueOf(funcionarioLogado.getRegime()));
    }
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