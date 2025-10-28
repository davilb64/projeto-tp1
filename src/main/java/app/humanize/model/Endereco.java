package app.humanize.model;

import app.humanize.util.EstadosBrasileiros;

public class Endereco {
    private String logradouro;
    private int numero;
    private String bairro;
    private String cidade;
    private EstadosBrasileiros estado;
    private String cep;

    //construtores

private Endereco(EnderecoBuilder builder) {
        this.logradouro = builder.logradouro;
        this.cep = builder.cep;
        this.bairro = builder.bairro;
        this.numero = builder.numero;
        this.cidade = builder.cidade;
        this.estado = builder.estado;
    }

    public static class EnderecoBuilder {
        private String logradouro;
        private int numero;
        private String bairro;
        private String cidade;
        private EstadosBrasileiros estado;
        private String cep;

        public EnderecoBuilder logradouro(String logradouro) {
            this.logradouro = logradouro;
            return this;
        }

        public EnderecoBuilder numero(int numero) {
            this.numero = numero;
            return this;
        }

        public EnderecoBuilder bairro(String bairro) {
            this.bairro = bairro;
            return this;
        }

        public EnderecoBuilder cidade(String cidade) {
            this.cidade = cidade;
            return this;
        }

        public EnderecoBuilder estado(EstadosBrasileiros estado) {
            this.estado = estado;
            return this;
        }

        public EnderecoBuilder cep(String cep) {
            this.cep = cep;
            return this;
        }

        public Endereco build() {
            return new Endereco(this);
        }
    }



    //metodos especiais

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public EstadosBrasileiros getEstado() {
        return estado;
    }

    public void setEstado(EstadosBrasileiros estado) {
        this.estado = estado;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    @Override
    public String toString() {
        return "Endereco{" +
                "logradouro='" + logradouro + '\'' +
                ", numero=" + numero +
                ", bairro='" + bairro + '\'' +
                ", cidade='" + cidade + '\'' +
                ", estado='" + estado + '\'' +
                ", cep='" + cep + '\'' +
                '}';
    }

    public String enderecoReduzido(){
        return logradouro + ", " + numero + ", " + bairro + ", " + cidade + " - " + estado;
    }


}
