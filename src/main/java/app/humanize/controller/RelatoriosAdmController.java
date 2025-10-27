package app.humanize.controller;

import app.humanize.model.Relatorio;
import app.humanize.model.TipoRelatorio;
import app.humanize.model.Usuario;
import app.humanize.repository.RelatorioRepository;
import app.humanize.repository.UsuarioRepository;
import app.humanize.service.formatters.CsvFormatter;
import app.humanize.service.formatters.IReportFormatter;
import app.humanize.service.formatters.PdfFormatter;
import app.humanize.service.relatorios.IGeradorRelatorio;
import app.humanize.service.relatorios.RelatorioListaUsuarios;
import app.humanize.service.relatorios.ReportData;
import app.humanize.util.UserSession;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop; // For opening file
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public class RelatoriosAdmController {

    @FXML private TableView<Relatorio> tblRelatorios;
    @FXML private TableColumn<Relatorio, Integer> colId;
    @FXML private TableColumn<Relatorio, TipoRelatorio> colTipoRelatorio;
    @FXML private TableColumn<Relatorio, LocalDate> colDataGeracao;
    @FXML private TableColumn<Relatorio, Usuario> colResponsavel;

    private final RelatorioRepository relatorioRepo = RelatorioRepository.getInstance();
    private final UsuarioRepository usuarioRepo = UsuarioRepository.getInstance();

    private final IReportFormatter formatadorCsv = new CsvFormatter();
    private final IReportFormatter formatadorPdf = new PdfFormatter();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTipoRelatorio.setCellValueFactory(new PropertyValueFactory<>("tipoRelatorio"));
        colDataGeracao.setCellValueFactory(new PropertyValueFactory<>("dataGeracao"));
        colResponsavel.setCellValueFactory(new PropertyValueFactory<>("responsavel"));

        colResponsavel.setCellFactory(column -> new TableCell<Relatorio, Usuario>() {
            @Override
            protected void updateItem(Usuario item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome());
            }
        });

        carregarHistoricoTabela();
    }

    private void carregarHistoricoTabela() {
        tblRelatorios.setItems(FXCollections.observableArrayList(relatorioRepo.getTodosRelatorios()));
        tblRelatorios.refresh();
    }

    @FXML
    private void gerarRelatorio() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GerarRelatorio.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Gerar Novo Relatório");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(tblRelatorios.getScene().getWindow());
        stage.showAndWait();
        carregarHistoricoTabela();
    }

    @FXML
    private void exportarPDF() {
        exportarRelatorioSelecionado(formatadorPdf);
    }

    @FXML
    private void exportarCSV() {
        exportarRelatorioSelecionado(formatadorCsv);
    }

    @FXML
    private void exportarXLS() {
        mostrarAlerta("Funcionalidade Indisponível", "Exportação para XLS ainda não implementada.", null);
    }


    private void exportarRelatorioSelecionado(IReportFormatter formatador) {
        Relatorio registroSelecionado = tblRelatorios.getSelectionModel().getSelectedItem();
        if (registroSelecionado == null) {
            mostrarAlerta("Seleção Inválida", "Selecione um relatório na tabela para exportar.", null);
            return;
        }

        IGeradorRelatorio estrategiaRelatorio;

        try {
            switch (registroSelecionado.getTipoRelatorio()) {
                case LISTA_USUARIOS:
                    estrategiaRelatorio = new RelatorioListaUsuarios();
                    break;

                default:
                    throw new IllegalStateException("Tipo de relatório não implementado para exportação: " + registroSelecionado.getTipoRelatorio());
            }

            if (!estrategiaRelatorio.podeGerar(UserSession.getInstance().getUsuarioLogado())) {
                mostrarAlerta("Acesso Negado", "Você não tem permissão para gerar este tipo de relatório.", null);
                return;
            }

            ReportData dados = estrategiaRelatorio.coletarDados();
            byte[] arquivoBytes = formatador.formatar(dados);

            salvarArquivoFisico(arquivoBytes,
                    estrategiaRelatorio.getNome().replace(" ", "_") + "_" + registroSelecionado.getId(),
                    formatador);

        } catch (IllegalArgumentException e) {
            mostrarAlerta("Erro nos Parâmetros", "Não foi possível recriar o relatório.", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta("Erro ao Exportar", "Não foi possível gerar o arquivo.", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void visualizarRelatorio() {
        Relatorio relatorio = tblRelatorios.getSelectionModel().getSelectedItem();
        if (relatorio == null) {
            mostrarAlerta("Seleção Inválida", "Selecione um relatório na tabela para visualizar.", null);
            return;
        }

        mostrarAlerta("Funcionalidade Indisponível", "Visualização direta ainda não implementada. Use Exportar.", null);
    }

    @FXML
    private void editarRelatorio() {
        mostrarAlerta("Funcionalidade Indisponível", "Edição de registros de relatório não é suportada.", null);

    }

    @FXML
    private void excluirRelatorio() {
        Relatorio relatorioSelecionado = tblRelatorios.getSelectionModel().getSelectedItem();
        if (relatorioSelecionado == null) {
            mostrarAlerta("Seleção Inválida", "Selecione um relatório na tabela para excluir.", null);
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Exclusão");
        confirmacao.setHeaderText("Excluir registro do relatório ID: " + relatorioSelecionado.getId());
        confirmacao.setContentText("Você tem certeza que deseja excluir este registro? O arquivo físico (se existir) não será apagado.");

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.OK) {
                try {
                    relatorioRepo.excluirRelatorio(relatorioSelecionado);
                    carregarHistoricoTabela(); // Refresh after deletion
                } catch (IOException e) {
                    mostrarAlerta("Erro ao Excluir", "Não foi possível excluir o registro do relatório.", e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }


    private File salvarArquivoFisico(byte[] bytes, String nomeBase, IReportFormatter formatador) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Relatório");
        fileChooser.setInitialFileName(nomeBase + formatador.getExtensao());

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                formatador.getDescricaoFiltro(), "*" + formatador.getExtensao());
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showSaveDialog(tblRelatorios.getScene().getWindow());

        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(bytes);
                mostrarAlerta("Sucesso", "Relatório exportado com sucesso!", file.getAbsolutePath());
                return file;
            } catch (IOException e) {
                mostrarAlerta("Erro de Salvamento", "Não foi possível salvar o arquivo.", e.getMessage());
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    private void mostrarAlerta(String titulo, String cabecalho, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        if ("Sucesso".equals(titulo)) {
            alert.setAlertType(Alert.AlertType.INFORMATION);
        }
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(conteudo);
        alert.showAndWait();
    }
}