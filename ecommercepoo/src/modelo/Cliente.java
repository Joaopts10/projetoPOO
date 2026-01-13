package modelo;

import java.util.ArrayList;

public class Cliente extends Usuario {
    private static final long serialVersionUID = 1L;

    private ArrayList<ItemCarrinho> carrinho;

    public Cliente(String login, String senha, String nome) {
        super(login, senha, nome);
        this.carrinho = new ArrayList<>();
    }

    @Override
    public boolean isAdmin() {
        return false;
    }

    public ArrayList<ItemCarrinho> getCarrinho() {
        return carrinho;
    }

    public void adicionarAoCarrinho(Produto produto, int quantidade) {
        for (ItemCarrinho item : carrinho) {
            if (item.getProduto().getId() == produto.getId()) {
                item.setQuantidade(item.getQuantidade() + quantidade);
                return;
            }
        }
        carrinho.add(new ItemCarrinho(produto, quantidade));
    }
    
    public int getQuantidadeNoCarrinho(int idProduto) {
        for (ItemCarrinho item : carrinho) {
            if (item.getProduto().getId() == idProduto) {
                return item.getQuantidade();
            }
        }
        return 0;
    }

    public void removerDoCarrinho(int indice) {
        if (indice >= 0 && indice < carrinho.size()) {
            carrinho.remove(indice);
        }
    }

    public void limparCarrinho() {
        this.carrinho.clear();
    }
    
    public double getTotalCarrinhoAtualizado(java.util.function.Function<Integer, modelo.Produto> buscarProduto) {
        double total = 0.0;
        for (ItemCarrinho item : carrinho) {
            modelo.Produto produtoAtualizado = buscarProduto.apply(item.getProduto().getId());
            if (produtoAtualizado != null) {
                total += produtoAtualizado.getPreco() * item.getQuantidade();
            } else {
                total += item.getSubtotal();
            }
        }
        return total;
    }
}