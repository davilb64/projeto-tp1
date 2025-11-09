package app.humanize.controller;

import app.humanize.exceptions.SenhaInvalidaException;
import app.humanize.model.Usuario;
import app.humanize.repository.UsuarioRepository;
import app.humanize.service.validacoes.ValidaSenha;
import app.humanize.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ConfiguracoesAdmController {
    @FXML private ComboBox<String> idiomaCombo;
    @FXML private TextField txtNome;
    @FXML private TextField txtEmail;
    @FXML private TextField txtSenha;
    @FXML private ToggleButton btnAlterarSenha;
    @FXML private VBox rootVBox;

    private final UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();
    private final ValidaSenha validaSenha = new ValidaSenha();
    private final Usuario usuarioLogado = UserSession.getInstance().getUsuarioLogado();
    private ResourceBundle bundle;
    private PrincipalAdministradorController mainController;
    private PrincipalGestorController mainControllerGestor;
    private PrincipalRecrutadorController mainControllerRecrutador;
    private PrincipalFuncionarioController mainControllerFuncionario;

    private final Map<String, String> idiomaMap = new HashMap<>();

    public void setMainController(PrincipalAdministradorController mainController) {
        this.mainController = mainController;
    }
    public void setMainController(PrincipalGestorController mainController) {
        this.mainControllerGestor = mainController;
    }
    public void setMainController(PrincipalRecrutadorController mainController) {
        this.mainControllerRecrutador = mainController;
    }
    public void setMainController(PrincipalFuncionarioController mainController) {
        this.mainControllerFuncionario = mainController;
    }

    @FXML
    public void initialize() {
        this.bundle = UserSession.getInstance().getBundle();

        String keyPt = "profile.language.portuguese";
        String keyEn = "profile.language.english";
        String keyEs = "profile.language.spanish";

        String valPt = bundle.getString(keyPt);
        String valEn = bundle.getString(keyEn);
        String valEs = bundle.getString(keyEs);

        idiomaMap.put(valPt, keyPt);
        idiomaMap.put(valEn, keyEn);
        idiomaMap.put(valEs, keyEs);

        idiomaCombo.getItems().addAll(valPt, valEn, valEs);

        String chaveIdiomaAtual = UserSession.getInstance().getStringFromLocale();
        idiomaCombo.setValue(bundle.getString(chaveIdiomaAtual));


        txtSenha.setEditable(false);
        txtSenha.setDisable(true);
        carregaDadosUser();
        atualizarTextoBotaoSenha();
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
            txtSenha.setPromptText(bundle.getString("profile.password.prompt"));
            txtSenha.requestFocus();
            btnAlterarSenha.setText(bundle.getString("profile.savePassword"));

        } else {
            String novaSenha = txtSenha.getText();

            try {
                validaSenha.validaSenha(novaSenha);
                salvarNovaSenha(novaSenha);

                txtSenha.setEditable(false);
                txtSenha.setDisable(true);
                txtSenha.setText("**********");

                atualizarTextoBotaoSenha();

            } catch (SenhaInvalidaException e) {
                mostrarAlerta(
                        bundle.getString("alert.error.invalidPassword.title"),
                        bundle.getString("alert.error.invalidPassword.header"),
                        e.getMessage()
                );
                btnAlterarSenha.setSelected(true);

            } catch (Exception e) {
                mostrarAlerta(
                        bundle.getString("alert.error.unexpected.title"),
                        bundle.getString("alert.error.unexpected.header"),
                        e.getMessage()
                );
                btnAlterarSenha.setSelected(true);
            }
        }
    }

    private void atualizarTextoBotaoSenha() {
        if (btnAlterarSenha.isSelected()) {
            btnAlterarSenha.setText(bundle.getString("profile.savePassword"));
        } else {
            btnAlterarSenha.setText(bundle.getString("profile.changePassword"));
        }
    }

    private void salvarNovaSenha(String novaSenha) throws IOException {
        String hashNovaSenha = BCrypt.hashpw(novaSenha, BCrypt.gensalt());
        usuarioLogado.setSenha(hashNovaSenha);
        usuarioRepository.atualizarUsuario(usuarioLogado);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(bundle.getString("alert.success.title"));
        alert.setHeaderText(bundle.getString("alert.success.passwordChanged.header"));
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
        idiomaCombo.setValue(bundle.getString("profile.language.portuguese"));
        salvarAlteracoes();
    }

    @FXML
    private void salvarAlteracoes() {
        try {
            String idiomaSelecionado = idiomaCombo.getValue();
            String chaveSelecionada = idiomaMap.get(idiomaSelecionado);
            if (chaveSelecionada != null) {
                UserSession.getInstance().setLocaleFromString(chaveSelecionada);
            } else {
                UserSession.getInstance().setLocaleFromString("profile.language.portuguese");
            }
            if (mainController != null) {
                mainController.showConfiguracoes();
            } else if (mainControllerGestor != null) {
                mainControllerGestor.showConfig();
            } else if (mainControllerRecrutador != null) {
                mainControllerRecrutador.showConfiguracoes();
            } else if (mainControllerFuncionario != null) {
                mainControllerFuncionario.showConfiguracoes();
            } else {
                System.err.println(bundle.getString("log.error.mainControllerNull"));
                mostrarAlerta(
                        bundle.getString("alert.error.reload.title"),
                        bundle.getString("alert.error.mainControllerNull.header"),
                        null
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(
                    bundle.getString("alert.error.reload.title"),
                    bundle.getString("alert.error.reload.header"),
                    e.getMessage()
            );
        }
    }

    @FXML
    private void fecharJanela() {
        Stage stage = (Stage) txtNome.getScene().getWindow();
        stage.close();
    }
}