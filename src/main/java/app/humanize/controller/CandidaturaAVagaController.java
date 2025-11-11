package app.humanize.controller;

import app.humanize.model.*;
import app.humanize.repository.CandidatoRepository;
import app.humanize.repository.VagaRepository;
import app.humanize.repository.CandidaturaRepository;
import app.humanize.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CandidaturaAVagaController {

    @FXML private ListView<Vaga> listVagas;
    @FXML private ListView<Candidato> listCandidatos;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;


    private CandidaturaRepository candidaturaRepository;
    private CandidatoRepository candidatoRepository;
    private VagaRepository vagaRepository;
    private ResourceBundle bundle;

    public CandidaturaAVagaController() {
        this.candidaturaRepository = CandidaturaRepository.getInstance();
        this.candidatoRepository = CandidatoRepository.getInstance();
        this.vagaRepository = VagaRepository.getInstance();
    }

    @FXML
    public void initialize() {
        this.bundle = UserSession.getInstance().getBundle();
        carregarDados();
        configurarBotoes();
        configurarListViews();
        configurarSelecoes();
    }



    private void carregarDados() {
        Usuario recrutadorLogado = UserSession.getInstance().getUsuarioLogado();
        if (recrutadorLogado instanceof Administrador) {
            List<Vaga> vagas = vagaRepository.getTodasVagas();
            listVagas.getItems().setAll(vagas);
        } else if (recrutadorLogado == null) {
            listVagas.getItems().clear();
            System.err.println("Erro: Nenhum recrutador está logado.");
        } else {
            List<Vaga> vagasDoRecrutador = vagaRepository.getVagasAbertasPorRecrutador(recrutadorLogado);
            listVagas.getItems().setAll(vagasDoRecrutador);
        }

        List<Candidato> candidatos = candidatoRepository.getTodos();
        listCandidatos.getItems().setAll(candidatos);
    }

    private void configurarListViews() {
        // Configurar como exibir as vagas
        listVagas.setCellFactory(lv -> new ListCell<Vaga>() {
            @Override
            protected void updateItem(Vaga vaga, boolean empty) {
                super.updateItem(vaga, empty);
                if (empty || vaga == null) {
                    setText(null);
                } else {
                    setText(String.format("%s - %s - R$ %s",
                            vaga.getCargo(),
                            vaga.getDepartamento(),
                            vaga.getSalario()));
                }
            }
        });

        // Configurar como exibir os candidatos
        listCandidatos.setCellFactory(lv -> new ListCell<Candidato>() {
            @Override
            protected void updateItem(Candidato candidato, boolean empty) {
                super.updateItem(candidato, empty);
                if (empty || candidato == null) {
                    setText(null);
                } else {
                    setText(String.format("%s - %s",
                            candidato.getNome(),
                            candidato.getEmail()));
                }
            }
        });
    }

    private void configurarSelecoes() {
        // Habilitar botão apenas quando ambos estiverem selecionados
        btnSalvar.disableProperty().bind(
                listCandidatos.getSelectionModel().selectedItemProperty().isNull()
                        .or(listVagas.getSelectionModel().selectedItemProperty().isNull())
        );
    }

    private void configurarBotoes() {
        btnSalvar.setOnAction(event -> associarCandidatura());
        btnCancelar.setOnAction(event -> limparSelecoes());
    }

    private void associarCandidatura() {
        Candidato candidatoSelecionado = listCandidatos.getSelectionModel().getSelectedItem();
        Vaga vagaSelecionada = listVagas.getSelectionModel().getSelectedItem();

        if (candidatoSelecionado == null || vagaSelecionada == null) {
            mostrarAlerta(
                    bundle.getString("applyToJob.alert.validation.title"),
                    bundle.getString("applyToJob.alert.validation.header"),
                    bundle.getString("applyToJob.alert.validation.content")
            );
            return;
        }

        try {
            if (candidaturaRepository.existeCandidatura(candidatoSelecionado, vagaSelecionada)) {
                mostrarAlerta(
                        bundle.getString("applyToJob.alert.alreadyApplied.title"),
                        bundle.getString("applyToJob.alert.alreadyApplied.header"),
                        bundle.getString("applyToJob.alert.alreadyApplied.content")
                );
                return;
            }

            Candidatura novaCandidatura = new Candidatura();
            novaCandidatura.setCandidato(candidatoSelecionado);
            novaCandidatura.setVaga(vagaSelecionada);
            novaCandidatura.setStatus(StatusCandidatura.PENDENTE);
            novaCandidatura.setDataCandidatura(LocalDate.now());

            candidaturaRepository.salvar(novaCandidatura);

            mostrarSucesso(
                    bundle.getString("applyToJob.alert.success.title"),
                    bundle.getString("applyToJob.alert.success.header"),
                    bundle.getString("applyToJob.alert.success.content")
            );
            limparSelecoes();


        } catch (Exception e) {
            mostrarErro(
                    bundle.getString("applyToJob.alert.error.title"),
                    bundle.getString("applyToJob.alert.error.header"),
                    e.getMessage()
            );
        }
    }


    private void limparSelecoes() {
        listCandidatos.getSelectionModel().clearSelection();
        listVagas.getSelectionModel().clearSelection();
    }


    private void mostrarAlerta(String titulo, String cabecalho, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(conteudo);
        alert.showAndWait();
    }

    private void mostrarSucesso(String titulo, String cabecalho, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(conteudo);
        alert.showAndWait();
    }

    private void mostrarErro(String titulo, String cabecalho, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(conteudo);
        alert.showAndWait();
    }
}