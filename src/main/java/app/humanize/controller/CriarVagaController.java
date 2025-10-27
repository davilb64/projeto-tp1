package app.humanize.controller;

import app.humanize.model.StatusVaga;
import app.humanize.model.Vaga;
import app.humanize.repository.VagaRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class CriarVagaController {
    @FXML
    private Label lblId;
    @FXML private TextField txtCargo;
    @FXML private TextField txtSalario;
    @FXML private TextField txtRequisitos;
    @FXML private ChoiceBox<String> choiceStatus;

    private final VagaRepository vagaRepository = VagaRepository.getInstance();

    private Vaga vagaParaEditar;

    @FXML
    public void initialize() {
        // Configurar as opções do ChoiceBox
        choiceStatus.getItems().addAll("ABERTA", "FECHADA");

        if (vagaParaEditar == null) {
            lblId.setText(String.valueOf(vagaRepository.getProximoId()));
            choiceStatus.setValue("ABERTA"); // Valor padrão para novas vagas
        }
    }

    public void prepararParaEdicao(Vaga vaga) {
        this.vagaParaEditar = vaga;

        lblId.setText(String.valueOf(vaga.getId())); // Define o ID existente
        txtCargo.setText(vaga.getCargo());
        txtSalario.setText(vaga.getSalario());
        txtRequisitos.setText(vaga.getRequisitos());

        // CORREÇÃO: Converter enum para String
        choiceStatus.setValue(vaga.getStatus().name());
    }

    private boolean validarCampos() {
        if (txtCargo.getText().isBlank() || txtRequisitos.getText().isBlank() ||
                choiceStatus.getValue() == null) {
            mostrarAlerta("Campos Obrigatórios", "Os campos Cargo, Status e Requisitos devem ser preenchidos.", null);
            return false;
        }
        return true;
    }

    @FXML
    private void salvarVaga() {
        if (!validarCampos()) {
            return;
        }

        if (vagaParaEditar == null) {

            try{
                Vaga vaga = new Vaga(txtCargo.getText(),  StatusVaga.valueOf(choiceStatus.getValue()),  txtSalario.getText(), txtRequisitos.getText());
                vagaRepository.escreveVagaNova(vaga);
            }catch (Exception e){
                mostrarAlerta("Erro inesperado","Tente novamente", e.getMessage());
            }

        } else {
            try{
                vagaParaEditar.setCargo(txtCargo.getText());
                vagaParaEditar.setRequisitos(txtRequisitos.getText());
                vagaParaEditar.setSalario(txtSalario.getText());
                vagaParaEditar.setStatus(StatusVaga.valueOf(choiceStatus.getValue()));

                vagaRepository.atualizarVaga();
            }catch (Exception e){
                mostrarAlerta("Erro inesperado","Tente novamente", e.getMessage());
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
