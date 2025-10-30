package app.humanize.controller;

import app.humanize.model.ContraCheque;
import app.humanize.model.Funcionario;
import app.humanize.repository.ContrachequeRepository;
import app.humanize.repository.UsuarioRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Optional;

public class ContraChequeController {

    @FXML private TextField txtNome;
    @FXML private TextField txtCargo;
    @FXML private TextField txtDepartamento;

    @FXML private TableView<ContraCheque> tabelaContracheque;
    @FXML private TableColumn<ContraCheque, String> colunaEmissao;
    @FXML private TableColumn<ContraCheque, Double> colunaProventos;
    @FXML private TableColumn<ContraCheque, Double> colunaDescon;
    @FXML private TableColumn<ContraCheque, Double> colunaSald;

    private final UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();
    private final ContrachequeRepository contraChequeRepository = ContrachequeRepository.getInstance();

    @FXML
    public void initialize() {
        configurarColunasTabela();
    }

    private void configurarColunasTabela() {
        colunaEmissao.setCellValueFactory(new PropertyValueFactory<>("dataEmissao"));
        colunaProventos.setCellValueFactory(new PropertyValueFactory<>("totalProventos"));
        colunaDescon.setCellValueFactory(new PropertyValueFactory<>("totalDescontos"));
        colunaSald.setCellValueFactory(new PropertyValueFactory<>("saldo"));
    }

    @FXML
    private void exportarContraCheque() {
        String nome = txtNome.getText().trim();
        String cargo = txtCargo.getText().trim();
        String departamento = txtDepartamento.getText().trim();

        if (nome.isEmpty() && cargo.isEmpty() && departamento.isEmpty()) {
            mostrarAlerta("Campos Vazios", "Preencha pelo menos um campo para buscar.");
            return;
        }

        Optional<Funcionario> funcionarioOpt = usuarioRepository.getTodosUsuarios().stream()
                .filter(usuario -> usuario instanceof Funcionario)
                .map(usuario -> (Funcionario) usuario)
                .filter(func ->
                        (nome.isEmpty() || func.getNome().equalsIgnoreCase(nome)) &&
                                (cargo.isEmpty() || (func.getCargo() != null && func.getCargo().equalsIgnoreCase(cargo))) &&
                                (departamento.isEmpty() || (func.getDepartamento() != null && func.getDepartamento().equalsIgnoreCase(departamento)))
                )
                .findFirst();

        if (funcionarioOpt.isPresent()) {
            Funcionario funcionario = funcionarioOpt.get();
            carregarContraCheques(funcionario.getNome());
        } else {
            tabelaContracheque.setItems(FXCollections.observableArrayList());
            mostrarAlerta("Funcionário não encontrado",
                    "Nenhum funcionário encontrado com os filtros informados.");
        }
    }

    private void carregarContraCheques(String nomeFuncionario) {
        List<ContraCheque> contraChequesDoFuncionario = contraChequeRepository.carregarContraChequesPorFuncionario(nomeFuncionario);
        ObservableList<ContraCheque> dados = FXCollections.observableArrayList(contraChequesDoFuncionario);
        tabelaContracheque.setItems(dados);

        if (contraChequesDoFuncionario.isEmpty()) {
            mostrarAlerta("ContraCheques", "Nenhum contracheque encontrado para: " + nomeFuncionario);
        }
    }

    @FXML
    private void imprimirContraCheque() {
        // vazio por enquanto
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}