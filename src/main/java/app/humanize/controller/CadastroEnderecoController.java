package app.humanize.controller;

import app.humanize.model.Endereco;
import app.humanize.util.EnderecoViaCep;
import app.humanize.util.EstadosBrasileiros;
import app.humanize.util.UserSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ResourceBundle;

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
    @FXML private ProgressIndicator progressIndicator;

    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    Endereco enderecoSalvo;

    private ResourceBundle bundle;

    @FXML
    private void initialize(){
        this.bundle = UserSession.getInstance().getBundle();

        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }

        //listener CEP
        txtCep.textProperty().addListener((observable, oldValue, newValue) -> {
            String cepNumerico = newValue.replaceAll("[^0-9]", "");

            if(cepNumerico.length() == 8){
                buscarEnderecoPorCep(cepNumerico);
            }
        });
        txtCep.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                String cepNumerico = txtCep.getText().replaceAll("[^0-9]", "");
                if (cepNumerico.length() == 8) {
                    buscarEnderecoPorCep(cepNumerico);
                }
            }
        });

        estadoCombo.getItems().setAll(EstadosBrasileiros.values());
    }

    private void buscarEnderecoPorCep(String cep) {
        if (progressIndicator != null) {
            progressIndicator.setVisible(true);
        }
        limparCamposEndereco();

        Task<EnderecoViaCep> task = new Task<>() {
            @Override
            protected EnderecoViaCep call() throws Exception {
                String uri = "https://viacep.com.br/ws/" + cep + "/json/";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(uri))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), EnderecoViaCep.class);
                } else {
                    throw new RuntimeException("Falha na API ViaCEP: Código " + response.statusCode());
                }
            }
        };

        task.setOnSucceeded(event -> {
            EnderecoViaCep endereco = task.getValue();
            if (endereco != null && !endereco.isErro()) {
                preencherCamposEndereco(endereco);
            } else {
                mostrarAlerta("addressRegistration.alert.cepNotFound.title", "addressRegistration.alert.cepNotFound.header", null);
            }
            if (progressIndicator != null) {
                progressIndicator.setVisible(false);
            }
        });

        task.setOnFailed(event -> {
            Throwable ex = task.getException();
            mostrarAlerta("addressRegistration.alert.networkError.title", "addressRegistration.alert.networkError.header", ex.getMessage());
            ex.printStackTrace();
            if (progressIndicator != null) {
                progressIndicator.setVisible(false);
            }
        });

        new Thread(task).start();
    }

    private void limparCamposEndereco() {
        Platform.runLater(() -> {
            txtLogradouro.clear();
            txtNumero.clear();
            txtBairro.clear();
            txtCidade.clear();
            estadoCombo.setValue(null);
        });
    }

    private void preencherCamposEndereco(EnderecoViaCep endereco) {
        Platform.runLater(() -> {
            txtLogradouro.setText(endereco.getLogradouro() != null ? endereco.getLogradouro() : "");
            txtBairro.setText(endereco.getBairro() != null ? endereco.getBairro() : "");
            txtCidade.setText(endereco.getLocalidade() != null ? endereco.getLocalidade() : "");
            estadoCombo.setValue(EstadosBrasileiros.valueOf(endereco.getUf() != null ? endereco.getUf() : ""));
            txtNumero.requestFocus();
        });
    }

    @FXML
    private void cancelar(){
        Stage stage = (Stage) txtCep.getScene().getWindow();
        stage.close();
    }
    @FXML
    private void salvarEndereco(){
        txtNumero.getText();

        if (txtLogradouro.getText().isBlank() || estadoCombo.getSelectionModel().isEmpty()){
            mostrarAlerta("addressRegistration.alert.fillAllFields.title", "addressRegistration.alert.fillAllFields.header", null);
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

    private void mostrarAlerta(String tituloKey, String cabecalhoKey, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(bundle.getString(tituloKey));
        alert.setHeaderText(bundle.getString(cabecalhoKey));
        alert.setContentText(conteudo != null ? conteudo : "");
        alert.showAndWait();
    }
}