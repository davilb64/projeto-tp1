package app.humanize.controller;

import app.humanize.model.*;
import app.humanize.repository.CandidatoRepository;
import app.humanize.repository.EntrevistaRepository;
import app.humanize.repository.UsuarioRepository;
import app.humanize.repository.VagaRepository;
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
    private Button btnSalvar;

    @FXML
    private Button btnCancelar;

    // 🔹 Repositórios
    private final CandidatoRepository candidatoRepository = CandidatoRepository.getInstance();
    private final VagaRepository vagaRepository = VagaRepository.getInstance();
    private final UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();
    private final EntrevistaRepository entrevistaRepository = EntrevistaRepository.getInstance();

    private Entrevista entrevistaParaEditar;

    private static final DateTimeFormatter BR_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private void initialize() {
        if (entrevistaParaEditar == null) {
            lblId.setText(String.valueOf(entrevistaRepository.getProximoId()));
        }
        carregarChoiceBoxes();
        configurarDatePicker();
    }

    /** Define valor inicial, formato e regras de seleção do DatePicker */
    private void configurarDatePicker() {
        // 1) Valor padrão (hoje) — ajuste para LocalDate.now().plusDays(1) se quiser “a partir de amanhã”
        dtDataEntrevista.setValue(LocalDate.now());

        // 2) Placeholder quando vazio
        dtDataEntrevista.setPromptText("dd/MM/aaaa");

        // 3) Conversor para mostrar/ler no formato dd/MM/yyyy
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
                    // opcional: mostre um alerta amigável
                    mostrarAlerta("Data inválida", "Use o formato dd/MM/aaaa.", null);
                    return null;
                }
            }
        });

        // 4) (Opcional) Desabilitar datas passadas
        dtDataEntrevista.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                boolean passado = date.isBefore(LocalDate.now());
                setDisable(empty || passado);
                if (passado) {
                    setStyle("-fx-opacity: 0.6;"); // visual de desabilitado
                }
            }
        });

        // 5) (Opcional) Ação ao escolher a data
        dtDataEntrevista.setOnAction(ev -> {
            LocalDate selecionada = dtDataEntrevista.getValue();
            // faça algo se precisar (log, validação adicional, etc.)
        });
    }

    // 🔹 Carrega listas nos ChoiceBoxes
    private void carregarChoiceBoxes() {
        try {
            cbCandidato.setItems(FXCollections.observableArrayList(candidatoRepository.getTodos()));
            cbCargo.setItems(FXCollections.observableArrayList(vagaRepository.getTodasVagas()));
            cbRecrutador.setItems(FXCollections.observableArrayList(usuarioRepository.getRecrutadores()));
        } catch (Exception e) {
            mostrarAlerta("Erro ao carregar dados", "Falha ao preencher os campos de seleção.", e.getMessage());
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

                Entrevista entrevista = new Entrevista(recrutador, vaga, candidato, null, data);
                entrevistaRepository.escreveEntrevistaNova(entrevista);

                mostrarAlerta("Sucesso", "Entrevista marcada com sucesso!", null);
                limparCampos();

            } catch (IOException e) {
                mostrarAlerta("Erro ao salvar", "Não foi possível registrar a entrevista.", e.getMessage());
            } catch (Exception e) {
                mostrarAlerta("Erro inesperado", "Tente novamente mais tarde.", e.getMessage());
            }
        }else{
            try{
                entrevistaParaEditar.setVaga(cbCargo.getValue());
                entrevistaParaEditar.setCandidato(cbCandidato.getValue());
                entrevistaParaEditar.setRecrutador(cbRecrutador.getValue());
                entrevistaParaEditar.setDataEntrevista(dtDataEntrevista.getValue());

                entrevistaRepository.atualizarEntrevista();
            }catch (Exception e){
                mostrarAlerta("Erro inesperado","Tente novamente", e.getMessage());
            }
        }
        fecharJanela();
    }

    // 🔹 Fecha a janela
    @FXML
    private void fecharJanela(ActionEvent event) {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    // 🔹 Validação simples
    private boolean validarCampos() {
        if (cbCandidato.getValue() == null) {
            mostrarAlerta("Campo obrigatório", "Selecione um candidato.", null);
            return false;
        }
        if (cbCargo.getValue() == null) {
            mostrarAlerta("Campo obrigatório", "Selecione uma vaga (cargo).", null);
            return false;
        }
        if (cbRecrutador.getValue() == null) {
            mostrarAlerta("Campo obrigatório", "Selecione o recrutador responsável.", null);
            return false;
        }
        if (dtDataEntrevista.getValue() == null) {
            mostrarAlerta("Campo obrigatório", "Escolha uma data para a entrevista.", null);
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
    private void mostrarAlerta(String titulo, String mensagem, String detalhe) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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

    }
}
