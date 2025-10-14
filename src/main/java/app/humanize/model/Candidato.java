package app.humanize.model;

import java.time.LocalDate;

public class Candidato extends Pessoa{
    private String formacao;
    private String experiencia;
    private double pretencaoSalarial;
    private String disponibilidade;
    private LocalDate dataCadastro;

    //construtores

    public Candidato(String nome, String cpf, Endereco endereco, String email, String formacao, String experiencia, double pretencaoSalarial, String disponibilidade, LocalDate dataCadastro) {
        super(nome, cpf, endereco, email);
        this.formacao = formacao;
        this.experiencia = experiencia;
        this.pretencaoSalarial = pretencaoSalarial;
        this.disponibilidade = disponibilidade;
        this.dataCadastro = dataCadastro;
    }


    public Candidato() {
    }

    //metodos especiais


    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public String getDisponibilidade() {
        return disponibilidade;
    }

    public void setDisponibilidade(String disponibilidade) {
        this.disponibilidade = disponibilidade;
    }

    public double getPretencaoSalarial() {
        return pretencaoSalarial;
    }

    public void setPretencaoSalarial(double pretencaoSalarial) {
        this.pretencaoSalarial = pretencaoSalarial;
    }

    public String getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(String experiencia) {
        this.experiencia = experiencia;
    }

    public String getFormacao() {
        return formacao;
    }

    public void setFormacao(String formacao) {
        this.formacao = formacao;
    }
}