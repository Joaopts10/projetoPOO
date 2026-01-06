package visao;

import modelo.Produto;
import sistema.SistemaLoja;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class TelaAdmin extends JFrame {
    private SistemaLoja sistema;
    private DefaultListModel<Produto> listModel;
    private JList<Produto> listaProdutos;

    
    private JTextField txtNome;
    private JTextField txtPreco;
    private JTextField txtEstoque;
    
    private Produto produtoSelecionado;

    public TelaAdmin(SistemaLoja sistema) {
        this.sistema = sistema;
        setTitle("Painel do Administrador");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        
        listModel = new DefaultListModel<>();
        atualizarLista();
        listaProdutos = new JList<>(listModel);
        listaProdutos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selecionarProduto();
            }
        });
        add(new JScrollPane(listaProdutos), BorderLayout.CENTER);

        
        JPanel painelCadastro = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        painelCadastro.add(new JLabel("Nome Produto:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtNome = new JTextField();
        painelCadastro.add(txtNome, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        painelCadastro.add(new JLabel("Preço:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtPreco = new JTextField();
        painelCadastro.add(txtPreco, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        painelCadastro.add(new JLabel("Estoque:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtEstoque = new JTextField();
        painelCadastro.add(txtEstoque, gbc);

        
        JPanel painelBotoes = new JPanel(new FlowLayout());
        JButton btnSalvar = new JButton("Cadastrar Produto");
        JButton btnEditar = new JButton("Editar");
        JButton btnExcluir = new JButton("Excluir");
        JButton btnLimpar = new JButton("Limpar");
        JButton btnLogout = new JButton("Sair");

        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnEditar);
        painelBotoes.add(btnExcluir);
        painelBotoes.add(btnLimpar);
        painelBotoes.add(btnLogout);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        painelCadastro.add(painelBotoes, gbc);

        add(painelCadastro, BorderLayout.SOUTH);

        
        btnSalvar.addActionListener(e -> cadastrarProduto());
        btnEditar.addActionListener(e -> editarProduto());
        btnExcluir.addActionListener(e -> excluirProduto());
        btnLimpar.addActionListener(e -> limparCampos());
        btnLogout.addActionListener(e -> {
            sistema.logout();
            new TelaLogin(sistema).setVisible(true);
            dispose();
        });
    }

    private void atualizarLista() {
        listModel.clear();
        for (Produto p : sistema.getProdutos()) {
            listModel.addElement(p);
        }
    }

    private void selecionarProduto() {
        produtoSelecionado = listaProdutos.getSelectedValue();
        if (produtoSelecionado != null) {
            txtNome.setText(produtoSelecionado.getNome());
            txtPreco.setText(String.format("%.2f", produtoSelecionado.getPreco()));
            txtEstoque.setText(String.valueOf(produtoSelecionado.getEstoque()));
        }
    }

    private void cadastrarProduto() {
        try {
            String nome = txtNome.getText().trim();
            if (nome.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nome do produto não pode ser vazio!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double preco = Double.parseDouble(txtPreco.getText().replace(",", "."));
            int estoque = Integer.parseInt(txtEstoque.getText());

            if (preco < 0) {
                JOptionPane.showMessageDialog(this, "Preço não pode ser negativo!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (estoque < 0) {
                JOptionPane.showMessageDialog(this, "Estoque não pode ser negativo!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Produto novo = new Produto(nome, preco, estoque);
            sistema.cadastrarProduto(novo);

            atualizarLista();
            limparCampos();
            JOptionPane.showMessageDialog(this, "Produto cadastrado com sucesso!");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Digite números válidos para preço e estoque.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar no arquivo: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarProduto() {
        if (produtoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um produto para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String nome = txtNome.getText().trim();
            if (nome.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nome do produto não pode ser vazio!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double preco = Double.parseDouble(txtPreco.getText().replace(",", "."));
            int estoque = Integer.parseInt(txtEstoque.getText());

            if (preco < 0) {
                JOptionPane.showMessageDialog(this, "Preço não pode ser negativo!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (estoque < 0) {
                JOptionPane.showMessageDialog(this, "Estoque não pode ser negativo!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            sistema.editarProduto(produtoSelecionado.getId(), nome, preco, estoque);

            atualizarLista();
            limparCampos();
            produtoSelecionado = null;
            JOptionPane.showMessageDialog(this, "Produto editado com sucesso!");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Digite números válidos para preço e estoque.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar no arquivo: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirProduto() {
        if (produtoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um produto para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmacao = JOptionPane.showConfirmDialog(this,
            "Deseja realmente excluir o produto: " + produtoSelecionado.getNome() + "?",
            "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);

        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                sistema.excluirProduto(produtoSelecionado.getId());
                atualizarLista();
                limparCampos();
                produtoSelecionado = null;
                JOptionPane.showMessageDialog(this, "Produto excluído com sucesso!");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar no arquivo: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limparCampos() {
        txtNome.setText("");
        txtPreco.setText("");
        txtEstoque.setText("");
        produtoSelecionado = null;
        listaProdutos.clearSelection();
    }
}
