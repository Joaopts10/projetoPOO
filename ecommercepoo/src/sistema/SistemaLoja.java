package sistema;

import dados.GerenciadorDados;
import modelo.*;
import excecoes.EstoqueInsuficienteException;

import java.util.ArrayList;

public class SistemaLoja {

    private ArrayList<Usuario> usuarios;
    private ArrayList<Produto> produtos;
    private ArrayList<Pedido> pedidos;

    private final String ARQUIVO_USUARIOS = "usuarios.ser";
    private final String ARQUIVO_PRODUTOS = "produtos.ser";
    private final String ARQUIVO_PEDIDOS = "pedidos.ser";
    
    private static int contadorPedidos = 1;

    public SistemaLoja() {
        this.usuarios = new ArrayList<>();
        this.produtos = new ArrayList<>();
        this.pedidos = new ArrayList<>();
        carregarDados();

        if (usuarios.isEmpty()) {
            cadastrarUsuario(new Admin("admin", "admin", "Administrador Principal"));
        }
    }

    private void carregarDados() {
        ArrayList<Object> objsUsers = GerenciadorDados.carregarObjetos(ARQUIVO_USUARIOS);
        for (Object o : objsUsers) {
            Usuario u = (Usuario) o;
            this.usuarios.add(u);
        }

        ArrayList<Object> objsProds = GerenciadorDados.carregarObjetos(ARQUIVO_PRODUTOS);
        int maiorId = 0;
        for (Object o : objsProds) {
            Produto p = (Produto) o;
            this.produtos.add(p);
            if (p.getId() > maiorId) {
                maiorId = p.getId();
            }
        }
        Produto.setContador(maiorId + 1);
        
        ArrayList<Object> objsPedidos = GerenciadorDados.carregarObjetos(ARQUIVO_PEDIDOS);
        int maiorIdPedido = 0;
        for (Object o : objsPedidos) {
            Pedido ped = (Pedido) o;
            this.pedidos.add(ped);
            if (ped.getId() > maiorIdPedido) {
                maiorIdPedido = ped.getId();
            }
        }
        contadorPedidos = maiorIdPedido + 1;
        
        sincronizarCarrinhos();
    }

    public Usuario fazerLogin(String login, String senha) {
        for (Usuario u : usuarios) {
            if (u.getLogin().equals(login) && u.autenticar(senha)) {
                if (u instanceof Cliente) {
                    Cliente cliente = (Cliente) u;
                    sincronizarCarrinhoCliente(cliente);
                    return cliente;
                }
                return u;
            }
        }
        return null;
    }
    
    private void sincronizarCarrinhoCliente(Cliente cliente) {
        boolean carrinhoModificado = false;
        
        java.util.Iterator<modelo.ItemCarrinho> iterator = cliente.getCarrinho().iterator();
        while (iterator.hasNext()) {
            modelo.ItemCarrinho item = iterator.next();
            Produto produtoAtual = buscarProdutoPorId(item.getProduto().getId());
            
            if (produtoAtual == null) {
                iterator.remove();
                carrinhoModificado = true;
            } else {
                item.getProduto().setNome(produtoAtual.getNome());
                item.getProduto().setPreco(produtoAtual.getPreco());
                item.getProduto().setEstoque(produtoAtual.getEstoque());
            }
        }
        
        if (carrinhoModificado) {
            salvarCarrinhoCliente(cliente);
        }
    }

    public void logout() {
    }

    public void cadastrarUsuario(Usuario u) {
        for (Usuario usuario : usuarios) {
            if (usuario.getLogin().equals(u.getLogin())) {
                throw new IllegalArgumentException("Login já existe!");
            }
        }
        this.usuarios.add(u);
        if (!GerenciadorDados.salvarObjeto(u, ARQUIVO_USUARIOS)) {
            this.usuarios.remove(u);
            throw new IllegalStateException("Falha ao salvar usuários.");
        }
    }

    public void cadastrarProduto(Produto p) {
        if (p.getNome() == null || p.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do produto não pode ser vazio!");
        }
        if (p.getPreco() < 0) {
            throw new IllegalArgumentException("Preço não pode ser negativo!");
        }
        if (p.getEstoque() < 0) {
            throw new IllegalArgumentException("Estoque não pode ser negativo!");
        }
        this.produtos.add(p);
        if (!GerenciadorDados.salvarObjeto(p, ARQUIVO_PRODUTOS)) {
            this.produtos.remove(p);
            throw new IllegalStateException("Falha ao salvar produtos.");
        }
    }
    
