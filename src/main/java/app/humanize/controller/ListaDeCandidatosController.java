package app.humanize.controller;

import app.humanize.model.Candidato;
import app.humanize.model.Vaga;
import app.humanize.model.Candidatura;
import app.humanize.model.StatusVaga;
import app.humanize.model.StatusCandidatura;
import app.humanize.repository.CandidatoRepository;
import app.humanize.repository.VagaRepository;
import app.humanize.repository.CandidaturaRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ListaDeCandidatosController {
    @FXML private TextField txtNome;
    @FXML private TextField txtFormacao;
    @FXML private TextField txtExperiencia;
    @FXML private Button btnFiltrar;
    @FXML private ComboBox <> comboSalario;
    @FXML private  Button btnCadastrar;
    @FXML private TableView<Candidato> tblUsuarios;
    @FXML private TableColumn<Candidato, String> colNome;
    @FXML private TableColumn<Candidato, String> colExperiencia;
    @FXML private TableColumn<Candidato, String> colFormacao;
    @FXML private TableColumn<Candidato, String> colDisponibilidade;
    @FXML private TableColumn<Candidato, String> colPretencao;
    @FXML private Button btnEditarC;
    @FXML private Button btnExcluiC;

    @FXML
    private void editarCandidato(){}

    @FXML
    public void excluirCandidato(){}


}
