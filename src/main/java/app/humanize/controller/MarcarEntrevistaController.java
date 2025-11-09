package app.humanize.controller;

import app.humanize.model.*;
import app.humanize.repository.CandidatoRepository;
import app.humanize.repository.EntrevistaRepository;
import app.humanize.repository.UsuarioRepository;
import app.humanize.repository.VagaRepository;
import app.humanize.util.UserSession;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
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

    // 🔹 Campos da interface
    @FXML
    private Label lblId;

    @FXML
    private ChoiceBox<Candidato> cbCandidato;

    @FXML
    private ChoiceBox<Vaga> cbCargo;

    @FXML
    private ChoiceBox<Usuario> cbRecrutador;

    @FXML
    private DatePicker dtDataEntrevista;

    @FXML
    private ChoiceBox<StatusEntrevista> cbStatus;

    @FXML
    private Button btnSalvar;

    @FXML
    private Button btnCancelar;

    // 🔹 Repositórios
    private final CandidatoRepository candidatoRepository = CandidatoRepository.getInstance();
    private final VagaRepository vagaRepository = VagaRepository.getInstance();
    private final UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();
    private final EntrevistaRepository entrevistaRepository = EntrevistaRepository.getInstance();

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
        // 1) Valor padrão
        dtDataEntrevista.setValue(LocalDate.now());

        // 2) Placeholder
        dtDataEntrevista.setPromptText("dd/MM/aaaa");

        // 3) Conversor para formato dd/MM/yyyy
        dtDataEntrevista.setConverter(new StringConverter<LocalDate>() {
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

        // 4) Desabilitar datas passadas
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

    // Helper para traduzir o Enum StatusEntrevista
    private String getTraducaoStatus(StatusEntrevista status) {
        if (status == null) return null;
        String key = "statusEntrevista." + status.name();
        return bundle.containsKey(key) ? bundle.getString(key) : status.name();
    }

    // 🔹 Carrega listas nos ChoiceBoxes
    private void carregarChoiceBoxes() {
        try {
            cbCandidato.setItems(FXCollections.observableArrayList(candidatoRepository.getTodos()));
            cbCargo.setItems(FXCollections.observableArrayList(vagaRepository.getTodasVagas()));
            cbRecrutador.setItems(FXCollections.observableArrayList(usuarioRepository.getRecrutadores()));
            cbStatus.setItems(FXCollections.observableArrayList(StatusEntrevista.values()));

            // ### CORREÇÃO AQUI ###
            // Substitui setCellFactory e setButtonCell por setConverter
            cbStatus.setConverter(new StringConverter<StatusEntrevista>() {
                @Override
                public String toString(StatusEntrevista status) {
                    // Retorna a string traduzida para exibir
                    return getTraducaoStatus(status);
                }

                @Override
                public StatusEntrevista fromString(String string) {
                    // Converte a string (traduzida) de volta para o Enum
                    if (string == null) return null;
                    for (StatusEntrevista status : StatusEntrevista.values()) {
                        if (getTraducaoStatus(status).equals(string)) {
                            return status;
                        }
                    }
                    return null;
                }
            });

            // Define um valor padrão para novos cadastros
            if (entrevistaParaEditar == null) {
                cbStatus.setValue(StatusEntrevista.Pendente);
            }

        } catch (Exception e) {
            mostrarAlerta(
                    bundle.getString("scheduleInterview.alert.loadDataError.title"),
                    bundle.getString("scheduleInterview.alert.loadDataError.header"),
                    e.getMessage(),
                    Alert.AlertType.ERROR
            );
        }
    }

    // 🔹 Salvar nova entrevista
    @FXML
    private void salvarEntrevista(ActionEvent event) {
        if (!validarCampos()) {
            return;
        }
        if(this.entrevistaParaEditar == null) {
            try {
                Candidato candidato = cbCandidato.getValue();
                Vaga vaga = cbCargo.getValue();
                Usuario recrutador = cbRecrutador.getValue();
                LocalDate data = dtDataEntrevista.getValue();
                StatusEntrevista status = cbStatus.getValue();

                Entrevista entrevista = new Entrevista(recrutador, vaga, candidato, status, data);
                entrevistaRepository.escreveEntrevistaNova(entrevista);

                mostrarAlerta(
                        bundle.getString("scheduleInterview.alert.success.title"),
                        bundle.getString("scheduleInterview.alert.success.header"),
                        null,
                        Alert.AlertType.INFORMATION
                );
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
                entrevistaParaEditar.setVaga(cbCargo.getValue());
                entrevistaParaEditar.setCandidato(cbCandidato.getValue());
                entrevistaParaEditar.setRecrutador(cbRecrutador.getValue());
                entrevistaParaEditar.setDataEntrevista(dtDataEntrevista.getValue());
                entrevistaParaEditar.setStatus(cbStatus.getValue());
                entrevistaRepository.atualizarEntrevista();
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

    // 🔹 Fecha a janela
    @FXML
    private void fecharJanela() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    // 🔹 Validação simples
    private boolean validarCampos() {
        if (cbCandidato.getValue() == null) {
            mostrarAlerta(bundle.getString("scheduleInterview.alert.validation.title"), bundle.getString("scheduleInterview.alert.validation.candidate"), null, Alert.AlertType.WARNING);
            return false;
        }
        if (cbCargo.getValue() == null) {
            mostrarAlerta(bundle.getString("scheduleInterview.alert.validation.title"), bundle.getString("scheduleInterview.alert.validation.job"), null, Alert.AlertType.WARNING);
            return false;
        }
        if (cbRecrutador.getValue() == null) {
            mostrarAlerta(bundle.getString("scheduleInterview.alert.validation.title"), bundle.getString("scheduleInterview.alert.validation.recruiter"), null, Alert.AlertType.WARNING);
            return false;
        }
        if (dtDataEntrevista.getValue() == null) {
            mostrarAlerta(bundle.getString("scheduleInterview.alert.validation.title"), bundle.getString("scheduleInterview.alert.validation.date"), null, Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    // 🔹 Limpa todos os campos após salvar
    private void limparCampos() {
        cbCandidato.setValue(null);
        cbCargo.setValue(null);
        cbRecrutador.setValue(null);
        dtDataEntrevista.setValue(null);
    }

    // 🔹 Mostra alerta genérico
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
        cbCandidato.setValue(entrevista.getCandidato());
        cbCargo.setValue(entrevista.getVaga());
        cbRecrutador.setValue(entrevista.getRecrutador());
        dtDataEntrevista.setValue(entrevista.getDataEntrevista());
        cbStatus.setValue(entrevista.getStatus()); // Define o status
    }
}