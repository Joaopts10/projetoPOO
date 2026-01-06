package sistema;

import dados.GerenciadorDados;
import modelo.*;
import excecoes.EstoqueInsuficienteException;

import java.io.IOException;
import java.util.ArrayList;

public class SistemaLoja {

    private ArrayList<Usuario> usuarios;
    private ArrayList<Produto> produtos;
    private ArrayList<Pedido> pedidos;
    private Usuario usuarioLogado;

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
            try {
                cadastrarUsuario(new Admin("admin", "admin", "Administrador Principal"));
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            if (p.getId() > maiorId) maiorId = p.getId();
        }
        Produto.setContador(maiorId + 1);
        
        ArrayList<Object> objsPedidos = GerenciadorDados.carregarObjetos(ARQUIVO_PEDIDOS);
        int maiorIdPedido = 0;
        for (Object o : objsPedidos) {
            Pedido ped = (Pedido) o;
            this.pedidos.add(ped);
            if (ped.getId() > maiorIdPedido) maiorIdPedido = ped.getId();
        }
        contadorPedidos = maiorIdPedido + 1;
        
        sincronizarCarrinhos();
    }

    public Usuario fazerLogin(String login, String senha) {
        for (Usuario u : usuarios) {
            if (u.getLogin().equals(login) && u.autenticar(senha)) {
                this.usuarioLogado = u;
                
                if (u instanceof Cliente) {
                    Cliente cliente = (Cliente) u;
                    sincronizarCarrinhoCliente(cliente);
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
            try {
                salvarCarrinhoCliente(cliente);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void logout() {
        this.usuarioLogado = null;
    }

    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public void cadastrarUsuario(Usuario u) throws IOException {
        for (Usuario usuario : usuarios) {
            if (usuario.getLogin().equals(u.getLogin())) {
                throw new IllegalArgumentException("Login já existe!");
            }
        }
        this.usuarios.add(u);
        GerenciadorDados.salvarObjeto(u, ARQUIVO_USUARIOS);
    }

    public void cadastrarProduto(Produto p) throws IOException {
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
        GerenciadorDados.salvarObjeto(p, ARQUIVO_PRODUTOS);
    }
    
    public void editarProduto(int id, String nome, double preco, int estoque) throws IOException {
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
        
        GerenciadorDados.reescreverArquivo(produtos, ARQUIVO_PRODUTOS);
        
        atualizarCarrinhosProdutoEditado(id);
    }
    
    public void excluirProduto(int id) throws IOException {
        Produto produto = buscarProdutoPorId(id);
        if (produto == null) {
            throw new IllegalArgumentException("Produto não encontrado!");
        }
        produtos.remove(produto);
        GerenciadorDados.reescreverArquivo(produtos, ARQUIVO_PRODUTOS);
        
        removerProdutoDosCarrinhos(id);
    }

    public ArrayList<Produto> getProdutos() {
        return produtos;
    }

    public void finalizarCompra(Cliente cliente) throws EstoqueInsuficienteException, IOException {
        if (cliente.getCarrinho().isEmpty()) {
            throw new EstoqueInsuficienteException("Carrinho vazio!");
        }

        for (modelo.ItemCarrinho itemCarrinho : cliente.getCarrinho()) {
            Produto produtoEstoque = buscarProdutoPorId(itemCarrinho.getProduto().getId());
            if (produtoEstoque == null) {
                throw new EstoqueInsuficienteException("Produto não encontrado: " + itemCarrinho.getProduto().getNome());
            }
            if (produtoEstoque.getEstoque() < itemCarrinho.getQuantidade()) {
                throw new EstoqueInsuficienteException("Estoque insuficiente para: " + itemCarrinho.getProduto().getNome() + 
                    " (disponível: " + produtoEstoque.getEstoque() + ", solicitado: " + itemCarrinho.getQuantidade() + ")");
            }
        }

        double total = cliente.getTotalCarrinho();
        java.util.ArrayList<Produto> itensPedido = new java.util.ArrayList<>();
        for (modelo.ItemCarrinho item : cliente.getCarrinho()) {
            for (int i = 0; i < item.getQuantidade(); i++) {
                itensPedido.add(item.getProduto());
            }
        }

        for (modelo.ItemCarrinho itemCarrinho : cliente.getCarrinho()) {
            Produto produtoEstoque = buscarProdutoPorId(itemCarrinho.getProduto().getId());
            produtoEstoque.setEstoque(produtoEstoque.getEstoque() - itemCarrinho.getQuantidade());
        }

        GerenciadorDados.reescreverArquivo(produtos, ARQUIVO_PRODUTOS);

        Pedido pedido = new Pedido(contadorPedidos++, cliente.getLogin(), itensPedido, total);
        pedidos.add(pedido);
        GerenciadorDados.salvarObjeto(pedido, ARQUIVO_PEDIDOS);

        cliente.limparCarrinho();
    }

    public Produto buscarProdutoPorId(int id) {
        for (Produto p : produtos) {
            if (p.getId() == id) return p;
        }
        return null;
    }
    
    public void salvarCarrinhoCliente(Cliente cliente) throws IOException {
        for (int i = 0; i < usuarios.size(); i++) {
            Usuario u = usuarios.get(i);
            if (u instanceof Cliente && u.getLogin().equals(cliente.getLogin())) {
                usuarios.set(i, cliente);
                break;
            }
        }
        GerenciadorDados.reescreverArquivo(usuarios, ARQUIVO_USUARIOS);
    }
    
    private void sincronizarCarrinhos() {
        boolean carrinhosModificados = false;
        
        for (Usuario u : usuarios) {
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
                    carrinhosModificados = true;
                }
            }
        }
        
        if (carrinhosModificados) {
            try {
                GerenciadorDados.reescreverArquivo(usuarios, ARQUIVO_USUARIOS);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void atualizarCarrinhosProdutoEditado(int idProduto) throws IOException {
        Produto produtoAtualizado = buscarProdutoPorId(idProduto);
        if (produtoAtualizado == null) return;
        
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
            GerenciadorDados.reescreverArquivo(usuarios, ARQUIVO_USUARIOS);
        }
    }
    
    private void removerProdutoDosCarrinhos(int idProduto) throws IOException {
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
            GerenciadorDados.reescreverArquivo(usuarios, ARQUIVO_USUARIOS);
        }
    }
}
