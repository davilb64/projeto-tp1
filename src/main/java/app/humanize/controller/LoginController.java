package app.humanize.controller;

import app.humanize.model.Perfil;
import app.humanize.model.Usuario;
import app.humanize.repository.UsuarioRepository;
import app.humanize.util.ScreenController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.util.Optional;

public class LoginController {
    @FXML
    private TextField txtUsuario;
    @FXML
    private TextField txtSenha;

    @FXML
    private void entrar(){
        String usuario = txtUsuario.getText();
        String senha = txtSenha.getText();
        if(usuario.isEmpty() || senha.isEmpty()){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Campo Vazio");
            alert.setHeaderText("Usuário / senha vazio");
            alert.setContentText("Informe um usuário e senha.");
            alert.showAndWait();
        }
        else{
            UsuarioRepository usuarioRepository = new UsuarioRepository();
            Optional<Usuario> usuarioBusca = usuarioRepository.buscaUsuarioPorLogin(usuario);
            if(usuarioBusca.isPresent()){
                if (usuarioBusca.get().getSenha().equals(senha)) {
                    if(usuarioBusca.get().getPerfil().equals(Perfil.ADMINISTRADOR)){
                        entrarAdm();
                    }
                    else if(usuarioBusca.get().getPerfil().equals(Perfil.GESTOR)){
                        entrarGestor();
                    }
                    else if(usuarioBusca.get().getPerfil().equals(Perfil.RECRUTADOR)){
                        entrarRecrutador();
                    }
                    else if(usuarioBusca.get().getPerfil().equals(Perfil.FUNCIONARIO)){
                        entrarFuncionario();
                    }
                }
                else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Senha incorreta");
                    alert.setHeaderText("Sua senha não corresponde ao usuário informado.");
                    alert.setContentText("Tente novamente ou entre em contato com seu superior");
                    alert.showAndWait();
                }
            }
            else{
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Usuário não encontrado");
                alert.setHeaderText("Seu usuário não foi encontrado");
                alert.setContentText("Tente novamente ou entre em contato com seu superior");
                alert.showAndWait();
            }

        }
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
