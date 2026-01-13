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
    
    private String nomeComprador;
    private String emailComprador;
    private String telefoneComprador;
    private Endereco enderecoEntrega;
    private MetodoPagamento metodoPagamento;

    public Pedido(int id, String loginCliente, List<Produto> itens, double total, String nomeComprador,
    String emailComprador, String telefoneComprador, Endereco enderecoEntrega, MetodoPagamento metodoPagamento) {
        this.id = id;
        this.loginCliente = loginCliente;
        this.itens = itens;
        this.total = total;
        this.dataHora = LocalDateTime.now();
        this.nomeComprador = nomeComprador;
        this.emailComprador = emailComprador;
        this.telefoneComprador = telefoneComprador;
        this.enderecoEntrega = enderecoEntrega;
        this.metodoPagamento = metodoPagamento;
    }

    public int getId() { 
    	return id; 
    }
    
    public String getLoginCliente() { 
    	return loginCliente; 
    }
    
    public List<Produto> getItens() { 
    	return itens; 
    }
    
    public double getTotal() { 
    	return total; 
    }
    
    public LocalDateTime getDataHora() { 
    	return dataHora; 
    }
    
    public String getNomeComprador() { 
    	return nomeComprador; 
    }
    
    public String getEmailComprador() { 
    	return emailComprador; 
    }
    
    public String getTelefoneComprador() { 
    	return telefoneComprador; 
    }
    
    public Endereco getEnderecoEntrega() { 
    	return enderecoEntrega; 
    }
    
    public MetodoPagamento getMetodoPagamento() { 
    	return metodoPagamento; 
    }
}
