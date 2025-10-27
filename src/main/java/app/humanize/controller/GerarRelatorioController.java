package app.humanize.controller;

import app.humanize.model.Relatorio;
import app.humanize.model.TipoRelatorio;
import app.humanize.model.Usuario;
import app.humanize.repository.RelatorioRepository;
import app.humanize.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter; // For better date formatting

public class GerarRelatorioController {
    @FXML private Label lblId;
    @FXML private Label lblData;
    @FXML private Label lblUser;
    @FXML private ComboBox<TipoRelatorio> tipoCombo;

    private Usuario usuarioLogado;
    private final RelatorioRepository relatorioRepository = RelatorioRepository.getInstance();

    @FXML public void initialize(){
        usuarioLogado = UserSession.getInstance().getUsuarioLogado();

        if (relatorioRepository != null) {
            lblId.setText(String.valueOf(relatorioRepository.getProximoId()));
        } else {
            lblId.setText("Erro");
            System.err.println("Erro: RelatorioRepository não foi inicializado!");
        }

        lblData.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        if (usuarioLogado != null) {
            lblUser.setText(usuarioLogado.getNome());
        } else {
            lblUser.setText("Usuário Desconhecido");
            System.err.println("Erro: Nenhum usuário logado na sessão ao abrir GerarRelatorioController!");
            mostrarAlerta("Erro Crítico", "Nenhum usuário logado.", "Não é possível gerar relatórios sem um usuário logado.");
            return;
        }

        if (tipoCombo != null) {
            tipoCombo.getItems().setAll(TipoRelatorio.values());
        } else {
            System.err.println("Erro: ComboBox tipoCombo não foi injetado pelo FXML!");
        }
    }

    @FXML public void cancelar(){
        fecharJanela();
    }

    @FXML public void salvarRegistroRelatorio(){
        TipoRelatorio tipoSelecionado = tipoCombo.getValue();

        if (tipoSelecionado == null) {
            mostrarAlerta("Seleção Inválida", "Por favor, selecione um Tipo de Relatório.", null);
            return;
        }
        if (usuarioLogado == null) {
            mostrarAlerta("Erro Crítico", "Nenhum usuário logado.", "Não é possível salvar o registro.");
            return;
        }


        try{
            Relatorio novoRegistro = new Relatorio();
            novoRegistro.setId(Integer.parseInt(lblId.getText()));
            novoRegistro.setTipoRelatorio(tipoSelecionado);
            novoRegistro.setDataGeracao(LocalDate.now());
            novoRegistro.setResponsavel(usuarioLogado);

            switch (tipoSelecionado){
                case LISTA_USUARIOS:
                    break;
                default:
                    System.out.println("Parâmetros para " + tipoSelecionado + " ainda não implementados.");
                    break;
            }

            relatorioRepository.escreverRelatorioNovo(novoRegistro);
            System.out.println("Registro de relatório salvo com sucesso.");
            fecharJanela();

        } catch (IOException e) {
            mostrarAlerta("Erro de Salvamento", "Não foi possível salvar o registro do relatório.", e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e){
            mostrarAlerta("Erro Interno", "ID do relatório inválido.", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta("Erro Inesperado", "Ocorreu um erro ao salvar o registro.", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void fecharJanela() {
        if (lblId != null && lblId.getScene() != null && lblId.getScene().getWindow() != null) {
            Stage stage = (Stage) lblId.getScene().getWindow();
            stage.close();
        } else {
            System.err.println("Erro ao fechar janela: Componente FXML não inicializado corretamente.");
        }
    }

    private void mostrarAlerta(String titulo, String cabecalho, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(conteudo);
        alert.showAndWait();
    }
}