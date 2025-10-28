package app.humanize.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;

public class ContraChequeController {

    @FXML private TextField txtNome;
    @FXML private TextField txtCargo;
    @FXML private TextField txtDepartamento;

    @FXML private TableView <String> tabelaContracheque;
    @FXML private TableColumn <String, String> colunaDesc;
    @FXML private  TableColumn <String, String> colunaVenci;
    @FXML private  TableColumn <String, String> colunaDescon;
    @FXML private  TableColumn  <String, String> colunaSald;

    @FXML private Button btnImprimir;
    @FXML private Button btnExportar;

    @FXML
    private void initialize() {
        configurarBotoes();
    }

    private void configurarBotoes() {
        btnImprimir.setOnAction(event -> imprimirContraCheque());
        btnExportar.setOnAction(event -> exportarContraCheque());
    }

    public void exportarContraCheque() {
        if (txtNome.getText().isEmpty() || txtCargo.getText().isEmpty() || txtDepartamento.getText().isEmpty()) {
            mostrarAlerta("Atenção", "Preencha todos os campos antes de exportar.", Alert.AlertType.WARNING);
            return;
        }
        // faltando logica de exportacao
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {

        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    public void imprimirContraCheque() {
        if (txtNome.getText().isEmpty() || txtCargo.getText().isEmpty() || txtDepartamento.getText().isEmpty()) {
            mostrarAlerta("Atenção", "Preencha todos os campos antes de imprimir.", Alert.AlertType.WARNING);
            return;
        }
        // faltando logica de impressao nesse diacho
    }
}