    public void editarProduto(int id, String nome, double preco, int estoque) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do produto não pode ser vazio!");
        }
        if (preco < 0) {
            throw new IllegalArgumentException("Preço não pode ser negativo!");
        }
        if (estoque < 0) {
            throw new IllegalArgumentException("Estoque não pode ser negativo!");
        }
        
        Produto produto = buscarProdutoPorId(id);
        if (produto == null) {
            throw new IllegalArgumentException("Produto não encontrado!");
        }
        
        produto.setNome(nome);
        produto.setPreco(preco);
        produto.setEstoque(estoque);
        
        if (!GerenciadorDados.reescreverArquivo(produtos, ARQUIVO_PRODUTOS)) {
            throw new IllegalStateException("Falha ao salvar produtos.");
        }
        
        atualizarCarrinhosProdutoEditado(id);
    }
    
    public void excluirProduto(int id) {
        Produto produto = buscarProdutoPorId(id);
        if (produto == null) {
            throw new IllegalArgumentException("Produto não encontrado!");
        }
        produtos.remove(produto);
        if (!GerenciadorDados.reescreverArquivo(produtos, ARQUIVO_PRODUTOS)) {
            throw new IllegalStateException("Falha ao salvar produtos.");
        }
        
        removerProdutoDosCarrinhos(id);
    }

    public ArrayList<Produto> getProdutos() {
        return produtos;
    }
    
    public ArrayList<Produto> getProdutos(boolean apenasComEstoque) {
        if (!apenasComEstoque) {
            return getProdutos();
        }
        ArrayList<Produto> disponiveis = new ArrayList<>();
        for (Produto p : produtos) {
            if (p.getEstoque() > 0) {
                disponiveis.add(p);
            }
        }
        return disponiveis;
    }
    
    public ArrayList<Pedido> getPedidos() {
        return pedidos;
    }

    public void finalizarCompra(
            Cliente cliente,
            String nomeComprador,
            String emailComprador,
            String telefoneComprador,
            Endereco enderecoEntrega,
            MetodoPagamento metodoPagamento
    ) throws EstoqueInsuficienteException {
        if (cliente.getCarrinho().isEmpty()) {
            throw new EstoqueInsuficienteException("Carrinho vazio!");
        }

        double total = 0.0;
        java.util.ArrayList<Produto> itensPedido = new java.util.ArrayList<>();
        java.util.ArrayList<Produto> produtosParaBaixar = new java.util.ArrayList<>();
        java.util.ArrayList<Integer> quantidadesParaBaixar = new java.util.ArrayList<>();
        for (modelo.ItemCarrinho itemCarrinho : cliente.getCarrinho()) {
            Produto produtoEstoque = buscarProdutoPorId(itemCarrinho.getProduto().getId());
            if (produtoEstoque == null) {
                throw new EstoqueInsuficienteException("Produto não encontrado: " + itemCarrinho.getProduto().getNome());
            }
            if (produtoEstoque.getEstoque() < itemCarrinho.getQuantidade()) {
                throw new EstoqueInsuficienteException("Estoque insuficiente para: " + itemCarrinho.getProduto().getNome() +
                        " (disponível: " + produtoEstoque.getEstoque() + ", solicitado: " + itemCarrinho.getQuantidade() + ")");
            }
            produtosParaBaixar.add(produtoEstoque);
            quantidadesParaBaixar.add(itemCarrinho.getQuantidade());
            for (int i = 0; i < itemCarrinho.getQuantidade(); i++) {
                itensPedido.add(Produto.snapshot(produtoEstoque));
            }
            total += produtoEstoque.getPreco() * itemCarrinho.getQuantidade();
        }

        for (int i = 0; i < produtosParaBaixar.size(); i++) {
            Produto produtoEstoque = produtosParaBaixar.get(i);
            int qtd = quantidadesParaBaixar.get(i);
            produtoEstoque.setEstoque(produtoEstoque.getEstoque() - qtd);
        }

        if (!GerenciadorDados.reescreverArquivo(produtos, ARQUIVO_PRODUTOS)) {
            throw new IllegalStateException("Falha ao salvar produtos.");
        }

        Pedido pedido = new Pedido(
                contadorPedidos++,
                cliente.getLogin(),
                itensPedido,
                total,
                nomeComprador,
                emailComprador,
                telefoneComprador,
                enderecoEntrega,
                metodoPagamento
        );
        pedidos.add(pedido);
        if (!GerenciadorDados.salvarObjeto(pedido, ARQUIVO_PEDIDOS)) {
            pedidos.remove(pedido);
            throw new IllegalStateException("Falha ao salvar pedidos.");
        }

        cliente.limparCarrinho();
        salvarCarrinhoCliente(cliente);
    }

    public Produto buscarProdutoPorId(int id) {
        for (Produto p : produtos) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }
    
    public void salvarCarrinhoCliente(Cliente cliente) {
        for (int i = 0; i < usuarios.size(); i++) {
            Usuario u = usuarios.get(i);
            if (u instanceof Cliente && u.getLogin().equals(cliente.getLogin())) {
                usuarios.set(i, cliente);
                break;
            }
        }
        if (!GerenciadorDados.reescreverArquivo(usuarios, ARQUIVO_USUARIOS)) {
            throw new IllegalStateException("Falha ao salvar usuários.");
        }
    }
    
    private void sincronizarCarrinhos() {
        boolean carrinhosModificados = false;
        
        for (int i = 0; i < usuarios.size(); i++) {
            Usuario u = usuarios.get(i);
            if (u instanceof Cliente) {
                Cliente cliente = (Cliente) u;
                boolean carrinhoModificado = false;
                
                java.util.Iterator<modelo.ItemCarrinho> iterator = cliente.getCarrinho().iterator();
                while (iterator.hasNext()) {
                    modelo.ItemCarrinho item = iterator.next();
                    Produto produtoAtual = buscarProdutoPorId(item.getProduto().getId());
                    
                    if (produtoAtual == null) {
                        iterator.remove();
                        carrinhoModificado = true;
                    } else {
                        item.getProduto().setNome(produtoAtual.getNome());
                        item.getProduto().setPreco(produtoAtual.getPreco());
                        item.getProduto().setEstoque(produtoAtual.getEstoque());
                    }
                }
                
                if (carrinhoModificado) {
                    usuarios.set(i, cliente);
                    carrinhosModificados = true;
                }
            }
        }
        
        if (carrinhosModificados) {
            if (!GerenciadorDados.reescreverArquivo(usuarios, ARQUIVO_USUARIOS)) {
                throw new IllegalStateException("Falha ao salvar usuários.");
            }
        }
    }
    
    private void atualizarCarrinhosProdutoEditado(int idProduto) {
        Produto produtoAtualizado = buscarProdutoPorId(idProduto);
        if (produtoAtualizado == null) {
            return;
        }
        
        boolean carrinhosModificados = false;
        
        for (Usuario u : usuarios) {
            if (u instanceof Cliente) {
                Cliente cliente = (Cliente) u;
                boolean carrinhoModificado = false;
                
                for (modelo.ItemCarrinho item : cliente.getCarrinho()) {
                    if (item.getProduto().getId() == idProduto) {
                        item.getProduto().setNome(produtoAtualizado.getNome());
                        item.getProduto().setPreco(produtoAtualizado.getPreco());
                        item.getProduto().setEstoque(produtoAtualizado.getEstoque());
                        carrinhoModificado = true;
                    }
                }
                
                if (carrinhoModificado) {
                    carrinhosModificados = true;
                }
            }
        }
        
        if (carrinhosModificados) {
            if (!GerenciadorDados.reescreverArquivo(usuarios, ARQUIVO_USUARIOS)) {
                throw new IllegalStateException("Falha ao salvar usuários.");
            }
        }
    }
    
    private void removerProdutoDosCarrinhos(int idProduto) {
        boolean carrinhosModificados = false;
        
        for (Usuario u : usuarios) {
            if (u instanceof Cliente) {
                Cliente cliente = (Cliente) u;
                boolean carrinhoModificado = false;
                
                java.util.Iterator<modelo.ItemCarrinho> iterator = cliente.getCarrinho().iterator();
                while (iterator.hasNext()) {
                    modelo.ItemCarrinho item = iterator.next();
                    if (item.getProduto().getId() == idProduto) {
                        iterator.remove();
                        carrinhoModificado = true;
                    }
                }
                
                if (carrinhoModificado) {
                    carrinhosModificados = true;
                }
            }
        }
        
        if (carrinhosModificados) {
            if (!GerenciadorDados.reescreverArquivo(usuarios, ARQUIVO_USUARIOS)) {
                throw new IllegalStateException("Falha ao salvar usuários.");
            }
        }
    }
}
