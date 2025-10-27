package app.humanize.controller;

import app.humanize.model.Candidato;
import app.humanize.model.Vaga;
import app.humanize.model.Candidatura;
import app.humanize.model.StatusVaga;
import app.humanize.model.StatusCandidatura;
import app.humanize.repository.CandidatoRepository;
import app.humanize.repository.VagaRepository;
import app.humanize.repository.CandidaturaRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert;
import javafx.scene.control.ListCell;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CandidaturaAVagaController {

    @FXML private ListView<Vaga> listVagas;
    @FXML private ListView<Candidato> listCandidatos;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;

    private CandidaturaRepository candidaturaRepository;
    private CandidatoRepository candidatoRepository;
    private VagaRepository vagaRepository;

    public CandidaturaAVagaController() {
        // Inicializar repositórios (ajuste conforme sua implementação)
        this.candidaturaRepository = CandidaturaRepository.getInstance();
        this.candidatoRepository = CandidatoRepository.getInstance();
        this.vagaRepository = VagaRepository.getInstance();
    }

    @FXML
    public void initialize() {
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
            mostrarAlerta("Selecione um candidato e uma vaga");
            return;
        }

        try {
            // Verificar se já existe candidatura
            if (candidaturaRepository.existeCandidatura(candidatoSelecionado, vagaSelecionada)) {
                mostrarAlerta("Este candidato já se candidatou a esta vaga");
                return;
            }

            // Criar nova candidatura
            Candidatura novaCandidatura = new Candidatura();
            novaCandidatura.setCandidato(candidatoSelecionado);
            novaCandidatura.setVaga(vagaSelecionada);
            novaCandidatura.setStatus(StatusCandidatura.PENDENTE);
            novaCandidatura.setDataCandidatura(LocalDate.now());

            candidaturaRepository.salvar(novaCandidatura);

            mostrarSucesso("Candidatura associada com sucesso!");
            limparSelecoes();

        } catch (Exception e) {
            mostrarErro("Erro ao associar candidatura: " + e.getMessage());
        }
    }

    private void limparSelecoes() {
        listCandidatos.getSelectionModel().clearSelection();
        listVagas.getSelectionModel().clearSelection();
    }

    // Métodos auxiliares para mostrar alertas
    private void mostrarAlerta(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atenção");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarSucesso(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}