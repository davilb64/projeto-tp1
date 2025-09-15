package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class UsuariosController {

    @FXML
    private Label tituloLabel;

    @FXML
    private Button cadastrarButton;

    @FXML
    private Button listarButton;

    // Método de inicialização (chamado automaticamente ao carregar o FXML)
    @FXML
    public void initialize() {
        tituloLabel.setText("Gestão de Usuários");
    }

    @FXML
    private void cadastrarUsuario() {
        System.out.println("👉 Botão de cadastro clicado! Aqui você pode abrir um formulário de cadastro.");
    }

    @FXML
    private void listarUsuarios() {
        System.out.println("👉 Botão de listar clicado! Aqui você pode carregar uma tabela de usuários.");
    }
}
