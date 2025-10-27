package app.humanize.controller;

import app.humanize.model.Funcionario;
import app.humanize.model.Usuario;
import app.humanize.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class FinanceiroUsuarioController {


    @FXML private Label lblTitulo;
    @FXML private Label lblNome;
    @FXML private Label lblCargo;
    @FXML private Label lblMatricula;
    @FXML private Label lblRegime;
    @FXML private TableView tblContracheques;
    @FXML private TableColumn colMesAno;
    @FXML private TableColumn colSalarioBruto;
    @FXML private TableColumn colDescontos;
    @FXML private TableColumn colSalarioLiquido;
    @FXML private TableColumn colAcoesContracheque;
    @FXML private VBox beneficiosContainer;
    @FXML private Label lblVT;
    @FXML private Label lblVA;
    @FXML private Label lblPlanoSaude;
    @FXML private Label lblOutrosBeneficios;
    @FXML private LineChart chartHistorico;

    Usuario usuarioLogado = UserSession.getInstance().getUsuarioLogado();

    public void initialize(){
        lblTitulo.setText("Financeiro - "+usuarioLogado.getNome());
        lblNome.setText(usuarioLogado.getNome());
        Funcionario funcionarioLogado = (Funcionario)usuarioLogado;
        lblMatricula.setText(String.valueOf(funcionarioLogado.getMatricula()));
        lblCargo.setText(funcionarioLogado.getCargo());
        lblRegime.setText(String.valueOf(funcionarioLogado.getRegime()));
    }

}
