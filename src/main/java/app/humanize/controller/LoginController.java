package app.humanize.controller;

import app.humanize.exceptions.SenhaIncorretaException;
import app.humanize.exceptions.UsuarioNaoEncontradoException;
import app.humanize.exceptions.ValidacaoException;
import app.humanize.model.Funcionario;
import app.humanize.model.Usuario;
import app.humanize.repository.UsuarioRepository;
import app.humanize.service.LoginService;
import app.humanize.util.ScreenController;
import app.humanize.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginController {
    @FXML
    private TextField txtUsuario;
    @FXML private PasswordField txtSenhaOculta;
    @FXML private TextField txtSenhaAberta;
    @FXML private ToggleButton btnMostrarSenha;

    @FXML
    private ImageView imgFotoPerfil;

    private final LoginService loginService = new LoginService();
    private final UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();

    private Image avatarPadrao;
    private ResourceBundle bundle;

    /**
     * Retorna o caminho absoluto para a pasta de fotos do aplicativo (fora do JAR).
     */
    private Path getPathParaFotos() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, ".humanize-app-data", "fotos_perfil");
    }

    /**
     * Copia as fotos padrão do JAR para a pasta externa (semeadura).
     */
    private void seedDefaultPhotos() {
        Path externalPhotoDir = getPathParaFotos();
        if (!Files.exists(externalPhotoDir)) {
            try {
                Files.createDirectories(externalPhotoDir);
            } catch (IOException e) {
                System.err.println("Falha ao criar diretório de fotos no Login: " + e.getMessage());
                return; // Não pode semear se a pasta falhar
            }
        }

        List<String> defaultPhotos = List.of(
                "2.png", "3.png", "4.png", "5.png", "6.png", "7.png", "8.png",
                "9.png", "10.png", "12.png", "04935825170.png",
                "08650999107.png", "default_avatar.png"
        );

        for (String photoName : defaultPhotos) {
            File externalFile = externalPhotoDir.resolve(photoName).toFile();
            if (!externalFile.exists()) {
                String resourcePath = "/fotos_perfil/" + photoName;
                try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
                    if (is == null) {
                        System.err.println("Foto de semeadura não encontrada no JAR: " + resourcePath);
                        continue;
                    }
                    Files.copy(is, externalFile.toPath());
                } catch (IOException e) {
                    System.err.println("Falha ao semear foto: " + photoName + " - " + e.getMessage());
                }
            }
        }
    }

    @FXML
    public void initialize() {
        this.bundle = UserSession.getInstance().getBundle();

        // Senha
        txtSenhaAberta.textProperty().bindBidirectional(txtSenhaOculta.textProperty());
        txtSenhaAberta.visibleProperty().bind(btnMostrarSenha.selectedProperty());
        txtSenhaOculta.visibleProperty().bind(btnMostrarSenha.selectedProperty().not());
        UserSession.getInstance().logout();

        // Garante que a pasta de fotos e as fotos padrão existam
        seedDefaultPhotos();

        // Foto Padrão
        try (InputStream is = getClass().getResourceAsStream("/fotos_perfil/default_avatar.png")) {
            if (is == null) throw new FileNotFoundException("Avatar padrão não encontrado nos resources.");
            avatarPadrao = new Image(is);
        } catch (Exception e) {
            System.err.println(bundle.getString("log.error.avatarDefaultNotFound"));
        }
        imgFotoPerfil.setImage(avatarPadrao);

        // Listener para atualizar a foto
        txtUsuario.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                imgFotoPerfil.setImage(avatarPadrao);
            } else {
                Optional<Usuario> usuarioOpt = usuarioRepository.buscaUsuarioPorLogin(newVal.trim());
                Image foto = usuarioOpt.map(usuario -> {
                    String caminho = null;
                    if (usuario instanceof Funcionario) {
                        caminho = ((Funcionario) usuario).getCaminhoFoto();
                    }
                    return carregarImagem(caminho);
                }).orElse(avatarPadrao);
                imgFotoPerfil.setImage(foto);
            }
        });
    }

    /**
     * Tenta carregar uma imagem do caminho EXTERNO.
     * Se falhar ou o caminho for vazio, retorna o avatar padrão.
     */
    private Image carregarImagem(String caminho) {
        if (caminho == null || caminho.isEmpty()) {
            return avatarPadrao; // Caminho vazio, usa padrão
        }
        try {
            // Tenta carregar do caminho absoluto externo
            return new Image(new FileInputStream(caminho));
        } catch (FileNotFoundException e) {
            System.err.println(bundle.getString("log.error.loginPhotoNotFound") + caminho);
            return avatarPadrao; // Arquivo não encontrado, usa padrão
        }
    }


    @FXML
    private void entrar(){
        String usuario = txtUsuario.getText();
        String senha = txtSenhaOculta.getText();
        try {
            Usuario usuarioAutenticado = loginService.autenticar(usuario, senha);
            UserSession.getInstance().login(usuarioAutenticado);
            switch (usuarioAutenticado.getPerfil()) {
                case ADMINISTRADOR:
                    entrarAdm();
                    break;

                case GESTOR:
                    entrarGestor();
                    break;

                case RECRUTADOR:
                    entrarRecrutador();
                    break;

                case FUNCIONARIO:
                    entrarFuncionario();
                    break;
            }
        }catch (ValidacaoException e) {
            mostrarAlerta(
                    bundle.getString("login.alert.emptyFields.title"),
                    bundle.getString("login.alert.emptyFields.header"),
                    bundle.getString("login.alert.emptyFields.content")
            );
        }
        catch (UsuarioNaoEncontradoException e) {
            mostrarAlerta(
                    bundle.getString("login.alert.userNotFound.title"),
                    bundle.getString("login.alert.userNotFound.header"),
                    bundle.getString("login.alert.userNotFound.content")
            );
        }
        catch (SenhaIncorretaException e) {
            mostrarAlerta(
                    bundle.getString("login.alert.wrongPassword.title"),
                    bundle.getString("login.alert.wrongPassword.header"),
                    bundle.getString("login.alert.wrongPassword.content")
            );
        }
        catch (Exception e) {
            mostrarAlerta(
                    bundle.getString("login.alert.unexpectedError.title"),
                    bundle.getString("login.alert.unexpectedError.header"),
                    e.getMessage()
            );
        }
    }

    private void mostrarAlerta(String titulo, String cabecalho, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(conteudo);
        alert.showAndWait();
    }

    @FXML
    private void entrarAdm() {
        ScreenController.changeScene("/view/telaPrincipalAdministrador.fxml");
    }
    @FXML
    private void entrarGestor() {
        ScreenController.changeScene("/view/TelaPrincipalGestor.fxml");
    }
    @FXML
    private void entrarRecrutador() {
        ScreenController.changeScene("/view/TelaPrincipalRecrutador.fxml");
    }
    @FXML
    private void entrarFuncionario() {
        ScreenController.changeScene("/view/TelaPrincipalFuncionario.fxml");
    }
}