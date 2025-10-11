package app.humanize.model;

import java.time.LocalDate;

public class Candidato extends Pessoa {
    private String formacao;
    private String experiencia;
    private double pretensaoSalarial;
    private String disponibilidade;
    private LocalDate dataCadastro;

    //construtores
    public Candidato(int id, String nome, String cpf, Endereco endereco, String email, String formacao, String experiencia, double pretensaoSalarial, String disponibilidade, LocalDate dataCadastro) {
        super(nome, cpf, endereco, email);
        this.formacao = formacao;
        this.experiencia = experiencia;
        this.pretensaoSalarial = pretensaoSalarial;
        this.disponibilidade = disponibilidade;
        this.dataCadastro = dataCadastro;
    }
    public Candidato() {}

    //metodos especiais

    public String getFormacao() {
        return formacao;
    }

    public void setFormacao(String formacao) {
        this.formacao = formacao;
    }

    public String getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(String experiencia) {
        this.experiencia = experiencia;
    }

    public double getPretensaoSalarial() {
        return pretensaoSalarial;
    }

    public void setPretensaoSalarial(double pretensaoSalarial) {
        this.pretensaoSalarial = pretensaoSalarial;
    }

    public String getDisponibilidade() {
        return disponibilidade;
    }

    public void setDisponibilidade(String disponibilidade) {
        this.disponibilidade = disponibilidade;
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}
