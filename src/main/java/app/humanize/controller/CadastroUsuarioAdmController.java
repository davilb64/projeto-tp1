package app.humanize.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class CadastroUsuarioAdmController {

    public PasswordField senhaField;
    @FXML
    private TextField nomeField;

    @FXML
    private TextField emailField;

    @FXML
    private ComboBox<String> perfilCombo;

    @FXML
    public void initialize() {
        perfilCombo.getItems().addAll("Administrador", "Usuário Comum", "Gerente");
    }

    @FXML
    private void salvarUsuario() {
        String nome = nomeField.getText();
        String email = emailField.getText();
        String perfil = perfilCombo.getValue();

        System.out.println("Usuário salvo: " + nome + " - " + email + " - " + perfil);

        fecharJanela();
    }

    @FXML
    private void fecharJanela() {
        Stage stage = (Stage) nomeField.getScene().getWindow();
        stage.close();
    }
}
