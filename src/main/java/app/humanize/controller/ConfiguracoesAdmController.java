package app.humanize.controller;

import app.humanize.exceptions.SenhaInvalidaException;
import app.humanize.model.Usuario;
import app.humanize.repository.UsuarioRepository;
import app.humanize.service.validacoes.ValidaSenha;
import app.humanize.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox; // Importe VBox
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

public class ConfiguracoesAdmController {
    @FXML private ComboBox<String> idiomaCombo;
    @FXML private TextField txtNome;
    @FXML private TextField txtEmail;
    @FXML private TextField txtSenha;
    @FXML private ToggleButton btnAlterarSenha;

    @FXML private VBox rootVBox;

    private final UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();
    private final ValidaSenha validaSenha = new ValidaSenha();
    Usuario usuarioLogado = UserSession.getInstance().getUsuarioLogado();

    @FXML
    public void initialize() {
        idiomaCombo.getItems().addAll("Português", "Inglês", "Espanhol");
        idiomaCombo.setValue("Português");
        txtSenha.setEditable(false);
        txtSenha.setDisable(true);
        carregaDadosUser();
    }

    private void carregaDadosUser() {
        txtNome.setText(usuarioLogado.getNome());
        txtEmail.setText(usuarioLogado.getEmail());
        txtSenha.setText("**********");
    }

    @FXML
    private void liberaAlterarSenha() {
        if (btnAlterarSenha.isSelected()) {
            txtSenha.setEditable(true);
            txtSenha.setDisable(false);
            txtSenha.clear();
            txtSenha.setPromptText("Digite a nova senha");
            txtSenha.requestFocus();
            btnAlterarSenha.setText("Salvar");

        } else {
            String novaSenha = txtSenha.getText();

            try {
                validaSenha.validaSenha(novaSenha);
                salvarNovaSenha(novaSenha);

                txtSenha.setEditable(false);
                txtSenha.setDisable(true);
                txtSenha.setText("**********");
                btnAlterarSenha.setText("Alterar Senha");

            } catch (SenhaInvalidaException e) {
                mostrarAlerta("Senha Inválida", "A senha não atende aos critérios de existência!", e.getMessage());
                btnAlterarSenha.setSelected(true);

            } catch (Exception e) {
                mostrarAlerta("Erro inesperado", "Tente novamente", e.getMessage());
                btnAlterarSenha.setSelected(true);
            }
        }
    }

    private void salvarNovaSenha(String novaSenha) throws IOException {
        String hashNovaSenha = BCrypt.hashpw(novaSenha, BCrypt.gensalt());
        usuarioLogado.setSenha(hashNovaSenha);
        usuarioRepository.atualizarUsuario(usuarioLogado);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText("Senha alterada com sucesso!");
        alert.showAndWait();
    }

    private void mostrarAlerta(String titulo, String cabecalho, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(conteudo);
        alert.showAndWait();
    }

    @FXML
    private void restaurarPadroes() {
        // Implementar lógica de restauração
        // Ex: idiomaCombo.setValue("Português");
        // Ex: radioClaro.setSelected(true);
    }

    @FXML
    private void salvarAlteracoes() {
        // Implementar lógica de salvamento de idioma/fuso/notificações
        // Ex: usuarioRepository.salvarPreferencia(usuarioLogado.getId(), idiomaCombo.getValue());
    }

    @FXML
    private void fecharJanela() {
        Stage stage = (Stage) txtNome.getScene().getWindow();
        stage.close();
    }
}