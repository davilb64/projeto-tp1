package app.humanize.controller;

import app.humanize.exceptions.SenhaIncorretaException;
import app.humanize.exceptions.UsuarioNaoEncontradoException;
import app.humanize.exceptions.ValidacaoException;
import app.humanize.model.Usuario;
import app.humanize.service.LoginService;
import app.humanize.util.ScreenController;
import app.humanize.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {
    @FXML
    private TextField txtUsuario;
    @FXML private PasswordField txtSenhaOculta;
    @FXML private TextField txtSenhaAberta;
    @FXML private ToggleButton btnMostrarSenha;

    private final LoginService loginService = new LoginService();


    @FXML
    public void initialize() {
        txtSenhaAberta.textProperty().bindBidirectional(txtSenhaOculta.textProperty());
        txtSenhaAberta.visibleProperty().bind(btnMostrarSenha.selectedProperty());
        txtSenhaOculta.visibleProperty().bind(btnMostrarSenha.selectedProperty().not());
        UserSession.getInstance().logout();
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
            // Um erro genérico caso algo inesperado aconteça
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
