

//to pondo isso pra poder testar, se apagar me avisa :) -kiki


package app.humanize.model;

public class Vaga {
    private String nome;

    // Construtor
    public Vaga(String nome) {
        this.nome = nome;
    }

    // Getter e Setter
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    // toString() define o texto exibido no ChoiceBox
    @Override
    public String toString() {
        return nome;
    }
}
