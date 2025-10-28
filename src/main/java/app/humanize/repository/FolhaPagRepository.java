package app.humanize.repository;

import app.humanize.model.FolhaPag;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FolhaPagRepository {

    public static final FolhaPagRepository instance = new FolhaPagRepository();
    private final String arquivoCsv = "./src/main/resources/folha_pagamento.csv";

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
        File arquivo = new File(arquivoCsv);

        if (!arquivo.exists()) {
            return folhas;
        }

        try (BufferedReader leitor = new BufferedReader(new FileReader(arquivo))) {
            leitor.readLine(); // Pula header
            String linha;
            while ((linha = leitor.readLine()) != null) {
                String[] campos = linha.split(";", -1);
                if (campos.length >= 9) {
                    String nome = campos[0];
                    String cargo = campos[1];
                    String nivel = campos[2];

                    double salarioBase = parseDoubleComVirgula(campos[3]);
                    double adicionalNivel = parseDoubleComVirgula(campos[4]);
                    double beneficios = parseDoubleComVirgula(campos[5]);
                    double adicionais = parseDoubleComVirgula(campos[6]);
                    double descontos = parseDoubleComVirgula(campos[7]);
                    double salarioLiquido = parseDoubleComVirgula(campos[8]);

                    FolhaPag folha = new FolhaPag(nome, cargo, nivel,
                            salarioBase, adicionalNivel, beneficios, adicionais, descontos, salarioLiquido);
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

    private void salvarTodasFolhas(List<FolhaPag> folhas) throws IOException {
        try (FileWriter escritor = new FileWriter(arquivoCsv, false)) {
            escritor.write("Nome;Cargo;Nivel;SalarioBase;AdicionalNivel;Beneficios;Adicionais;Descontos;SalarioLiquido\n");

            for (FolhaPag folha : folhas) {
                escritor.write(String.format("%s;%s;%s;%.2f;%.2f;%.2f;%.2f;%.2f;%.2f\n",
                        folha.getNome(),
                        folha.getCargo(),
                        folha.getNivel(),
                        folha.getSalarioBase(),
                        folha.getAdicionalNivel(),
                        folha.getBeneficios(),
                        folha.getAdicionais(),
                        folha.getDescontos(),
                        folha.getSalarioLiquido()));
            }
        }
    }
}