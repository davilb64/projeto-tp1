package app.humanize.controller;

import app.humanize.model.Candidato;
import app.humanize.model.Vaga;
import app.humanize.model.Candidatura;
import app.humanize.model.StatusVaga;
import app.humanize.model.StatusCandidatura;
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
        // Carregar vagas disponíveis (status = ABERTA)
        List<Vaga> todasVagas = vagaRepository.getTodasVagas();
        List<Vaga> vagasAbertas = todasVagas.stream()
                .filter(vaga -> vaga.getStatus() == StatusVaga.ABERTA)
                .collect(Collectors.toList());
        listVagas.getItems().setAll(vagasAbertas);

        // Carregar todos os candidatos
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
            novaCandidatura.setStatus(StatusCandidatura.EM_ANALISE);
            novaCandidatura.setDataCandidatura(LocalDate.now());

            candidaturaRepository.salvar(novaCandidatura);

            mostrarSucesso(
                    bundle.getString("applyToJob.alert.success.title"),
                    bundle.getString("applyToJob.alert.success.header"),
                    bundle.getString("applyToJob.alert.success.content")
            );
            limparSelecoes();

           /* if (controllerPai != null) {
                controllerPai.showStatus(); // ✅ vai pra tela StatusDaCandidatura
            }*/

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

    // Métodos auxiliares para mostrar alertas
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