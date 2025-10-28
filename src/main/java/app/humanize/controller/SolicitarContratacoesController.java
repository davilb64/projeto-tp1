package app.humanize.controller;

import app.humanize.model.Candidato;
import app.humanize.model.Contratacao;
import app.humanize.model.Vaga;
import app.humanize.repository.CandidatoRepository;
import app.humanize.repository.ContratacaoRepository;
import app.humanize.repository.VagaRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

public class SolicitarContratacoesController
{
    @FXML
    private ComboBox<Candidato> cbCandidato;

    @FXML
    private ComboBox<Vaga> cbVaga;

    @FXML
    private DatePicker dpDataContratacao;

    @FXML
    private TextField txtRegime;

    @FXML
    private Button btnContratar;

    // Repositórios simulando acesso a dados (pode ser lista em memória ou banco)
    private final CandidatoRepository candidatoRepository = CandidatoRepository.getInstance();
    private final VagaRepository vagaRepository = VagaRepository.getInstance();
    private final ContratacaoRepository contratacaoRepository = ContratacaoRepository.getInstance();

    @FXML
    public void initialize() {
        //chamar os metodos no initialize
        carregarCandidatos();
        carregarVagas();

        btnContratar.setOnAction(event -> contratar());
    }

    private void carregarCandidatos() {
        cbCandidato.getItems().clear();
        cbCandidato.getItems().addAll(candidatoRepository.getTodos());
    }

    private void carregarVagas() {
        cbVaga.getItems().clear(); //pegar o nome do fx:id do choice box e limpar os itens
        cbVaga.getItems().addAll(vagaRepository.getTodasVagas()); // //adicionar todas as vagas no choice box
        //note que estou usando o metodo getTodasVagas da classe  VagaRepository
    }
    private void contratar() {

        if (!validarCampos()) {
            return;
        }

        try{
            // Pega os valores da tela
            Candidato candidato = cbCandidato.getValue();
            Vaga vaga = cbVaga.getValue();
            LocalDate data = dpDataContratacao.getValue();
            String regime = txtRegime.getText();

            Contratacao contratacao = new Contratacao(candidato, vaga, data, regime);
            contratacaoRepository.escreveContracaoNova(contratacao);

            // Mostra mensagem de sucesso
            mostrarMensagemSucesso();

            // Limpa os campos da tela
            limparCampos();
        }catch (Exception e){
            mostrarAlerta("Erro inesperado","Tente novamente", e.getMessage());
        }
    }

    private void limparCampos() {
        cbCandidato.setValue(null);
        cbVaga.setValue(null);
        dpDataContratacao.setValue(null);
        txtRegime.clear();
    }

    private boolean validarCampos() {
        // Pega os valores da tela
        Candidato candidato = cbCandidato.getValue();
        Vaga vaga = cbVaga.getValue();
        LocalDate data = dpDataContratacao.getValue();
        String regime = txtRegime.getText();

        StringBuilder erros = new StringBuilder();

        if (candidato == null) {
            erros.append("- Selecione um candidato.\n");
        }
        if (vaga == null) {
            erros.append("- Selecione uma vaga.\n");
        }
        if (data == null) {
            erros.append("- Informe a data de contratação.\n");
        }
        if (regime == null || regime.trim().isEmpty()) {
            erros.append("- Digite o regime (ex: CLT, Estágio, PJ).\n");
        }

        if (!erros.isEmpty()) {
            mostrarAlerta("Campos obrigatórios", erros.toString(), null);
            return false;
        }

        return true;
    }

    private void mostrarAlerta(String titulo, String mensagem, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(titulo);
        alert.setContentText(mensagem);
        alert.setContentText(conteudo);
        alert.showAndWait();
    }

    private void mostrarMensagemSucesso() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText("Contratação salva com sucesso!");
        alert.showAndWait();
    }
}
