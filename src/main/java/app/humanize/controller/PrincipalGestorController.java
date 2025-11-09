package app.humanize.controller;

import app.humanize.model.Funcionario;
import app.humanize.model.Usuario;
import app.humanize.util.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
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
import java.util.ResourceBundle;

public class PrincipalGestorController {
    public BorderPane root;

    @FXML
    private StackPane contentArea;

    @FXML private ImageView fotoPerfil;

    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnContratacoes;
    @FXML
    private Button btnRecrutadores;
    @FXML
    private Button btnRelatorios;
    @FXML
    private Button btnVagas;
    @FXML
    private Button btnFinanceiro;
    @FXML
    private Button btnConfig;
    @FXML
    private Button btnPerfil;
    @FXML
    private Button btnEntrevistas;
    @FXML
    private Button btnCandidatos;
    @FXML
    private Button btnFuncionarios;

    private Button activeButton;

    private static final String FOTO_PADRAO = "src/main/resources/fotos_perfil/default_avatar.png";

    private ResourceBundle bundle;

    @FXML
    public void initialize() {
        this.bundle = UserSession.getInstance().getBundle();
        atualizarTextosSidebar();

        carregarFotoPerfil();
        showDashboard();
    }

    private void atualizarTextosSidebar() {
        btnDashboard.setText(bundle.getString("sidebar.dashboard"));
        btnContratacoes.setText(bundle.getString("sidebar.hires"));
        btnRecrutadores.setText(bundle.getString("sidebar.recruiters"));
        btnRelatorios.setText(bundle.getString("sidebar.reports"));
        btnVagas.setText(bundle.getString("sidebar.vacancies"));
        btnFinanceiro.setText(bundle.getString("sidebar.finance"));
        btnConfig.setText(bundle.getString("sidebar.settings"));
        btnPerfil.setText(bundle.getString("sidebar.profile"));
        btnEntrevistas.setText(bundle.getString("sidebar.interviews"));
        btnCandidatos.setText(bundle.getString("sidebar.candidates"));
        btnFuncionarios.setText(bundle.getString("sidebar.employees"));
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

    private void loadUI(String fxml) {
        try {
            String fxmlPath = "/view/" + fxml + ".fxml";
            URL resource = getClass().getResource(fxmlPath);
            System.out.println(bundle.getString("log.info.fxmlLoading") + resource);

            if (resource == null) {
                throw new IllegalStateException(bundle.getString("exception.fxmlNotFound.generic") + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resource, bundle);
            Node view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof DashboardGestorController) {
                ((DashboardGestorController) controller).setMainController(this);
            }
            if (controller instanceof ConfiguracoesAdmController) {
                ((ConfiguracoesAdmController) controller).setMainController(this);
            }

            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(
                    bundle.getString("alert.error.reload.title"),
                    bundle.getString("alert.error.reload.header"),
                    e.getMessage()
            );
        } catch (Exception e) {
            System.err.println(bundle.getString("log.error.uiLoad.unexpected") + e.getMessage());
            e.printStackTrace();
            mostrarAlerta(
                    bundle.getString("alert.error.unexpected.title"),
                    bundle.getString("alert.error.unexpected.header"),
                    e.getMessage()
            );
        }
    }

    private void mostrarAlerta(String titulo, String cabecalho, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(conteudo != null ? conteudo : "");
        alert.showAndWait();
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("buttonLateral-active");
        }
        button.getStyleClass().add("buttonLateral-active");
        activeButton = button;
    }

    @FXML
    private void showDashboard() {
        loadUI("DashboardGestor");
        setActiveButton(btnDashboard);
    }

    @FXML
    public void showRecrutadores() {
        loadUI("AtribuirRecrutadorAVaga");
        setActiveButton(btnRecrutadores);
    }

    @FXML
    private void showEntrevistas() {
        loadUI("GestaoEntrevista");
        setActiveButton(btnEntrevistas);
    }

    @FXML
    private void showPerfil() {
        loadUI("FinanceiroUsuario");
        setActiveButton(btnPerfil);
    }

    @FXML
    public void showVagas() {
        loadUI("Vagas");
        setActiveButton(btnVagas);
    }

    @FXML
    private void showRelatorios() {
        loadUI("RelatoriosAdm");
        setActiveButton(btnRelatorios);
    }

    @FXML
    private void showContratacoes() {
        loadUI("ContratacoesRecrutador");
        setActiveButton(btnContratacoes);
    }

    @FXML
    private void showFinanceiro() {
        loadUI("MenuFinanceiroAdm");
        setActiveButton(btnFinanceiro);
    }

    @FXML
    void showConfig() {
        loadUI("ConfiguracoesAdm");
        setActiveButton(btnConfig);

        this.bundle = UserSession.getInstance().getBundle();
        atualizarTextosSidebar();
    }

    @FXML
    private void showCandidatos() {
        loadUI("StatusDaCandidaturaGestor");
        setActiveButton(btnCandidatos);
    }

    @FXML
    public void showFuncionarios() {
        loadUI("Funcionario");
        setActiveButton(btnFuncionarios);
    }

    @FXML
    private void sair() {
        ScreenController.changeScene("/view/LoginView.fxml");
    }
}