package modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Pedido implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String loginCliente;
    private List<Produto> itens;
    private double total;
    private LocalDateTime dataHora;

    public Pedido(int id, String loginCliente, List<Produto> itens, double total) {
        this.id = id;
        this.loginCliente = loginCliente;
        this.itens = itens;
        this.total = total;
        this.dataHora = LocalDateTime.now();
    }

    public int getId() { return id; }
    public String getLoginCliente() { return loginCliente; }
    public List<Produto> getItens() { return itens; }
    public double getTotal() { return total; }
    public LocalDateTime getDataHora() { return dataHora; }
}
