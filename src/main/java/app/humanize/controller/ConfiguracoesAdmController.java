package app.humanize.controller;

import app.humanize.exceptions.SenhaInvalidaException;
import app.humanize.model.Usuario;
import app.humanize.repository.UsuarioRepository;
import app.humanize.service.validacoes.ValidaSenha;
import app.humanize.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

public class ConfiguracoesAdmController {
    public ToggleGroup tema;
    @FXML
    private ComboBox<String> idiomaCombo;
    @FXML
    private ComboBox<String> fusoCombo;
    @FXML
    private TextField txtNome;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtSenha;
    @FXML
    private ToggleButton btnAlterarSenha;

    private final UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();
    private final ValidaSenha validaSenha = new ValidaSenha();
    Usuario usuarioLogado = UserSession.getInstance().getUsuarioLogado();

    @FXML
    public void initialize() {
        idiomaCombo.getItems().addAll("Português", "Inglês", "Espanhol");
        idiomaCombo.setValue("Português");
        fusoCombo.getItems().addAll("GMT-3", "GMT-5", "UTC");
        fusoCombo.setValue("GMT-3");
        txtSenha.setEditable(false);
        txtSenha.setDisable(true);
        carregaDadosUser();
    }

    private void carregaDadosUser() {
        txtNome.setText(usuarioLogado.getNome());
        txtEmail.setText(usuarioLogado.getEmail());
        txtSenha.setText("Senha Criptografada");
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
        usuarioRepository.atualizarUsuario();

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
}
