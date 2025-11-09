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
import java.net.URL;
import java.util.ResourceBundle;

public class PrincipalFuncionarioController {
    public BorderPane root;

    @FXML private Button btnFinanceiro;
    @FXML private Button btnConfig;
    @FXML private ImageView fotoPerfil;

    @FXML
    private StackPane contentArea;

    private Button activeButton;

    private static final String FOTO_PADRAO = "src/main/resources/fotos_perfil/default_avatar.png";

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

    private void loadUI(String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            System.out.println(">> Tentando carregar: " + resource);

            if (resource == null) {
                throw new IllegalStateException("FXML não encontrado: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resource, bundle);
            Node view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ConfiguracoesAdmController) {
                ((ConfiguracoesAdmController) controller).setMainController(this);
            }

            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            System.err.println("Erro de IO ao carregar FXML: " + fxmlPath);
            e.printStackTrace();
            mostrarAlerta(
                    bundle.getString("alert.error.reload.title"),
                    bundle.getString("alert.error.reload.header"),
                    e.getMessage()
            );
        } catch (NullPointerException e) {
            System.err.println("Erro: Recurso FXML não encontrado: " + fxmlPath);
            e.printStackTrace();
            mostrarAlerta(
                    bundle.getString("alert.error.reload.title"),
                    bundle.getString("alert.error.fxmlNotFound.header"),
                    "Caminho: " + fxmlPath
            );
        } catch (Exception e) {
            System.err.println("Erro inesperado ao carregar FXML: " + fxmlPath);
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