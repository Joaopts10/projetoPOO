package visao;

import modelo.Cliente;
import modelo.ItemCarrinho;
import modelo.Produto;
import sistema.SistemaLoja;

import javax.swing.*;
import java.awt.*;

public class TelaCliente extends JFrame {
    private SistemaLoja sistema;
    private Cliente clienteLogado;

    private DefaultListModel<Produto> modelCatalogo;
    private JList<Produto> listaCatalogo;

    private DefaultListModel<ItemCarrinho> modelCarrinho;
    private JList<ItemCarrinho> listaCarrinho;
    
    private JLabel lblTotal;

    public TelaCliente(SistemaLoja sistema, Cliente cliente) {
        this.sistema = sistema;
        this.clienteLogado = cliente;

        setTitle("Loja Virtual - Bem-vindo " + cliente.getNome());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                salvarCarrinho();
                sistema.logout();
                new TelaLogin(sistema).setVisible(true);
                dispose();
            }
        });

        
        JPanel painelPrincipal = new JPanel(new GridLayout(1, 2, 10, 10));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        
        JPanel panelCatalogo = new JPanel(new BorderLayout());
        panelCatalogo.setBorder(BorderFactory.createTitledBorder("Catálogo de Produtos"));

        modelCatalogo = new DefaultListModel<>();
        atualizarCatalogo();
        listaCatalogo = new JList<>(modelCatalogo);
        panelCatalogo.add(new JScrollPane(listaCatalogo), BorderLayout.CENTER);

        JPanel painelAdd = new JPanel(new BorderLayout());
        JSpinner spinnerQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        JButton btnAdd = new JButton("Adicionar ao Carrinho");
        painelAdd.add(new JLabel("Quantidade:"), BorderLayout.WEST);
        painelAdd.add(spinnerQuantidade, BorderLayout.CENTER);
        painelAdd.add(btnAdd, BorderLayout.EAST);
        panelCatalogo.add(painelAdd, BorderLayout.SOUTH);

        
        JPanel panelCarrinho = new JPanel(new BorderLayout());
        panelCarrinho.setBorder(BorderFactory.createTitledBorder("Seu Carrinho"));

        modelCarrinho = new DefaultListModel<>();
        listaCarrinho = new JList<>(modelCarrinho);
        panelCarrinho.add(new JScrollPane(listaCarrinho), BorderLayout.CENTER);

        
        JPanel painelTotal = new JPanel(new BorderLayout());
        lblTotal = new JLabel("Total: R$ 0,00", SwingConstants.CENTER);
        lblTotal.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        painelTotal.add(lblTotal, BorderLayout.CENTER);
        panelCarrinho.add(painelTotal, BorderLayout.NORTH);

        
        JPanel panelBotoesCarrinho = new JPanel(new GridLayout(2, 1, 5, 5));
        JButton btnRemover = new JButton("Remover do Carrinho");
        JButton btnFinalizar = new JButton("Finalizar Compra");
        JButton btnSair = new JButton("Sair");

        panelBotoesCarrinho.add(btnRemover);
        panelBotoesCarrinho.add(btnFinalizar);
        panelCarrinho.add(panelBotoesCarrinho, BorderLayout.SOUTH);

        painelPrincipal.add(panelCatalogo);
        painelPrincipal.add(panelCarrinho);

        
        JPanel painelInferior = new JPanel();
        painelInferior.add(btnSair);

        add(painelPrincipal, BorderLayout.CENTER);
        add(painelInferior, BorderLayout.SOUTH);

        atualizarCarrinho();

        
        btnAdd.addActionListener(e -> {
            Produto selecionado = listaCatalogo.getSelectedValue();
            if (selecionado != null) {
                
                Produto produtoAtual = sistema.buscarProdutoPorId(selecionado.getId());
                if (produtoAtual == null) {
                    JOptionPane.showMessageDialog(this, 
                        "Produto não encontrado ou foi removido!", 
                        "Erro", JOptionPane.ERROR_MESSAGE);
                    atualizarCatalogo(); 
                    return;
                }
                int quantidade = (Integer) spinnerQuantidade.getValue();
                int quantidadeNoCarrinho = clienteLogado.getQuantidadeNoCarrinho(produtoAtual.getId());
                int quantidadeTotal = quantidadeNoCarrinho + quantidade;
                
                if (quantidadeTotal > produtoAtual.getEstoque()) {
                    JOptionPane.showMessageDialog(this, 
                        "Estoque insuficiente! Disponível: " + produtoAtual.getEstoque() + 
                        ", já no carrinho: " + quantidadeNoCarrinho + 
                        ", tentando adicionar: " + quantidade, 
                        "Erro", JOptionPane.WARNING_MESSAGE);
                    atualizarCatalogo(); 
                    return;
                }
                clienteLogado.adicionarAoCarrinho(produtoAtual, quantidade);
                atualizarCarrinho();
                atualizarCatalogo(); 
                
                salvarCarrinho();
                JOptionPane.showMessageDialog(this, "Produto adicionado ao carrinho!");
            } else {
                JOptionPane.showMessageDialog(this, "Selecione um produto primeiro.");
            }
        });

        btnRemover.addActionListener(e -> {
            int indice = listaCarrinho.getSelectedIndex();
            if (indice >= 0) {
                clienteLogado.removerDoCarrinho(indice);
                atualizarCarrinho();
                
                salvarCarrinho();
            } else {
                JOptionPane.showMessageDialog(this, "Selecione um item para remover.");
            }
        });

        btnFinalizar.addActionListener(e -> {
            if (clienteLogado.getCarrinho().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Carrinho vazio!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            TelaCheckout checkout = new TelaCheckout(this, sistema, clienteLogado);
            checkout.setVisible(true);
            if (checkout.isCompraFinalizada()) {
                atualizarCarrinho();
                atualizarCatalogo();
            }
        });

        btnSair.addActionListener(e -> {
            
            salvarCarrinho();
            sistema.logout();
            new TelaLogin(sistema).setVisible(true);
            dispose();
        });
    }

    private void salvarCarrinho() {
        try {
            sistema.salvarCarrinhoCliente(clienteLogado);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarCatalogo() {
        modelCatalogo.clear();
        for (Produto p : sistema.getProdutos(true)) {
            modelCatalogo.addElement(p);
        }
    }

    private void atualizarCarrinho() {
        modelCarrinho.clear();
        boolean carrinhoModificado = false;
        
        
        java.util.Iterator<ItemCarrinho> iterator = clienteLogado.getCarrinho().iterator();
        while (iterator.hasNext()) {
            ItemCarrinho item = iterator.next();
            Produto produtoAtual = sistema.buscarProdutoPorId(item.getProduto().getId());
            if (produtoAtual == null) {
                
                iterator.remove();
                carrinhoModificado = true;
            } else {
                
                item.getProduto().setNome(produtoAtual.getNome());
                item.getProduto().setPreco(produtoAtual.getPreco());
                item.getProduto().setEstoque(produtoAtual.getEstoque());
                modelCarrinho.addElement(item);
            }
        }
        
        
        if (carrinhoModificado) {
            salvarCarrinho();
        }
        
        
        double total = clienteLogado.getTotalCarrinhoAtualizado(id -> sistema.buscarProdutoPorId(id));
        lblTotal.setText("Total: R$ " + String.format("%.2f", total));
    }
}
