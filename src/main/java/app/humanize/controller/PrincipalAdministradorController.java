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
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

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
    @FXML private Button btnCandidatos;
    @FXML private Button btnVagas;
    @FXML private Button btnEntrevistas;
    @FXML private Button btnFuncionarios;
    @FXML private Button btnPerfil;
    private Image avatarPadrao;

    private Button activeButton;

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
        btnUsuarios.setText(bundle.getString("sidebar.users"));
        btnRelatorios.setText(bundle.getString("sidebar.reports"));
        btnConfig.setText(bundle.getString("sidebar.settings"));
        btnFinanceiro.setText(bundle.getString("sidebar.finance"));
        btnCandidatos.setText(bundle.getString("sidebar.candidates"));
        btnVagas.setText(bundle.getString("sidebar.vacancies"));
        btnEntrevistas.setText(bundle.getString("sidebar.interviews"));
        btnFuncionarios.setText(bundle.getString("sidebar.employees"));
        btnPerfil.setText(bundle.getString("sidebar.profile"));
    }



    private void carregarFotoPerfil() {
        Usuario usuario = UserSession.getInstance().getUsuarioLogado();
        String caminhoFoto = null;
        Image imagemParaCarregar = null;

        if (usuario instanceof Funcionario) {
            caminhoFoto = ((Funcionario) usuario).getCaminhoFoto();
        }

        try (InputStream is = getClass().getResourceAsStream("/fotos_perfil/default_avatar.png")) {
            if (is == null) throw new FileNotFoundException("Avatar padrão não encontrado nos resources.");
            this.avatarPadrao = new Image(is);
        } catch (Exception e) {
            System.err.println(bundle.getString("log.error.avatarDefaultNotFound"));
        }

        if (caminhoFoto != null && !caminhoFoto.isEmpty()) {
            try {
                imagemParaCarregar = new Image(new FileInputStream(caminhoFoto));
            } catch (FileNotFoundException e) {
                System.err.println(bundle.getString("log.error.photoNotFound") + caminhoFoto);
            }
        }

        if (imagemParaCarregar == null) {
            imagemParaCarregar = avatarPadrao;
        }

        fotoPerfil.setImage(imagemParaCarregar);
    }


    private void loadUI(String fxml) {
        try {
            ResourceBundle bundle = UserSession.getInstance().getBundle();

            String fxmlPath = "/view/" + fxml + ".fxml";
            URL resource = getClass().getResource(fxmlPath);
            System.out.println(bundle.getString("log.info.fxmlLoading") + resource);

            if (resource == null) {
                throw new IllegalStateException(bundle.getString("exception.fxmlNotFound.generic") + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resource, bundle);

            Node view = loader.load();

            Object controller = loader.getController();

            if (controller instanceof DashboardAdministradorController) {
                ((DashboardAdministradorController) controller).setMainController(this);
            }

            if (controller instanceof ConfiguracoesAdmController) {
                ((ConfiguracoesAdmController) controller).setMainController(this);
            }

            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            System.err.println(bundle.getString("log.error.fxmlLoad.io") + fxml);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println(bundle.getString("log.error.uiLoad.unexpected") + e.getMessage());
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
    void showConfiguracoes() {
        loadUI("ConfiguracoesAdm");
        setActiveButton(btnConfig);

        // Atualiza a sidebar
        this.bundle = UserSession.getInstance().getBundle();
        atualizarTextosSidebar();
    }

    @FXML
    private void showFinanceiro() {
        loadUI("MenuFinanceiroAdm");
        setActiveButton(btnFinanceiro);
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