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

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

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

    private ResourceBundle bundle;

    @FXML
    public void initialize() {
        this.bundle = UserSession.getInstance().getBundle();

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
        URL resource = getClass().getResource("/view/GerarRelatorio.fxml");
        if (resource == null) {
            mostrarAlerta(bundle.getString("alert.error.fxmlNotFound.header"), null, null);
            return;
        }

        FXMLLoader loader = new FXMLLoader(resource, bundle);
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle(bundle.getString("generateReport.title"));
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
        mostrarAlerta(
                bundle.getString("reportsAdmin.alert.notImplemented.title"),
                bundle.getString("reportsAdmin.alert.notImplemented.headerXLS"),
                null
        );
    }


    private void exportarRelatorioSelecionado(IReportFormatter formatador) {
        Relatorio registroSelecionado = tblRelatorios.getSelectionModel().getSelectedItem();
        if (registroSelecionado == null) {
            mostrarAlerta(
                    bundle.getString("reportsAdmin.alert.noSelection.title"),
                    bundle.getString("reportsAdmin.alert.noSelection.headerExport"),
                    null
            );
            return;
        }

        IGeradorRelatorio estrategiaRelatorio;

        try {
            switch (registroSelecionado.getTipoRelatorio()) {
                case LISTA_USUARIOS:
                    estrategiaRelatorio = new RelatorioListaUsuarios();
                    break;
                default:
                    throw new IllegalStateException(bundle.getString("reportsAdmin.alert.unsupportedType") + " " + registroSelecionado.getTipoRelatorio());
            }

            if (!estrategiaRelatorio.podeGerar(UserSession.getInstance().getUsuarioLogado())) {
                mostrarAlerta(
                        bundle.getString("reportsAdmin.alert.accessDenied.title"),
                        bundle.getString("reportsAdmin.alert.accessDenied.header"),
                        null
                );
                return;
            }

            ReportData dados = estrategiaRelatorio.coletarDados();
            byte[] arquivoBytes = formatador.formatar(dados);

            salvarArquivoFisico(arquivoBytes,
                    estrategiaRelatorio.getNome().replace(" ", "_") + "_" + registroSelecionado.getId(),
                    formatador);

        } catch (IllegalArgumentException e) {
            mostrarAlerta(
                    bundle.getString("reportsAdmin.alert.paramError.title"),
                    bundle.getString("reportsAdmin.alert.paramError.header"),
                    e.getMessage()
            );
        } catch (Exception e) {
            mostrarAlerta(
                    bundle.getString("reportsAdmin.alert.exportError.title"),
                    bundle.getString("reportsAdmin.alert.exportError.header"),
                    e.getMessage()
            );
            e.printStackTrace();
        }
    }

    @FXML
    private void visualizarRelatorio() {
        Relatorio relatorio = tblRelatorios.getSelectionModel().getSelectedItem();
        if (relatorio == null) {
            mostrarAlerta(
                    bundle.getString("reportsAdmin.alert.noSelection.title"),
                    bundle.getString("reportsAdmin.alert.noSelection.headerView"),
                    null
            );
            return;
        }

        mostrarAlerta(
                bundle.getString("reportsAdmin.alert.notImplemented.title"),
                bundle.getString("reportsAdmin.alert.notImplemented.headerView"),
                null
        );
    }

    @FXML
    private void editarRelatorio() {
        mostrarAlerta(
                bundle.getString("reportsAdmin.alert.notImplemented.title"),
                bundle.getString("reportsAdmin.alert.notImplemented.headerEdit"),
                null
        );
    }

    @FXML
    private void excluirRelatorio() {
        Relatorio relatorioSelecionado = tblRelatorios.getSelectionModel().getSelectedItem();
        if (relatorioSelecionado == null) {
            mostrarAlerta(
                    bundle.getString("reportsAdmin.alert.noSelection.title"),
                    bundle.getString("reportsAdmin.alert.noSelection.headerDelete"),
                    null
            );
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle(bundle.getString("userManagement.alert.confirmDeleteTitle")); // Chave reutilizada
        confirmacao.setHeaderText(bundle.getString("reportsAdmin.alert.confirmDelete.header") + " " + relatorioSelecionado.getId());
        confirmacao.setContentText(bundle.getString("reportsAdmin.alert.confirmDelete.content"));

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.OK) {
                try {
                    relatorioRepo.excluirRelatorio(relatorioSelecionado);
                    carregarHistoricoTabela();
                } catch (IOException e) {
                    mostrarAlerta(
                            bundle.getString("reportsAdmin.alert.deleteError.title"),
                            bundle.getString("reportsAdmin.alert.deleteError.header"),
                            e.getMessage()
                    );
                    e.printStackTrace();
                }
            }
        });
    }


    private File salvarArquivoFisico(byte[] bytes, String nomeBase, IReportFormatter formatador) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("reportsAdmin.saveDialog.title"));
        fileChooser.setInitialFileName(nomeBase + formatador.getExtensao());

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                formatador.getDescricaoFiltro(), "*" + formatador.getExtensao());
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showSaveDialog(tblRelatorios.getScene().getWindow());

        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(bytes);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(bundle.getString("alert.success.title"));
                alert.setHeaderText(bundle.getString("reportsAdmin.alert.exportSuccess.header"));
                alert.setContentText(file.getAbsolutePath());
                alert.showAndWait();

                return file;
            } catch (IOException e) {
                mostrarAlerta(
                        bundle.getString("userRegistration.alert.saveError.title"),
                        bundle.getString("reportsAdmin.alert.saveError.header"),
                        e.getMessage()
                );
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
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