package app.humanize.controller;

import app.humanize.model.Candidato;
import app.humanize.model.Vaga;
import app.humanize.repository.CandidatoRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.time.LocalDate;

public class CadastroDeCandidatoController {

    @FXML private TextField txtNome;
    @FXML private TextField txtCpf;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefone;
    @FXML private TextField txtFormacao;
    @FXML private TextField txtDisponibilidade;
    @FXML private TextField txtPretencao;
    @FXML private TextArea txtExperiencia;
    @FXML private ComboBox<Vaga> comboVaga;
    @FXML private Button btnUpload;
    @FXML private Button btnSalvar;

    private Candidato candidatoEmEdicao = null; // 🔹 usado quando estiver editando

    @FXML
    private void initialize() {
        comboVaga.getItems().addAll(
                criarVaga("Analista de Dados"),
                criarVaga("Desenvolvedor Backend"),
                criarVaga("Designer UI/UX"),
                criarVaga("Engenheiro de Software"),
                criarVaga("Gerente de Projetos")
        );
    }

    private Vaga criarVaga(String nome) {
        Vaga v = new Vaga();
        v.setCargo(nome);
        return v;
    }

    // 🔹 chamado quando clicamos em "Editar" na tabela
    public void prepararParaEdicao(Candidato candidato) {
        this.candidatoEmEdicao = candidato;
        txtNome.setText(candidato.getNome());
        txtCpf.setText(candidato.getCpf());
        txtEmail.setText(candidato.getEmail());
        txtTelefone.setText(candidato.getTelefone());
        txtFormacao.setText(candidato.getFormacao());
        txtDisponibilidade.setText(candidato.getDisponibilidade());
        txtPretencao.setText(String.valueOf(candidato.getPretencaoSalarial()));
        txtExperiencia.setText(candidato.getExperiencia());
        comboVaga.setValue(candidato.getVaga());
    }

    @FXML
    private void salvarCandidato() {
        try {
            double pretencao = txtPretencao.getText().isEmpty() ? 0.0 : Double.parseDouble(txtPretencao.getText());

            if (candidatoEmEdicao == null) {
                // ➕ novo candidato
                Candidato novo = new Candidato.CandidatoBuilder()
                        .nome(txtNome.getText())
                        .cpf(txtCpf.getText())
                        .email(txtEmail.getText())
                        .telefone(txtTelefone.getText())
                        .formacao(txtFormacao.getText())
                        .disponibilidade(txtDisponibilidade.getText())
                        .pretencaoSalarial(pretencao)
                        .experiencia(txtExperiencia.getText())
                        .vaga(comboVaga.getValue())
                        .dataCadastro(LocalDate.now())
                        .build();

                CandidatoRepository.getInstance().adicionar(novo);

                mostrarAlerta("Cadastro realizado com sucesso!");
            } else {
                // ✏️ edição de candidato existente
                candidatoEmEdicao.setNome(txtNome.getText());
                candidatoEmEdicao.setCpf(txtCpf.getText());
                candidatoEmEdicao.setEmail(txtEmail.getText());
                candidatoEmEdicao.setTelefone(txtTelefone.getText());
                candidatoEmEdicao.setFormacao(txtFormacao.getText());
                candidatoEmEdicao.setDisponibilidade(txtDisponibilidade.getText());
                candidatoEmEdicao.setPretencaoSalarial(pretencao);
                candidatoEmEdicao.setExperiencia(txtExperiencia.getText());
                candidatoEmEdicao.setVaga(comboVaga.getValue());

                CandidatoRepository.getInstance().atualizar();

                mostrarAlerta("Alterações salvas com sucesso!");
                irParaTelaStatusCandidato();

            }



        } catch (IOException e) {
            mostrarErro("Erro ao salvar candidato: " + e.getMessage());
        } catch (Exception e) {
            mostrarErro("Erro inesperado: " + e.getMessage());
        }
    }

    public void uploadDocumentos(){
        mostrarAlerta("Nao funciona ainda, bjos");
    }
    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void irParaTelaStatusCandidato() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/StatusDaCandidatura.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) btnSalvar.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Status das Candidaturas");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir tela de status: " + e.getMessage());
        }
    }

}
