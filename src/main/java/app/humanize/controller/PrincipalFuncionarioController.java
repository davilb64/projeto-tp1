package app.humanize.controller;

import app.humanize.model.Funcionario;
import app.humanize.model.Usuario;
import app.humanize.util.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class PrincipalFuncionarioController {
    public BorderPane root;
    private Image avatarPadrao;

    @FXML private Button btnFinanceiro;
    @FXML private Button btnConfig;
    @FXML private ImageView fotoPerfil;

    @FXML
    private StackPane contentArea;

    private Button activeButton;

    private ResourceBundle bundle;

    @FXML private void initialize(){
        this.bundle = UserSession.getInstance().getBundle();
        atualizarTextosSidebar();

        carregarFotoPerfil();
        showFinanceiro();
    }

    private void atualizarTextosSidebar() {
        btnFinanceiro.setText(bundle.getString("sidebar.profile"));
        btnConfig.setText(bundle.getString("sidebar.settings"));
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

    private void loadUI(String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            System.out.println(bundle.getString("log.info.fxmlLoading") + resource);

            if (resource == null) {
                throw new IllegalStateException(bundle.getString("exception.fxmlNotFound.generic") + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resource, bundle);
            Node view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ConfiguracoesAdmController) {
                ((ConfiguracoesAdmController) controller).setMainController(this);
            }

            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            System.err.println(bundle.getString("log.error.fxmlLoad.io") + fxmlPath);
            e.printStackTrace();
            mostrarAlerta(
                    bundle.getString("alert.error.reload.title"),
                    bundle.getString("alert.error.reload.header"),
                    e.getMessage()
            );
        } catch (NullPointerException | IllegalStateException e) {
            System.err.println(bundle.getString("log.error.fxmlNotFound") + fxmlPath);
            e.printStackTrace();
            mostrarAlerta(
                    bundle.getString("alert.error.reload.title"),
                    bundle.getString("alert.error.fxmlNotFound.header"),
                    bundle.getString("alert.error.fxmlNotFound.content.path") + fxmlPath
            );
        } catch (Exception e) {
            System.err.println(bundle.getString("log.error.fxmlLoad.unexpected") + fxmlPath);
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
    private void showFinanceiro() {
        loadUI("/view/FinanceiroUsuario.fxml");
        setActiveButton(btnFinanceiro);
    }


    @FXML
    void showConfiguracoes() {
        loadUI("/view/ConfiguracoesAdm.fxml");
        setActiveButton(btnConfig);

        this.bundle = UserSession.getInstance().getBundle();
        atualizarTextosSidebar();
    }

    @FXML
    private void sair() {
        ScreenController.changeScene("/view/LoginView.fxml");
    }
}