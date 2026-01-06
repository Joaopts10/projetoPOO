package modelo;

import java.io.Serializable;

public class Produto implements Serializable {
    private static final long serialVersionUID = 1L;

    private static int contadorIds = 1;

    private int id;
    private String nome;
    private double preco;
    private int estoque;

    public Produto(String nome, double preco, int estoque) {
        this.id = contadorIds++;
        this.nome = nome;
        this.preco = preco;
        this.estoque = estoque;
    }

    public static void setContador(int valor) {
        contadorIds = valor;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }
    public int getEstoque() { return estoque; }
    public void setEstoque(int estoque) { this.estoque = estoque; }

    @Override
    public String toString() {
        return id + " | " + nome + " | R$" + String.format("%.2f", preco) + " | Estoque: " + estoque;
    }
}