package app.humanize.controller;

import app.humanize.exceptions.CpfInvalidoException;
import app.humanize.exceptions.SenhaInvalidaException;
import app.humanize.model.Endereco;
import app.humanize.model.Perfil;
import app.humanize.model.Usuario;
import app.humanize.model.factories.UsuarioFactory;
import app.humanize.repository.UsuarioRepository;
import app.humanize.service.ValidaCpf;
import app.humanize.service.ValidaSenha;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.Optional;

public class CadastroUsuarioAdmController {

    @FXML private Label lblId;
    @FXML private TextField txtNome;
    @FXML private TextField txtEmail;
    @FXML private TextField txtCpf;
    @FXML private TextField txtLogin;
    @FXML private PasswordField txtSenhaOculta;
    @FXML private TextField txtSenhaVisivel;
    @FXML private ToggleButton btnMostrarSenha;
    @FXML private ComboBox<Perfil> perfilCombo;
    @FXML private Label lblEndereco;

    private Endereco enderecoDoOutroController;
    private final UsuarioFactory usuarioFactory = new UsuarioFactory();
    private final UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();
    private final ValidaCpf validaCpf = new ValidaCpf();
    private final ValidaSenha validaSenha = new ValidaSenha();

    private Usuario usuarioParaEditar;

    @FXML
    public void initialize() {
        if (usuarioParaEditar == null) {
            lblId.setText(String.valueOf(usuarioRepository.getProximoId()));
        }
        perfilCombo.getItems().setAll(Perfil.values());
        txtSenhaVisivel.textProperty().bindBidirectional(txtSenhaOculta.textProperty());
        txtSenhaVisivel.visibleProperty().bind(btnMostrarSenha.selectedProperty());
        txtSenhaOculta.visibleProperty().bind(btnMostrarSenha.selectedProperty().not());
    }

    public void prepararParaEdicao(Usuario usuario) {
        this.usuarioParaEditar = usuario;

        lblId.setText(String.valueOf(usuario.getId())); // Define o ID existente
        txtNome.setText(usuario.getNome());
        txtEmail.setText(usuario.getEmail());
        txtCpf.setText(usuario.getCpf());
        txtLogin.setText(usuario.getLogin());
        perfilCombo.setValue(usuario.getPerfil()); // Define o perfil no ComboBox

        this.enderecoDoOutroController = usuario.getEndereco();
        if (this.enderecoDoOutroController != null) {
            lblEndereco.setText(enderecoDoOutroController.enderecoReduzido());
        }

        txtSenhaOculta.setPromptText("Digite apenas se desejar alterar a senha");
        txtSenhaVisivel.setPromptText("Digite apenas se desejar alterar a senha");
    }

    @FXML
    private void cadastrarEndereco() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CadastroEndereco.fxml"));
            Parent root = loader.load();
            CadastroEnderecoController enderecoController = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Cadastro de Endereço");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(txtNome.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            this.enderecoDoOutroController = enderecoController.getEnderecoSalvo();
            if (this.enderecoDoOutroController != null) {
                lblEndereco.setText(enderecoDoOutroController.enderecoReduzido());
            }
        } catch (IOException e) {
            mostrarAlerta("Erro Crítico", "Não foi possível carregar a tela de endereço. Verifique se o arquivo FXML está no local correto: /app/humanize/view/CadastroEndereco.fxml",null);
        }
    }

    private boolean validarCampos() {
        if (txtNome.getText().isBlank() || txtCpf.getText().isBlank() || txtLogin.getText().isBlank()) {
            mostrarAlerta("Campos Obrigatórios", "Os campos Nome, CPF e Login devem ser preenchidos.",null);
            return false;
        }

        if (usuarioParaEditar == null && txtSenhaOculta.getText().isBlank()) {
            mostrarAlerta("Campos Obrigatórios", "O campo Senha deve ser preenchido para novos usuários.",null);
            return false;
        }

        if (perfilCombo.getSelectionModel().isEmpty()) {
            mostrarAlerta("Seleção Obrigatória", "Por favor, selecione um perfil para o usuário.",null);
            return false;
        }
        if (enderecoDoOutroController == null) {
            mostrarAlerta("Endereço Obrigatório", "Por favor, cadastre um endereço para o usuário.",null);
            return false;
        }

        Optional<Usuario> usuarioComEsteLogin = usuarioRepository.buscaUsuarioPorLogin(txtLogin.getText());
        if (usuarioComEsteLogin.isPresent()) {
            if (usuarioParaEditar == null || usuarioParaEditar.getId() != usuarioComEsteLogin.get().getId()) {
                mostrarAlerta("Login Inválido", "Este login já está em uso por outro usuário.",null);
                return false;
            }
        }

        return true;
    }

    @FXML
    private void salvarUsuario() {
        if (!validarCampos()) {
            return;
        }

        String senha = txtSenhaOculta.getText();
        String hash;

        if (usuarioParaEditar != null && senha.isBlank()) {
            hash = usuarioParaEditar.getSenha();
        } else {
            hash = BCrypt.hashpw(senha, BCrypt.gensalt());
        }

        if (usuarioParaEditar == null) {

            try{
                validaCpf.validaCpf(txtCpf.getText());
                validaSenha.validaSenha(txtSenhaOculta.getText());
                Usuario usuario = usuarioFactory.createUsuario(txtNome.getText(), txtCpf.getText(), enderecoDoOutroController,
                        txtEmail.getText(), txtLogin.getText(), hash, perfilCombo.getValue());
                usuarioRepository.escreveUsuarioNovo(usuario);
            }catch (CpfInvalidoException e){
                mostrarAlerta("CPF Inválido", "CPF não atende aos critérios de existência!", e.getMessage());
                return;
            }catch (SenhaInvalidaException e){
                mostrarAlerta("Senha Inválida", "A senha não atende aos critérios de existência!", e.getMessage());
                return;
            }catch (Exception e){
                mostrarAlerta("Erro inesperado","Tente novamente", e.getMessage());
            }

        } else {
            try{
                usuarioParaEditar.setNome(txtNome.getText());
                usuarioParaEditar.setCpf(txtCpf.getText());
                usuarioParaEditar.setEmail(txtEmail.getText());
                usuarioParaEditar.setLogin(txtLogin.getText());
                usuarioParaEditar.setPerfil(perfilCombo.getValue());
                usuarioParaEditar.setEndereco(enderecoDoOutroController);
                usuarioParaEditar.setSenha(hash);
                usuarioRepository.atualizarUsuario(usuarioParaEditar);
            }catch (CpfInvalidoException e){
                mostrarAlerta("CPF Inválido", "CPF não atende aos critérios de existência!", e.getMessage());
                return;
            }catch (SenhaInvalidaException e){
                mostrarAlerta("Senha Inválida", "A senha não atende aos critérios de existência!", e.getMessage());
                return;
            }catch (Exception e){
                mostrarAlerta("Erro inesperado","Tente novamente", e.getMessage());
            }

        }
        fecharJanela();
    }


    private void mostrarAlerta(String titulo, String mensagem, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(titulo);
        alert.setContentText(mensagem);
        alert.setContentText(conteudo);
        alert.showAndWait();
    }

    @FXML
    private void fecharJanela() {
        Stage stage = (Stage) txtNome.getScene().getWindow();
        stage.close();
    }
}