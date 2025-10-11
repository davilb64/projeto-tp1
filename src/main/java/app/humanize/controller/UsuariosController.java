package app.humanize.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class UsuariosController {

    public Button abrirCadastroUsuario;
    @FXML
    private Label tituloLabel;

    @FXML
    public void initialize() {
        tituloLabel.setText("Gestão de Usuários");
    }

    @FXML
    private void cadastrarUsuario() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CadastroUsuarioAdm.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Cadastrar Usuário");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL); // trava a tela até fechar
        stage.showAndWait(); // aguarda fechar para voltar
    }


}
