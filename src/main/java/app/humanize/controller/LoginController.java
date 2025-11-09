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
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Optional;

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
    private static final String DIRETORIO_FOTOS = "src/main/resources/fotos_perfil/";


    @FXML
    public void initialize() {
        // senha
        txtSenhaAberta.textProperty().bindBidirectional(txtSenhaOculta.textProperty());
        txtSenhaAberta.visibleProperty().bind(btnMostrarSenha.selectedProperty());
        txtSenhaOculta.visibleProperty().bind(btnMostrarSenha.selectedProperty().not());
        UserSession.getInstance().logout();

        // foto

        try {
            avatarPadrao = new Image(new FileInputStream(DIRETORIO_FOTOS + "default_avatar.png"));
        } catch (FileNotFoundException e) {
            System.err.println("Avatar padrão não encontrado!");
            avatarPadrao = null;
        }
        imgFotoPerfil.setImage(avatarPadrao); // imagem inicial

        // listener para atualizar a foto
        txtUsuario.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                // = vazio, reseta para o padrão
                imgFotoPerfil.setImage(avatarPadrao);
            } else {
                // busca pelo login (novoVal)
                Optional<Usuario> usuarioOpt = usuarioRepository.buscaUsuarioPorLogin(newVal.trim());

                Image foto = usuarioOpt.map(usuario -> {
                    String caminho = null;
                    if (usuario instanceof Funcionario) {
                        // cast
                        caminho = ((Funcionario) usuario).getCaminhoFoto();

                    }
                    return carregarImagem(caminho);
                }).orElse(avatarPadrao);

                imgFotoPerfil.setImage(foto);
            }
        });
    }

    /**
     * Tenta carregar uma imagem do caminho. Se falhar, retorna o avatar padrão.
     */
    private Image carregarImagem(String caminho) {
        if (caminho == null || caminho.isEmpty()) {
            return avatarPadrao;
        }
        try {
            return new Image(new FileInputStream(caminho));
        } catch (FileNotFoundException e) {
            System.err.println("Foto de login não encontrada: " + caminho);
            return avatarPadrao;
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
            mostrarAlerta("Campo Vazio", "Usuário / senha vazio", "Informe um usuário e senha.");
        }
        catch (UsuarioNaoEncontradoException e) {
            mostrarAlerta("Usuário não encontrado", "Seu usuário não foi encontrado", "Tente novamente ou entre em contato com seu superior");
        }
        catch (SenhaIncorretaException e) {
            mostrarAlerta("Senha incorreta", "Sua senha não corresponde ao usuário informado.", "Tente novamente ou entre em contato com seu superior");
        }
        catch (Exception e) {
            mostrarAlerta("Erro Inesperado", "Ocorreu um erro", e.getMessage());
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