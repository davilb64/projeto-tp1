package app.humanize.controller;

import app.humanize.model.Candidato;
import app.humanize.model.Vaga;
import app.humanize.repository.CandidatoRepository;
import app.humanize.repository.ContratacaoRepository;
import app.humanize.repository.VagaRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SolicitarContratacoesController
{
    @FXML
    private ChoiceBox<Candidato> cbCandidato;

    @FXML
    private ChoiceBox<Vaga> cbVaga;

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

    }
}
