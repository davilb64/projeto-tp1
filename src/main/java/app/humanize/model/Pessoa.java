package app.humanize.model;

public abstract class Pessoa {
    private static int idCounter = 0;
    private final int id; // ID deve ser final após a criação
    private String nome;
    private String cpf;
    private String email;
    private Endereco endereco;

    // Construtor
    public Pessoa(String nome, String cpf, Endereco endereco, String email) {
        this.id = ++idCounter;
        this.nome = nome;
        this.cpf = cpf;
        this.endereco = endereco;
        this.email = email;
    }

    public Pessoa() {
        this.id = ++idCounter;
    }

    // metodos especiais

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }
}