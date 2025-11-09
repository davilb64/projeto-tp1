package app.humanize.controller;

import app.humanize.model.StatusVaga;
import app.humanize.model.Vaga;
import app.humanize.repository.VagaRepository;
import app.humanize.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class CriarVagaController {
    @FXML
    private Label lblId;
    @FXML private TextField txtCargo;
    @FXML private TextField txtSalario;
    @FXML private TextField txtRequisitos;
    @FXML private ChoiceBox<String> choiceStatus;
    @FXML private TextField txtDepartamento;

    private final VagaRepository vagaRepository = VagaRepository.getInstance();
    private ResourceBundle bundle;
    private Vaga vagaParaEditar;

    // Mapa para associar o status traduzido de volta ao Enum
    private final Map<String, StatusVaga> statusMap = new HashMap<>();

    @FXML
    public void initialize() {
        this.bundle = UserSession.getInstance().getBundle();

        // Configurar as opções do ChoiceBox com tradução
        String statusAberta = bundle.getString("jobStatus.open");
        String statusFechada = bundle.getString("jobStatus.closed");

        statusMap.put(statusAberta, StatusVaga.ABERTA);
        statusMap.put(statusFechada, StatusVaga.FECHADA);

        choiceStatus.getItems().addAll(statusAberta, statusFechada);

        if (vagaParaEditar == null) {
            lblId.setText(String.valueOf(vagaRepository.getProximoId()));
            choiceStatus.setValue(statusAberta); // Valor padrão para novas vagas
        }
    }

    public void prepararParaEdicao(Vaga vaga) {
        this.vagaParaEditar = vaga;

        lblId.setText(String.valueOf(vaga.getId())); // Define o ID existente
        txtCargo.setText(vaga.getCargo());
        txtSalario.setText(vaga.getSalario());
        txtRequisitos.setText(vaga.getRequisitos());
        txtDepartamento.setText(vaga.getDepartamento());

        // Define o valor traduzido com base no enum
        String statusKey = (vaga.getStatus() == StatusVaga.ABERTA) ? "jobStatus.open" : "jobStatus.closed";
        choiceStatus.setValue(bundle.getString(statusKey));
    }

    private boolean validarCampos() {
        if (txtCargo.getText().isBlank() || txtRequisitos.getText().isBlank() ||
                choiceStatus.getValue() == null || txtDepartamento.getText().isBlank()) {
            mostrarAlerta(
                    bundle.getString("createJob.alert.requiredFields.title"),
                    bundle.getString("createJob.alert.requiredFields.header"),
                    null
            );
            return false;
        }
        return true;
    }

    @FXML
    private void salvarVaga() {
        if (!validarCampos()) {
            return;
        }

        // Obtém o Enum correspondente ao texto traduzido selecionado
        StatusVaga statusSelecionado = statusMap.get(choiceStatus.getValue());

        if (vagaParaEditar == null) {
            try{
                Vaga vaga = new Vaga(txtCargo.getText(), statusSelecionado, txtSalario.getText(), txtRequisitos.getText(), txtDepartamento.getText());
                vagaRepository.escreveVagaNova(vaga);
            }catch (Exception e){
                mostrarAlerta(
                        bundle.getString("alert.error.unexpected.title"),
                        bundle.getString("alert.error.unexpected.header.tryAgain"),
                        e.getMessage()
                );
            }

        } else {
            try{
                vagaParaEditar.setCargo(txtCargo.getText());
                vagaParaEditar.setRequisitos(txtRequisitos.getText());
                vagaParaEditar.setSalario(txtSalario.getText());
                vagaParaEditar.setStatus(statusSelecionado);
                vagaParaEditar.setDepartamento(txtDepartamento.getText());

                vagaRepository.atualizarVaga();
            }catch (Exception e){
                mostrarAlerta(
                        bundle.getString("alert.error.unexpected.title"),
                        bundle.getString("alert.error.unexpected.header.tryAgain"),
                        e.getMessage()
                );
            }
        }
        fecharJanela();
    }


    private void mostrarAlerta(String titulo, String mensagem, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(mensagem);
        alert.setContentText(conteudo != null ? conteudo : "");
        alert.showAndWait();
    }

    @FXML
    private void fecharJanela() {
        Stage stage = (Stage) txtCargo.getScene().getWindow();
        stage.close();
    }
}