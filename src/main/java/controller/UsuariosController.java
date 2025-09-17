package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class UsuariosController {

    public Button cadastrarButton;
    @FXML
    private Label tituloLabel;

    // Método de inicialização (chamado automaticamente ao carregar o FXML)
    @FXML
    public void initialize() {
        tituloLabel.setText("Gestão de Usuários");
    }

    @FXML
    private void cadastrarUsuario() {
        System.out.println("👉 Botão de cadastro clicado! Aqui você pode abrir um formulário de cadastro.");
    }

}
