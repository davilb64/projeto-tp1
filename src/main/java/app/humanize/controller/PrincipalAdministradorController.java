package app.humanize.controller;

import app.humanize.model.Funcionario;
import app.humanize.model.Usuario;
import app.humanize.util.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import app.humanize.util.ScreenController;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class PrincipalAdministradorController {

    public BorderPane root;
    @FXML private ImageView fotoPerfil;

    @FXML
    private StackPane contentArea;

    @FXML private Button btnDashboard;

    @FXML private Button btnUsuarios;
    @FXML private Button btnRelatorios;
    @FXML private Button btnConfig;
    @FXML private Button btnFinanceiro;
    @FXML private Button btnRecrutadores;
    @FXML private Button btnCandidatos;
    @FXML private Button btnVagas;
    @FXML private Button btnEntrevistas;
    @FXML private Button btnFuncionarios;
    @FXML private Button btnContratacoes;
    @FXML private Button btnPerfil;

    private Button activeButton;
    private static final String FOTO_PADRAO = "src/main/resources/fotos_perfil/default_avatar.png";

    @FXML
    public void initialize() {
        carregarFotoPerfil();

        if (btnDashboard != null) {
            btnDashboard.getStyleClass().add("buttonLateral-active");
            activeButton = btnDashboard;
        }
        showDashboard();
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
            System.err.println("Arquivo de foto não encontrado: " + (caminhoFoto != null ? caminhoFoto : FOTO_PADRAO));
            try {
                fotoPerfil.setImage(new Image(new FileInputStream(FOTO_PADRAO)));
            } catch (FileNotFoundException ex) {
                System.err.println("Foto padrão também não encontrada em: " + FOTO_PADRAO);
            }
        }
    }


    private boolean isDarkThemeActive() {
        if (root != null && root.getStyleClass().contains("dark")) {
            return true;
        }
        return contentArea != null && contentArea.getScene() != null && contentArea.getScene().getRoot().getStyleClass().contains("dark");
    }

    private void applyCurrentTheme(Node node) {
        if (isDarkThemeActive()) {
            node.getStyleClass().add("dark");
        } else {
            node.getStyleClass().remove("dark");
        }
    }

    private void loadUI(String fxml) {
        try {
            URL resource = getClass().getResource("/view/" + fxml + ".fxml");
            System.out.println(">> Tentando carregar: " + resource);

            if (resource == null) {
                throw new IllegalStateException("FXML não encontrado: " + fxml);
            }

            Node view = FXMLLoader.load(resource);

            applyCurrentTheme(view);

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Erro de IO ao carregar FXML: " + fxml);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erro inesperado ao carregar UI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("buttonLateral-active");
        }
        button.getStyleClass().add("buttonLateral-active");
        activeButton = button;
    }

    @FXML
    private void showDashboard(){
        loadUI("DashboardAdm");
        setActiveButton(btnDashboard);
    }


    @FXML
    public void showVagas() {
        loadUI("Vagas");
        setActiveButton(btnVagas);
    }

    @FXML
    public void showUsuarios() {
        loadUI("UsuariosAdm");
        setActiveButton(btnUsuarios);
    }

    @FXML
    public void showPerfil() {
        loadUI("FinanceiroUsuario");
        setActiveButton(btnPerfil);
    }

    @FXML
    void showRelatorios() {
        loadUI("RelatoriosAdm");
        setActiveButton(btnRelatorios);
    }

    @FXML
    private void showConfiguracoes() {
        loadUI("ConfiguracoesAdm");
        setActiveButton(btnConfig);
    }

    @FXML
    private void showFinanceiro() {
        loadUI("MenuFinanceiroAdm");
        setActiveButton(btnFinanceiro);
    }

    @FXML
    private void showContratacoes() {
        loadUI("ContratacoesRecrutador");
        setActiveButton(btnContratacoes);
    }

    @FXML
    private void showRecrutadores() {
        loadUI("AtribuirRecrutadorAVaga");
        setActiveButton(btnRecrutadores);
    }

    @FXML
    private void showEntrevistas() {
        loadUI("GestaoEntrevista");
        setActiveButton(btnEntrevistas);
    }

    @FXML
    private void showCandidatos() {
        loadUI("CandidatosAdm");
        setActiveButton(btnCandidatos);
    }

    @FXML
    private void showFuncionarios() {
        loadUI("Funcionario");
        setActiveButton(btnFuncionarios);
    }

    @FXML
    private void sair() {
        ScreenController.changeScene("/view/LoginView.fxml");
    }
}