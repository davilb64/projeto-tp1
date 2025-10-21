package app.humanize.controller;

import app.humanize.model.Endereco;
import app.humanize.util.EstadosBrasileiros;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CadastroEnderecoController {
    @FXML
    private TextField txtLogradouro;
    @FXML
    private TextField txtNumero;
    @FXML
    private TextField txtBairro;
    @FXML
    private TextField txtCidade;
    @FXML
    private TextField txtCep;
    @FXML
    private ComboBox<EstadosBrasileiros> estadoCombo;

    Endereco enderecoSalvo;

    @FXML
    private void initialize(){
        estadoCombo.getItems().setAll(EstadosBrasileiros.values());
    }

    @FXML
    private void cancelar(){
        Stage stage = (Stage) txtCep.getScene().getWindow();
        stage.close();
    }
    @FXML
    private void salvarEndereco(){
        String numeroTexto = txtNumero.getText();
        int numero;

        try {
            numero = Integer.parseInt(numeroTexto);
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "O campo 'Número' deve conter apenas dígitos.");
            alert.setHeaderText("Formato Inválido");
            alert.showAndWait();
            return;
        }

        if (txtLogradouro.getText().isBlank() || estadoCombo.getSelectionModel().isEmpty()){
            new Alert(Alert.AlertType.WARNING, "Preencha todos os campos.").showAndWait();
            return;
        }

        EstadosBrasileiros estado = estadoCombo.getSelectionModel().getSelectedItem();
        enderecoSalvo = new Endereco.EnderecoBuilder()
                .logradouro(txtLogradouro.getText()).numero(Integer.parseInt(txtNumero.getText())).bairro(txtBairro.getText()).cidade(txtCidade.getText()).cep(txtCep.getText()).estado(estado).build();
        Stage stage = (Stage) txtLogradouro.getScene().getWindow();
        stage.close();
    }

    public Endereco getEnderecoSalvo() {
        return enderecoSalvo;
    }
}
