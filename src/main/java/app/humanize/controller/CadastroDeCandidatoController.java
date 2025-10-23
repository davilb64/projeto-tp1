package app.humanize.controller;

import app.humanize.model.Candidato;
import app.humanize.model.Endereco;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import app.humanize.model.Vaga;

import java.time.LocalDate;

public class CadastroDeCandidatoController {

    @FXML private TextField txtNome;
    @FXML private TextField txtCpf;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefone;
    @FXML private TextField txtFormacao;
    @FXML private TextField txtDisponibilidade;
    @FXML private TextField txtPretencao;
    @FXML private TextField txtExperiencia;
    @FXML private ChoiceBox<Vaga> choiceVaga;
    @FXML private Button btnUpload;
    @FXML private Button btnSalvar;

    @FXML
    private void initialize() {
        choiceVaga.getItems().addAll(
                new Vaga("Analista de Dados"),
                new Vaga("Desenvolvedor Backend"),
                new Vaga("Designer UI/UX"),
                new Vaga("Engenheiro de Software"),
                new Vaga("Gerente de Projetos")
        );
    }

    // Ação do botão "Salvar"
    @FXML
    private void salvarCandidato() {
        try {
            // Conversão segura de pretensão salarial
            double pretencao = 0.0;
            if (!txtPretencao.getText().trim().isEmpty()) {
                pretencao = Double.parseDouble(txtPretencao.getText());
            }

            // Aqui poderia vir um endereço real (mockado por enquanto)
            //Endereco enderecoFake = new Endereco(); // supondo que tenha construtor padrão

            // Monta o candidato usando o builder

            Candidato candidato = new Candidato.CandidatoBuilder()
                    .nome(txtNome.getText())
                    .cpf(txtCpf.getText())
                    .email(txtEmail.getText())
                    .telefone(txtTelefone.getText())
                    .formacao(txtFormacao.getText())
                    .disponibilidade(txtDisponibilidade.getText())
                    .pretencaoSalarial(pretencao)
                    .experiencia(txtExperiencia.getText())
                    .vaga(choiceVaga.getValue())
                    .dataCadastro(LocalDate.now())
                    .build();

            StatusDaCandidaturaController.adicionarCandidato(candidato);

            // Exemplo de confirmação
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Cadastro realizado");
            alert.setHeaderText("Candidato salvo com sucesso!");
            alert.setContentText("Nome: " + candidato.getNome() + "\nCPF: " + candidato.getCpf());
            alert.showAndWait();

            limparCampos();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro no cadastro");
            alert.setHeaderText("Não foi possível salvar o candidato");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void uploadDocumentos() {
        System.out.println("salvo!");
        // Aqui você pode implementar FileChooser futuramente
    }

    private void limparCampos() {
        txtNome.clear();
        txtCpf.clear();
        txtEmail.clear();
        txtTelefone.clear();
        txtFormacao.clear();
        txtDisponibilidade.clear();
        txtPretencao.clear();
        txtExperiencia.clear();
    }
}
