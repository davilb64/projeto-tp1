package app.humanize.controller;

import app.humanize.model.Candidato;
import app.humanize.repository.CandidatoRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ListaDeCandidatosController {

    @FXML private TextField txtNome;
    @FXML private TextField txtFormacao;
    @FXML private TextField txtExperiencia;
    @FXML private ComboBox<String> comboSalario;
    @FXML private Button btnFiltrar;
    @FXML private Button btnCadastrar;
    @FXML private TableView<Candidato> tblUsuarios;
    @FXML private TableColumn<Candidato, String> colNome;
    @FXML private TableColumn<Candidato, String> colExperiencia;
    @FXML private TableColumn<Candidato, String> colFormacao;
    @FXML private TableColumn<Candidato, String> colDisponibilidade;
    @FXML private TableColumn<Candidato, Double> colPretencao;

    private final CandidatoRepository candidatoRepository = CandidatoRepository.getInstance();
    private final ObservableList<Candidato> listaCandidatos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        configurarTabela();
        carregarCandidatos();
        configurarComboSalario();
    }

    /** Configura as colunas da tabela */
    private void configurarTabela() {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colExperiencia.setCellValueFactory(new PropertyValueFactory<>("experiencia"));
        colFormacao.setCellValueFactory(new PropertyValueFactory<>("formacao"));
        colDisponibilidade.setCellValueFactory(new PropertyValueFactory<>("disponibilidade"));
        colPretencao.setCellValueFactory(new PropertyValueFactory<>("pretencaoSalarial"));

        tblUsuarios.setItems(listaCandidatos);
    }

    /** Carrega os candidatos do repositório para a tabela */
    private void carregarCandidatos() {
        listaCandidatos.clear();
        listaCandidatos.addAll(candidatoRepository.getTodos());
    }

    /** Preenche o ComboBox com intervalos salariais */
    private void configurarComboSalario() {
        comboSalario.setItems(FXCollections.observableArrayList(
                "Todos",
                "Até 2.000",
                "2.000 - 4.000",
                "4.000 - 6.000",
                "Acima de 6.000"
        ));
        comboSalario.getSelectionModel().select("Todos");
    }

    /** Filtro por nome, formação, experiência e faixa salarial */
    @FXML
    private void filtra() {
        String nomeFiltro = txtNome.getText().toLowerCase();
        String formacaoFiltro = txtFormacao.getText().toLowerCase();
        String experienciaFiltro = txtExperiencia.getText().toLowerCase();
        String faixaSalario = comboSalario.getValue();

        List<Candidato> filtrados = candidatoRepository.getTodos().stream()
                .filter(c ->
                        (safeString(c.getNome()).toLowerCase().contains(nomeFiltro) || safeString(c.getEmail()).toLowerCase().contains(nomeFiltro)) &&
                                safeString(c.getFormacao()).toLowerCase().contains(formacaoFiltro) &&
                                safeString(c.getExperiencia()).toLowerCase().contains(experienciaFiltro) &&
                                dentroDaFaixaSalarial(c.getPretencaoSalarial(), faixaSalario)
                )
                .collect(Collectors.toList());

        listaCandidatos.setAll(filtrados);
    }

    private String safeString(String s) {
        return s == null ? "" : s;
    }


    private boolean dentroDaFaixaSalarial(double salario, String faixa) {
        return switch (faixa) {
            case "Até 2.000" -> salario <= 2000;
            case "2.000 - 4.000" -> salario > 2000 && salario <= 4000;
            case "4.000 - 6.000" -> salario > 4000 && salario <= 6000;
            case "Acima de 6.000" -> salario > 6000;
            default -> true; // "Todos"
        };
    }

    /** Ação do botão Editar (pode abrir uma tela de edição no futuro) */
    @FXML
    private void editarCandidato() {
        Candidato selecionado = tblUsuarios.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAlerta("Selecione um candidato para editar.");
            return;
        }

        try {
            // Carrega o FXML de cadastro
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CadastroDeCandidato.fxml"));
            Parent root = loader.load();

            // Obtém o controller da tela de cadastro
            CadastroDeCandidatoController controller = loader.getController();

            // Passa o candidato selecionado para edição
            controller.prepararParaEdicao(selecionado);

            // Abre em uma nova janela modal
            Stage stage = new Stage();
            stage.setTitle("Editar Candidato");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Atualiza a tabela após fechar a janela
            carregarCandidatos();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir a tela de edição: " + e.getMessage());
        }
    }


    /** Ação do botão Excluir — remove da tabela e do CSV */
    @FXML
    private void excluirCandidato() {
        Candidato selecionado = tblUsuarios.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAlerta("Selecione um candidato para excluir.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmação");
        confirmacao.setHeaderText("Excluir candidato?");
        confirmacao.setContentText("Tem certeza que deseja excluir " + selecionado.getNome() + "?");

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.OK) {
                try {
                    candidatoRepository.remover(selecionado);
                    listaCandidatos.remove(selecionado);
                    mostrarInfo("Candidato excluído com sucesso!", selecionado.getNome());
                } catch (IOException e) {
                    mostrarErro("Erro ao excluir candidato: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void visualizarCandidato(){
        Candidato selecionado = tblUsuarios.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAlerta("Selecione um candidato para vizualizar.");
            return;
        }

        try {
            // Carrega o FXML de cadastro
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CadastroDeCandidato.fxml"));
            Parent root = loader.load();

            // Obtém o controller da tela de cadastro
            CadastroDeCandidatoController controller = loader.getController();

            // Passa o candidato selecionado para edição
            controller.esconderBotaoEditar();
            controller.prepararParaVisualizacao(selecionado);

            // Abre em uma nova janela modal
            Stage stage = new Stage();
            stage.setTitle("Vizualizar Candidato");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Atualiza a tabela após fechar a janela
            carregarCandidatos();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir a tela de vizualização: " + e.getMessage());
        }
    }

    @FXML
    private void cadastrarUsuario() throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CadastroDeCandidato.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Cadastrar Candidato");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        carregarCandidatos();

    }


    /** Utilitários de alerta */
    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atenção");
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
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
