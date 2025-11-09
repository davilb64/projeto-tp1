package app.humanize.repository;

import app.humanize.model.FolhaPag;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FolhaPagRepository extends BaseRepository {

    public static final FolhaPagRepository instance = new FolhaPagRepository();
    private static final String NOME_ARQUIVO = "folha_pagamento.csv";
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private FolhaPagRepository() {}

    public static FolhaPagRepository getInstance() {
        return instance;
    }

    public void salvarFolha(FolhaPag folha) throws IOException {
        List<FolhaPag> folhasExistentes = carregarTodasFolhas();
        folhasExistentes.add(folha);
        salvarTodasFolhas(folhasExistentes);
    }

    public List<FolhaPag> carregarTodasFolhas() {
        List<FolhaPag> folhas = new ArrayList<>();
        File arquivo = getArquivoDePersistencia(NOME_ARQUIVO);

        if (!arquivo.exists()) {
            System.out.println("Arquivo " + NOME_ARQUIVO + " não encontrado. Copiando arquivo padrão...");
            try {
                copiarArquivoDefaultDeResources(NOME_ARQUIVO, arquivo);
            } catch (IOException e) {
                System.err.println("!!! FALHA CRÍTICA AO COPIAR ARQUIVO PADRÃO: " + NOME_ARQUIVO);
                e.printStackTrace();
                return folhas;
            }
        }

        try (BufferedReader leitor = new BufferedReader(new FileReader(arquivo))) {
            leitor.readLine(); // Pula header
            String linha;
            while ((linha = leitor.readLine()) != null) {
                String[] campos = linha.split(";", -1);
                if (campos.length >= 10) {
                    String nome = campos[0];
                    String cargo = campos[1];
                    String nivel = campos[2];

                    double salarioBase = parseDoubleComVirgula(campos[3]);
                    double adicionalNivel = parseDoubleComVirgula(campos[4]);
                    double beneficios = parseDoubleComVirgula(campos[5]);
                    double adicionais = parseDoubleComVirgula(campos[6]);
                    double descontos = parseDoubleComVirgula(campos[7]);
                    double salarioLiquido = parseDoubleComVirgula(campos[8]);

                    LocalDate data = parseData(campos[9]);

                    FolhaPag folha = new FolhaPag(nome, cargo, nivel,
                            salarioBase, adicionalNivel, beneficios, adicionais, descontos, salarioLiquido, data);
                    folhas.add(folha);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Erro ao carregar folhas do CSV: " + e.getMessage());
        }

        return folhas;
    }

    private double parseDoubleComVirgula(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(valor.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            System.err.println("Erro ao converter valor: '" + valor + "' - usando 0.0 como padrão");
            return 0.0;
        }
    }

    private LocalDate parseData(String dataStr) {
        if (dataStr == null || dataStr.trim().isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(dataStr.trim(), dateFormatter);
        } catch (Exception e) {
            System.err.println("Erro ao converter data: '" + dataStr + "' - usando data atual");
            return LocalDate.now();
        }
    }

    private void salvarTodasFolhas(List<FolhaPag> folhas) throws IOException {
        File arquivo = getArquivoDePersistencia(NOME_ARQUIVO);
        try (FileWriter escritor = new FileWriter(arquivo, false)) {
            escritor.write("Nome;Cargo;Nivel;SalarioBase;AdicionalNivel;Beneficios;Adicionais;Descontos;SalarioLiquido;Data\n");

            for (FolhaPag folha : folhas) {
                String dataStr = folha.getData() != null ?
                        folha.getData().format(dateFormatter) :
                        LocalDate.now().format(dateFormatter);

                escritor.write(String.format("%s;%s;%s;%.2f;%.2f;%.2f;%.2f;%.2f;%.2f;%s\n",
                        folha.getNome(),
                        folha.getCargo(),
                        folha.getNivel(),
                        folha.getSalarioBase(),
                        folha.getAdicionalNivel(),
                        folha.getBeneficios(),
                        folha.getAdicionais(),
                        folha.getDescontos(),
                        folha.getSalarioLiquido(),
                        dataStr));
            }
        }
    }
}