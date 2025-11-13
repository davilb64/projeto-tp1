package app.humanize.controller;

import app.humanize.model.*;
import app.humanize.repository.*;
import app.humanize.util.UserSession;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

public class MarcarEntrevistaController {

    @FXML
    private Label lblId;

    @FXML
    private ChoiceBox<Candidatura> cbCandidatura;

    @FXML
    private ChoiceBox<Vaga> cbVaga;

    @FXML
    private DatePicker dtDataEntrevista;

    @FXML
    private Button btnCancelar;

    private final VagaRepository vagaRepository = VagaRepository.getInstance();
    private final EntrevistaRepository entrevistaRepository = EntrevistaRepository.getInstance();
    private final CandidaturaRepository candidaturaRepository = CandidaturaRepository.getInstance();

    private Entrevista entrevistaParaEditar;
    private ResourceBundle bundle;

    private static final DateTimeFormatter BR_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private void initialize() {
        this.bundle = UserSession.getInstance().getBundle();
        if (entrevistaParaEditar == null) {
            lblId.setText(String.valueOf(entrevistaRepository.getProximoId()));
        }
        carregarChoiceBoxes();
        configurarDatePicker();
    }

    /** Define valor inicial, formato e regras de seleção do DatePicker */
    private void configurarDatePicker() {
        dtDataEntrevista.setValue(LocalDate.now());
        dtDataEntrevista.setPromptText("dd/MM/aaaa");
        dtDataEntrevista.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return (date == null) ? "" : BR_FORMATTER.format(date);
            }
            @Override
            public LocalDate fromString(String str) {
                if (str == null || str.trim().isEmpty()) return null;
                try {
                    return LocalDate.parse(str.trim(), BR_FORMATTER);
                } catch (DateTimeParseException e) {
                    mostrarAlerta(
                            bundle.getString("scheduleInterview.alert.invalidDate.title"),
                            bundle.getString("scheduleInterview.alert.invalidDate.header"),
                            null,
                            Alert.AlertType.WARNING
                    );
                    return null;
                }
            }
        });

        dtDataEntrevista.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                boolean passado = date.isBefore(LocalDate.now());
                setDisable(empty || passado);
                if (passado) {
                    setStyle("-fx-opacity: 0.6;");
                }
            }
        });
    }

    private void carregarChoiceBoxes() {
        try {
            Usuario usuarioLogado = UserSession.getInstance().getUsuarioLogado();

            if(usuarioLogado != null && usuarioLogado.getPerfil().equals(Perfil.RECRUTADOR)) {
                cbVaga.setItems(FXCollections.observableArrayList(vagaRepository.getVagasAbertasPorRecrutador(usuarioLogado)));
            }else{
                cbVaga.setItems(FXCollections.observableArrayList(vagaRepository.getTodasVagas()));
            }

            // Quando o usuário muda a vaga selecionada:
            cbVaga.getSelectionModel().selectedItemProperty().addListener((obs, vagaAntiga, vagaNova) -> {
                if (vagaNova != null) {
                    cbCandidatura.setItems(FXCollections.observableArrayList(candidaturaRepository.getCandidaturasPendentePorVaga(vagaNova)));
                }
            });
        } catch (Exception e) {
            mostrarAlerta(
                    bundle.getString("scheduleInterview.alert.loadDataError.title"),
                    bundle.getString("scheduleInterview.alert.loadDataError.header"),
                    e.getMessage(),
                    Alert.AlertType.ERROR
            );
        }
    }

    // nova entrevista
    @FXML
    private void salvarEntrevista() {
        if (!validarCampos()) {
            return;
        }
        if(this.entrevistaParaEditar == null) {
            try {
                Candidatura candidatura = cbCandidatura.getValue();
                Vaga vaga = cbVaga.getValue();
                Usuario recrutador = UserSession.getInstance().getUsuarioLogado();
                if(recrutador != null && !recrutador.getPerfil().equals(Perfil.RECRUTADOR)) {
                    mostrarAlerta(bundle.getString("scheduleInterview.alert.validation.recrutador.title"), bundle.getString("scheduleInterview.alert.validation.recrutador.job"), null, Alert.AlertType.WARNING);
                    return;
                }
                LocalDate data = dtDataEntrevista.getValue();

                Entrevista entrevista = new Entrevista(recrutador, vaga, candidatura, StatusEntrevista.Pendente, data);
                entrevistaRepository.escreveEntrevistaNova(entrevista);

                mostrarAlerta(
                        bundle.getString("scheduleInterview.alert.success.title"),
                        bundle.getString("scheduleInterview.alert.success.header"),
                        null,
                        Alert.AlertType.INFORMATION
                );
                //muda o status da candidatura
                candidatura.setStatus(StatusCandidatura.EM_ANALISE);
                candidaturaRepository.salvarOuAtualizar(candidatura);
                limparCampos();

            } catch (IOException e) {
                mostrarAlerta(
                        bundle.getString("scheduleInterview.alert.saveError.title"),
                        bundle.getString("scheduleInterview.alert.saveError.header"),
                        e.getMessage(),
                        Alert.AlertType.ERROR
                );
            } catch (Exception e) {
                mostrarAlerta(
                        bundle.getString("scheduleInterview.alert.unexpectedError.title"),
                        bundle.getString("scheduleInterview.alert.unexpectedError.header"),
                        e.getMessage(),
                        Alert.AlertType.ERROR
                );
            }
        }else{
            try{
                Usuario usuarioLogado = UserSession.getInstance().getUsuarioLogado();
                if(usuarioLogado != null && !usuarioLogado.getPerfil().equals(Perfil.RECRUTADOR)) {
                    mostrarAlerta(bundle.getString("scheduleInterview.alert.validation.recrutador.title"), bundle.getString("scheduleInterview.alert.validation.recrutador.job"), null, Alert.AlertType.WARNING);
                    return;
                }
                Candidatura candidatura = cbCandidatura.getValue();
                entrevistaParaEditar.setVaga(cbVaga.getValue());
                entrevistaParaEditar.setCandidatura(candidatura);
                entrevistaParaEditar.setRecrutador(usuarioLogado);
                entrevistaParaEditar.setDataEntrevista(dtDataEntrevista.getValue());
                entrevistaParaEditar.setStatus(StatusEntrevista.Pendente);
                entrevistaRepository.atualizarEntrevista();
                candidatura.setStatus(StatusCandidatura.EM_ANALISE);
                candidaturaRepository.salvarOuAtualizar(candidatura);
            }catch (Exception e){
                mostrarAlerta(
                        bundle.getString("scheduleInterview.alert.unexpectedError.title"),
                        bundle.getString("scheduleInterview.alert.unexpectedError.header"),
                        e.getMessage(),
                        Alert.AlertType.ERROR
                );
            }
        }
        fecharJanela();
    }

    @FXML
    private void fecharJanela() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private boolean validarCampos() {
        if (cbCandidatura.getValue() == null) {
            mostrarAlerta(bundle.getString("scheduleInterview.alert.validation.title"), bundle.getString("scheduleInterview.alert.validation.candidate"), null, Alert.AlertType.WARNING);
            return false;
        }
        if (cbVaga.getValue() == null) {
            mostrarAlerta(bundle.getString("scheduleInterview.alert.validation.title"), bundle.getString("scheduleInterview.alert.validation.job"), null, Alert.AlertType.WARNING);
            return false;
        }
        if (dtDataEntrevista.getValue() == null) {
            mostrarAlerta(bundle.getString("scheduleInterview.alert.validation.title"), bundle.getString("scheduleInterview.alert.validation.date"), null, Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void limparCampos() {
        cbCandidatura.setValue(null);
        cbVaga.setValue(null);
        dtDataEntrevista.setValue(null);
    }

    private void mostrarAlerta(String titulo, String mensagem, String detalhe, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titulo);
        alert.setHeaderText(mensagem);
        if (detalhe != null && !detalhe.isEmpty()) {
            alert.setContentText(detalhe);
        }
        alert.showAndWait();
    }

    public void prepararParaEdicao(Entrevista entrevista) {
        this.entrevistaParaEditar = entrevista;

        lblId.setText(String.valueOf(entrevista.getId()));
        cbCandidatura.setValue(entrevista.getCandidatura());
        cbVaga.setValue(entrevista.getVaga());
        dtDataEntrevista.setValue(entrevista.getDataEntrevista());
    }
}