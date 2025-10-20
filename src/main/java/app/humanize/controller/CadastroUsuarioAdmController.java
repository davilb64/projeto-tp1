package app.humanize.controller;

import app.humanize.model.Endereco;
import app.humanize.model.Perfil;
import app.humanize.model.Usuario;
import app.humanize.model.factories.UsuarioFactory;
import app.humanize.repository.UsuarioRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

public class CadastroUsuarioAdmController {

    @FXML
    private Label lblId;
    @FXML
    private TextField txtNome;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtCpf;
    @FXML
    private TextField txtLogin;
    @FXML
    private PasswordField txtSenhaOculta;
    @FXML
    private TextField txtSenhaVisivel;
    @FXML
    private ToggleButton btnMostrarSenha;
    @FXML
    private ComboBox<Perfil> perfilCombo;

    private final UsuarioFactory usuarioFactory = new UsuarioFactory();
    private final UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();

    @FXML
    public void initialize() {
        lblId.setText(String.valueOf(usuarioRepository.getProximoId()));
        perfilCombo.getItems().setAll(Perfil.values());

        txtSenhaVisivel.textProperty().bindBidirectional(txtSenhaOculta.textProperty());
        txtSenhaVisivel.visibleProperty().bind(btnMostrarSenha.selectedProperty());
        txtSenhaOculta.visibleProperty().bind(btnMostrarSenha.selectedProperty().not());
    }

    @FXML
    private void salvarUsuario() throws IOException {
        Perfil perfil = perfilCombo.getSelectionModel().getSelectedItem();

        if (perfil == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Por favor, selecione um perfil para o usuário.");
            alert.setHeaderText("Campo Obrigatório");
            alert.showAndWait();
            return;
        }

        String senha = txtSenhaOculta.getText();
        String hash = BCrypt.hashpw(senha, BCrypt.gensalt());

        Endereco endereco = new Endereco.EnderecoBuilder()
                .logradouro("Rua das Flores").numero(123).bairro("Centro")
                .cidade("São Paulo").estado("SP").cep("01000-000").build();

        Usuario usuario = usuarioFactory.createUsuario(txtNome.getText(), txtCpf.getText(), endereco, txtEmail.getText(), txtLogin.getText(), hash, perfil);

        usuarioRepository.escreveUsuarioNovo(usuario);
        fecharJanela();
    }

    @FXML
    private void fecharJanela() {
        Stage stage = (Stage) txtNome.getScene().getWindow();
        stage.close();
    }
}