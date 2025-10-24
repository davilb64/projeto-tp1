package app.humanize.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;

public class RegrasSalariaisController {


    @FXML private TextField txtCargo;
    @FXML private TextField txtNivel;
    @FXML private TextField txtSalarioBase;
    @FXML private TextField txtBeneficios;
    @FXML private TextField txtDescon;
    @FXML private TextField txtBenef;
    @FXML private TextField txtAjustes;

    @FXML private Button btnSalvar;


    @FXML
    public void initialize() {
        configurarBotao();
        configurarValidacao();
    }

    private void configurarBotao() {
        btnSalvar.setOnAction(this::salvarRegras);
    }


    private void salvarRegras(ActionEvent actionEvent) {
        if (!validarCamposObrigatorios()) {
            return;
        }

        // caso esteja em texto tenta converter pra numero
        try {
            Double salarioBase = Double.valueOf(txtSalarioBase.getText());
            Double beneficios = Double.valueOf(txtBeneficios.getText());
            Double descontos = Double.valueOf(txtDescon.getText());
            Double adicionais = Double.valueOf(txtBenef.getText());
            Double ajustes = Double.valueOf(txtAjustes.getText());


            salvarRegrasNoSistema(

                    txtCargo.getText(),

                    txtNivel.getText(), salarioBase, beneficios, descontos, adicionais, ajustes);

        } catch (NumberFormatException e) {
            mostrarAlerta("Erro", "Digite apenas números nos campos financeiros.", Alert.AlertType.ERROR);
        }
    }

    private boolean validarCamposObrigatorios() {
        if (txtCargo.getText().trim().isEmpty() || txtNivel.getText().trim().isEmpty()) {
            mostrarAlerta("Atenção", "Preencha os campos Cargo e Nível.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void salvarRegrasNoSistema(String cargo, String nivel, Double salarioBase,
                                       Double beneficios, Double descontos, Double adicionais, Double ajustes) {

        // faltando logica de como salvar pdf arquivo etc

       // Double salarioTotal = salarioBase + beneficios + adicionais + ajustes - descontos;

        mostrarAlerta("Sucesso", "Regras salariais salvas com sucesso!", Alert.AlertType.INFORMATION);
        limparCampos();
    }


    private void configurarValidacao() {
        configurarCampoNumerico(txtSalarioBase);
        configurarCampoNumerico(txtBeneficios);
        configurarCampoNumerico(txtDescon);
        configurarCampoNumerico(txtBenef);
        configurarCampoNumerico(txtAjustes);
    }

    private void configurarCampoNumerico(TextField campo) {
        campo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                campo.setText(oldValue);
            }
        });
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void limparCampos() {
        txtCargo.clear();
        txtNivel.clear();
        txtSalarioBase.clear();
        txtBeneficios.clear();
        txtDescon.clear();
        txtBenef.clear();
        txtAjustes.clear();
    }
}