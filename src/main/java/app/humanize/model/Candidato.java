package app.humanize.model;

import java.time.LocalDate;

public class Candidato extends Pessoa{
    private String formacao;
    private String experiencia;
    private double pretencaoSalarial;
    private String disponibilidade;
    private String telefone;
    private LocalDate dataCadastro;
    private Vaga vaga;
    private String caminhoDocumento;

    //construtores

    private Candidato(CandidatoBuilder builder) {
        super(builder.nome, builder.cpf, builder.endereco, builder.email);
        this.formacao = builder.formacao;
        this.experiencia = builder.experiencia;
        this.pretencaoSalarial = builder.pretencaoSalarial;
        this.disponibilidade = builder.disponibilidade;
        this.dataCadastro = builder.dataCadastro;
        this.vaga = builder.vaga;
        this.telefone = builder.telefone;
    }

    public Candidato() {
    }

    //builder

    public static class CandidatoBuilder {
        private String formacao;
        private String experiencia;
        private double pretencaoSalarial;
        private String disponibilidade;
        private String telefone;
        private LocalDate dataCadastro;
        private Vaga vaga;
        //campos de pessoa
        private String nome;
        private String cpf;
        private String email;
        private Endereco endereco;

        public CandidatoBuilder nome(String nome) {
            this.nome = nome;
            return this;
        }
        public CandidatoBuilder cpf(String cpf) {
            this.cpf = cpf;
            return this;
        }
        public CandidatoBuilder email(String email) {
            this.email = email;
            return this;
        }
        public CandidatoBuilder endereco(Endereco endereco) {
            this.endereco = endereco;
            return this;
        }

        public CandidatoBuilder formacao(String formacao) {
            this.formacao = formacao;
            return this;
        }
        public CandidatoBuilder disponibilidade(String disponibilidade) {
            this.disponibilidade = disponibilidade;
            return this;
        }
        public CandidatoBuilder dataCadastro(LocalDate dataCadastro) {
            this.dataCadastro = dataCadastro;
            return this;
        }
        public CandidatoBuilder telefone(String telefone) {
            this.telefone = telefone;
            return this;
        }
        public CandidatoBuilder pretencaoSalarial(double pretencao) {
            this.pretencaoSalarial = pretencao;
            return this;
        }
        public CandidatoBuilder experiencia(String experiencia) {
            this.experiencia = experiencia;
            return this;
        }
        public CandidatoBuilder vaga(Vaga vaga) {
            this.vaga = vaga;
            return this;
        }
        public Candidato build() {
            return new Candidato(this);
        }
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

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getTelefone() {
        return telefone;
    }
    public String getCaminhoDocumento() {
        return caminhoDocumento;
    }

    public void setCaminhoDocumento(String caminhoDocumento) {
        this.caminhoDocumento = caminhoDocumento;
    }

    public void setVaga(Vaga vaga) {
        this.vaga = vaga;
    }
    public Vaga getVaga() {
        return vaga;
    }

    @Override
    public String toString() {
        return this.getNome();
    }
}