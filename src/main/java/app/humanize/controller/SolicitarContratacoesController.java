package app.humanize.controller;

import app.humanize.model.Candidato;
import app.humanize.model.Contratacao;
import app.humanize.model.Vaga;
import app.humanize.repository.CandidatoRepository;
import app.humanize.repository.ContratacaoRepository;
import app.humanize.repository.EntrevistaRepository;
import app.humanize.repository.VagaRepository;
import app.humanize.util.UserSession;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.ResourceBundle;

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

    // Repositórios
    private final CandidatoRepository candidatoRepository = CandidatoRepository.getInstance();
    private final VagaRepository vagaRepository = VagaRepository.getInstance();
    private final ContratacaoRepository contratacaoRepository = ContratacaoRepository.getInstance();
    private final EntrevistaRepository entrevistaRepository = EntrevistaRepository.getInstance();

    private ResourceBundle bundle;

    @FXML
    public void initialize() {
        this.bundle = UserSession.getInstance().getBundle();
        //chamar os metodos no initialize
        carregarCandidatos();

        // Quando o usuário muda a vaga selecionada:
        cbCandidato.getSelectionModel().selectedItemProperty().addListener((obs, cbCandidatoOld, cbCandidatoNew) -> {
            if (cbCandidatoNew != null) {
                carregarVagas(cbCandidatoNew);
            }
        });

        btnContratar.setOnAction(event -> contratar());
    }

    private void carregarCandidatos() {
        cbCandidato.getItems().clear();
        cbCandidato.getItems().addAll(entrevistaRepository.getCandidatosAprovadosEntrevistas());
    }

    private void carregarVagas(Candidato candidato) {
        cbVaga.getItems().clear(); //pegar o nome do fx:id do choice box e limpar os itens
        cbVaga.getItems().addAll(entrevistaRepository.getVagaPorCandidato(candidato)); // //adicionar todas as vagas no choice box
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
            mostrarAlerta(
                    bundle.getString("alert.error.unexpected.title"),
                    bundle.getString("alert.error.unexpected.header.tryAgain"),
                    e.getMessage()
            );
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
            erros.append(bundle.getString("requestHire.validation.candidate")).append("\n");
        }
        if (vaga == null) {
            erros.append(bundle.getString("requestHire.validation.job")).append("\n");
        }
        if (data == null) {
            erros.append(bundle.getString("requestHire.validation.date")).append("\n");
        }
        if (regime == null || regime.trim().isEmpty()) {
            erros.append(bundle.getString("requestHire.validation.regime")).append("\n");
        }

        if (!erros.isEmpty()) {
            mostrarAlerta(
                    bundle.getString("requestHire.validation.title"),
                    bundle.getString("requestHire.validation.header"),
                    erros.toString()
            );
            return false;
        }

        return true;
    }

    private void mostrarAlerta(String titulo, String cabecalho, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(conteudo);
        alert.showAndWait();
    }

    private void mostrarMensagemSucesso() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); // Alterado para INFORMATION
        alert.setTitle(bundle.getString("alert.success.title"));
        alert.setHeaderText(null);
        alert.setContentText(bundle.getString("requestHire.alert.success.header"));
        alert.showAndWait();
    }
}