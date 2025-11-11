package app.humanize.controller;

import app.humanize.model.Entrevista;
import app.humanize.model.Funcionario;
import app.humanize.model.Perfil;
import app.humanize.model.Usuario;
import app.humanize.repository.ContratacaoRepository;
import app.humanize.repository.EntrevistaRepository;
import app.humanize.repository.UsuarioRepository;
import app.humanize.repository.VagaRepository;
import app.humanize.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ResourceBundle;


public class ContratacaoDeFuncionarioController {

    @FXML
    private ChoiceBox<Entrevista> cbNome;
    @FXML
    private TextField txtEmailFunc;
    @FXML
    private TextField txtCargoFunc;
    @FXML
    private TextField txtDepartamentoFunc;
    @FXML
    private Button btnSalvar;
    @FXML
    private Button btnCancelar;

    private final ContratacaoRepository contratacaoRepository = ContratacaoRepository.getInstance();
    private final VagaRepository vagaRepository = VagaRepository.getInstance();
    private final UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();
    private final EntrevistaRepository entrevistaRepository = EntrevistaRepository.getInstance();

    private Funcionario funcionarioParaEditar;
    private ResourceBundle bundle;

    @FXML
    public void initialize() {
        this.bundle = UserSession.getInstance().getBundle();
        cbNome.getItems().clear();
        cbNome.getItems().addAll(entrevistaRepository.buscarCandidatosAprovados());

        cbNome.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                atualizarCargo(novo); // chama o metodo sempre que o valor mudar
            }
        });
    }

    private void atualizarCargo(Entrevista entrevistaSelecionada) {
        // Exemplo: busca cargo do funcionário e preenche o campo
        if (entrevistaSelecionada != null) {
            txtCargoFunc.setText(entrevistaRepository.buscarNomeCargoEntrevista(entrevistaSelecionada.getId()));
        } else {
            txtCargoFunc.clear();
        }
    }

    @FXML
    private void salvarFuncionario() throws IOException  {

        if (!validarCampos()) {
            return;
        }

        String nome = cbNome.getValue().getCandidatura().getCandidato().getNome();
        String email = txtEmailFunc.getText();
        String cargo = txtCargoFunc.getText();
        String departamento = txtDepartamentoFunc.getText();

        if (funcionarioParaEditar == null) {
            Usuario usuario;

            usuario = new Funcionario.FuncionarioBuilder()
                    .nome(nome).email(email).perfil(Perfil.FUNCIONARIO)
                    .departamento(departamento).cargo(cargo)
                    .build();

            usuarioRepository.escreveUsuarioNovo(usuario);
        }else{
            Funcionario func = this.funcionarioParaEditar;
            func.setNome(nome);
            func.setEmail(email);
            func.setCargo(cargo);
            func.setDepartamento(departamento);

            usuarioRepository.atualizarUsuario(func);
        }
        fecharJanela();
    }

    private boolean validarCampos() {
        if (cbNome.getValue() == null || txtCargoFunc.getText().isBlank() || txtDepartamentoFunc.getText().isBlank()) {
            mostrarAlerta(
                    bundle.getString("hire.alert.requiredFields.title"),
                    bundle.getString("hire.alert.requiredFields.header"),
                    null
            );
            return false;
        }
        return true;
    }

    // 🔹 Mostra alerta genérico
    private void mostrarAlerta(String titulo, String mensagem, String detalhe) {
        Alert alert = new Alert(Alert.AlertType.WARNING); // Alterado para WARNING para erros de validação
        alert.setTitle(titulo);
        alert.setHeaderText(mensagem);
        if (detalhe != null && !detalhe.isEmpty()) {
            alert.setContentText(detalhe);
        }
        alert.showAndWait();
    }

    public void prepararParaEdicao(Funcionario funcionario) {
        this.funcionarioParaEditar = funcionario;

        cbNome.setValue(this.entrevistaRepository.buscarEntrevistaPorNomeCandidato(funcionario.getNome()));
        txtEmailFunc.setText(this.funcionarioParaEditar.getEmail());
        txtCargoFunc.setText(this.funcionarioParaEditar.getCargo());
        txtDepartamentoFunc.setText(this.funcionarioParaEditar.getDepartamento());
    }

    @FXML
    private void fecharJanela() {
        Stage stage = (Stage) cbNome.getScene().getWindow();
        stage.close();
    }
}