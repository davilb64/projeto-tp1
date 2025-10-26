package app.humanize.controller;

import app.humanize.model.Usuario;
import app.humanize.repository.UsuarioRepository;
import app.humanize.repository.VagaRepository;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class DashboardAdministradorController {
    public Label lblTotalUsuarios;
    public Label lblTotalCandidatos;
    public Label lblVagasAbertas;
    public PieChart chartCandidaturas;
    public BarChart chartFuncionarios;
    public TableView tblNotificacoes;

    private final UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();
    private final VagaRepository vagaRepository = VagaRepository.getInstance();

    public void initialize(){
        lblVagasAbertas.setText(Integer.toString(vagaRepository.getQtdVaga()));
        lblTotalUsuarios.setText(Integer.toString(usuarioRepository.getQtdUsuarios()));
    }
}
