package app.humanize.controller;

import app.humanize.model.Funcionario;
import app.humanize.repository.UsuarioRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class CadastroDeFuncionarioController {

    @FXML
    private TextField txtNomeFunc;

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

    private final UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();

    @FXML
    private void initialize() {
        configurarEventos();
    }

    private void configurarEventos() {
        //btnSalvar.setOnAction(this::salvarFuncionario);
        //btnCancelar.setOnAction(this::cancelar);
    }

    /*private void salvarFuncionario(ActionEvent event) {
        String nome = txtNomeFunc.getText().trim();
        String email = txtEmailFunc.getText().trim();
        String cargo = txtCargoFunc.getText().trim();
        String departamento = txtDepartamentoFunc.getText().trim();

        if (nome.isEmpty() || email.isEmpty() || cargo.isEmpty() || departamento.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos obrigatórios", "Por favor, preencha todos os campos antes de salvar.");
            return;
        }

        try {
            // Cria o funcionário via Builder
            Funcionario funcionario = new Funcionario.FuncionarioBuilder()
                    .nome(nome)
                    .email(email)
                    .build();

            // Adiciona e salva
            //UsuarioRepository.escreveUsuarioNovo(funcionario);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Funcionário salvo com sucesso!");
            limparCampos();

        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao salvar", "Não foi possível salvar o funcionário:\n" + e.getMessage());
        }
    }

    private void cancelar(ActionEvent event) {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void limparCampos() {
        txtNomeFunc.clear();
        txtEmailFunc.clear();
        txtCargoFunc.clear();
        txtDepartamentoFunc.clear();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
     */
}
