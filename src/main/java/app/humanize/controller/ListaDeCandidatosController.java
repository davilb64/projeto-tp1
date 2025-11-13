package app.humanize.controller;

import app.humanize.model.*;
import app.humanize.repository.CandidatoRepository;
import app.humanize.repository.CandidaturaRepository;
import app.humanize.util.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ListaDeCandidatosController {

    @FXML private TextField txtNome;
    @FXML private TextField txtFormacao;
    @FXML private TextField txtExperiencia;
    @FXML private ComboBox<String> comboSalario;
    @FXML private TableView<Candidato> tblUsuarios;
    @FXML private TableColumn<Candidato, String> colNome;
    @FXML private TableColumn<Candidato, String> colExperiencia;
    @FXML private TableColumn<Candidato, String> colFormacao;
    @FXML private TableColumn<Candidato, String> colDisponibilidade;
    @FXML private TableColumn<Candidato, Double> colPretencao;

    private final CandidatoRepository candidatoRepository = CandidatoRepository.getInstance();
    private final ObservableList<Candidato> listaCandidatos = FXCollections.observableArrayList();

    private ResourceBundle bundle;
    // Mapa para tradução do filtro de salário
    private final Map<String, String> salarioKeyMap = new HashMap<>();


    @FXML
    private void initialize() {
        this.bundle = UserSession.getInstance().getBundle();
        configurarTabela();
        carregarCandidatos();
        configurarComboSalario();
    }

    private void configurarTabela() {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colExperiencia.setCellValueFactory(new PropertyValueFactory<>("experiencia"));
        colFormacao.setCellValueFactory(new PropertyValueFactory<>("formacao"));
        colDisponibilidade.setCellValueFactory(new PropertyValueFactory<>("disponibilidade"));
        colPretencao.setCellValueFactory(new PropertyValueFactory<>("pretencaoSalarial"));

        tblUsuarios.setItems(listaCandidatos);
    }

    private void carregarCandidatos() {
        listaCandidatos.clear();
        listaCandidatos.addAll(candidatoRepository.getTodos());
    }

    private void configurarComboSalario() {
        // Pega os valores traduzidos
        String all = bundle.getString("salary.range.all");
        String upto2k = bundle.getString("salary.range.upto2k");
        String k2to4k = bundle.getString("salary.range.2kto4k");
        String k4to6k = bundle.getString("salary.range.4kto6k");
        String over6k = bundle.getString("salary.range.over6k");

        // Mapeia o valor traduzido para uma chave interna estável
        salarioKeyMap.put(all, "ALL");
        salarioKeyMap.put(upto2k, "UP_TO_2K");
        salarioKeyMap.put(k2to4k, "2K_TO_4K");
        salarioKeyMap.put(k4to6k, "4K_TO_6K");
        salarioKeyMap.put(over6k, "OVER_6K");

        comboSalario.setItems(FXCollections.observableArrayList(all, upto2k, k2to4k, k4to6k, over6k));
        comboSalario.getSelectionModel().select(all); // Seleciona "Todos" por padrão
    }

    @FXML
    private void filtra() {
        String nomeFiltro = txtNome.getText().toLowerCase();
        String formacaoFiltro = txtFormacao.getText().toLowerCase();
        String experienciaFiltro = txtExperiencia.getText().toLowerCase();
        String faixaSalarioTraduzida = comboSalario.getValue();
        String faixaSalarioKey = salarioKeyMap.getOrDefault(faixaSalarioTraduzida, "ALL");


        List<Candidato> filtrados = candidatoRepository.getTodos().stream()
                .filter(c ->
                        (safeString(c.getNome()).toLowerCase().contains(nomeFiltro) || safeString(c.getEmail()).toLowerCase().contains(nomeFiltro)) &&
                                safeString(c.getFormacao()).toLowerCase().contains(formacaoFiltro) &&
                                safeString(c.getExperiencia()).toLowerCase().contains(experienciaFiltro) &&
                                dentroDaFaixaSalarial(c.getPretencaoSalarial(), faixaSalarioKey)
                )
                .collect(Collectors.toList());

        listaCandidatos.setAll(filtrados);
    }

    private String safeString(String s) {
        return s == null ? "" : s;
    }


    private boolean dentroDaFaixaSalarial(double salario, String faixaKey) {
        return switch (faixaKey) {
            case "UP_TO_2K" -> salario <= 2000;
            case "2K_TO_4K" -> salario > 2000 && salario <= 8000;
            case "4K_TO_6K" -> salario > 8000 && salario <= 15000;
            case "OVER_6K" -> salario > 15000;
            default -> true; // "ALL"
        };
    }

    @FXML
    private void editarCandidato() {
        Candidato selecionado = tblUsuarios.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAlerta(bundle.getString("candidateList.alert.noSelectionEdit"));
            return;
        }

        CandidaturaRepository candidaturaRepository = CandidaturaRepository.getInstance();
        boolean possuiCandidaturas = candidaturaRepository.getTodas().stream()
                .anyMatch(c -> c.getCandidato().getCpf().equals(selecionado.getCpf()));

        if (possuiCandidaturas) {
            mostrarAlerta(bundle.getString("candidateList.alert.hasApplicationsEdit"));
            return;
        }

        try {
            URL resource = getClass().getResource("/view/CadastroDeCandidato.fxml");
            FXMLLoader loader = new FXMLLoader(resource, bundle);
            Parent root = loader.load();

            CadastroDeCandidatoController controller = loader.getController();
            controller.prepararParaEdicao(selecionado);

            Stage stage = new Stage();
            stage.setTitle(bundle.getString("candidateList.alert.editTitle"));
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            carregarCandidatos();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarErro(bundle.getString("candidateList.alert.errorLoadEdit") + " " + e.getMessage());
        }
    }

    @FXML
    private void excluirCandidato() {
        Candidato selecionado = tblUsuarios.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAlerta(bundle.getString("candidateList.alert.noSelectionDelete"));
            return;
        }

        CandidaturaRepository candidaturaRepository = CandidaturaRepository.getInstance();
        boolean possuiCandidaturas = candidaturaRepository.getTodas().stream()
                .anyMatch(c -> c.getCandidato().getCpf().equals(selecionado.getCpf()));

        if (possuiCandidaturas) {
            mostrarAlerta(bundle.getString("candidateList.alert.hasApplicationsDelete"));
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle(bundle.getString("candidateList.alert.confirmDeleteTitle"));
        confirmacao.setHeaderText(bundle.getString("candidateList.alert.confirmDeleteHeader"));
        confirmacao.setContentText(bundle.getString("candidateList.alert.confirmDeleteContent") + " " + selecionado.getNome() + "?");

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.OK) {
                try {
                    candidatoRepository.remover(selecionado);
                    listaCandidatos.remove(selecionado);
                    mostrarInfo(bundle.getString("candidateList.alert.deleteSuccessTitle"), selecionado.getNome());
                } catch (IOException e) {
                    mostrarErro(bundle.getString("candidateList.alert.errorDelete") + " " + e.getMessage());
                }
            }
        });
    }


    @FXML
    private void visualizarCandidato(){
        Candidato selecionado = tblUsuarios.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAlerta(bundle.getString("candidateList.alert.noSelectionView"));
            return;
        }

        try {
            URL resource = getClass().getResource("/view/CadastroDeCandidato.fxml");
            FXMLLoader loader = new FXMLLoader(resource, bundle);
            Parent root = loader.load();

            CadastroDeCandidatoController controller = loader.getController();

            controller.esconderBotaoEditar();
            controller.prepararParaVisualizacao(selecionado);

            Stage stage = new Stage();
            stage.setTitle(bundle.getString("candidateList.alert.viewTitle"));
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            carregarCandidatos();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarErro(bundle.getString("candidateList.alert.errorLoadView") + " " + e.getMessage());
        }
    }

    @FXML
    private void cadastrarUsuario() throws IOException {
        URL resource = getClass().getResource("/view/CadastroDeCandidato.fxml");
        FXMLLoader loader = new FXMLLoader(resource, bundle);
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle(bundle.getString("candidateList.alert.registerTitle"));
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        carregarCandidatos();
    }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(bundle.getString("userManagement.alert.attention"));
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(bundle.getString("alert.error.reload.title"));
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}