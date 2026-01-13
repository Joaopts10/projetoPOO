package visao;

import excecoes.EstoqueInsuficienteException;
import modelo.Cliente;
import modelo.Endereco;
import modelo.ItemCarrinho;
import modelo.MetodoPagamento;
import sistema.SistemaLoja;

import javax.swing.*;
import java.awt.*;

public class TelaCheckout extends JDialog {
    private final SistemaLoja sistema;
    private final Cliente cliente;

    private boolean compraFinalizada = false;

    private JTextField txtNome;
    private JTextField txtEmail;
    private JTextField txtTelefone;

    private JTextField txtCep;
    private JTextField txtRua;
    private JTextField txtNumero;
    private JTextField txtComplemento;
    private JTextField txtBairro;
    private JTextField txtCidade;
    private JTextField txtEstado;

    private JComboBox<MetodoPagamento> cmbPagamento;
    private JLabel lblTotal;

    public TelaCheckout(JFrame owner, SistemaLoja sistema, Cliente cliente) {
        super(owner, "Checkout", true);
        this.sistema = sistema;
        this.cliente = cliente;

        setSize(900, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        add(criarPainelResumo(), BorderLayout.WEST);
        add(criarPainelFormulario(), BorderLayout.CENTER);
        add(criarPainelAcoes(), BorderLayout.SOUTH);
    }

    public boolean isCompraFinalizada() {
        return compraFinalizada;
    }

    private JPanel criarPainelResumo() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Resumo do Carrinho"));
        panel.setPreferredSize(new Dimension(360, 0));

        DefaultListModel<String> model = new DefaultListModel<>();
        for (ItemCarrinho item : cliente.getCarrinho()) {
            model.addElement(item.toString());
        }
        JList<String> lista = new JList<>(model);
        panel.add(new JScrollPane(lista), BorderLayout.CENTER);

        double totalAtualizado = cliente.getTotalCarrinhoAtualizado(id -> sistema.buscarProdutoPorId(id));
        lblTotal = new JLabel("Total: R$ " + String.format("%.2f", totalAtualizado), SwingConstants.CENTER);
        lblTotal.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        panel.add(lblTotal, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel criarPainelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Dados do Comprador"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int y = 0;

        txtNome = new JTextField(cliente.getNome());
        addLinha(panel, gbc, y++, "Nome:", txtNome);

        txtEmail = new JTextField();
        addLinha(panel, gbc, y++, "Email:", txtEmail);

        txtTelefone = new JTextField();
        addLinha(panel, gbc, y++, "Telefone:", txtTelefone);

        panel.add(new JSeparator(), separador(gbc, y++));

        txtCep = new JTextField();
        addLinha(panel, gbc, y++, "CEP:", txtCep);

        txtRua = new JTextField();
        addLinha(panel, gbc, y++, "Rua:", txtRua);

        txtNumero = new JTextField();
        addLinha(panel, gbc, y++, "Número:", txtNumero);

        txtComplemento = new JTextField();
        addLinha(panel, gbc, y++, "Complemento:", txtComplemento);

        txtBairro = new JTextField();
        addLinha(panel, gbc, y++, "Bairro:", txtBairro);

        txtCidade = new JTextField();
        addLinha(panel, gbc, y++, "Cidade:", txtCidade);

        txtEstado = new JTextField();
        addLinha(panel, gbc, y++, "Estado:", txtEstado);

        panel.add(new JSeparator(), separador(gbc, y++));

        cmbPagamento = new JComboBox<>(MetodoPagamento.values());
        addLinha(panel, gbc, y++, "Pagamento:", cmbPagamento);

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    private JPanel criarPainelAcoes() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Voltar");
        JButton btnConfirmar = new JButton("Confirmar Compra");

        btnCancelar.addActionListener(e -> dispose());
        btnConfirmar.addActionListener(e -> confirmarCompra());

        panel.add(btnCancelar);
        panel.add(btnConfirmar);
        return panel;
    }

    private void confirmarCompra() {
        String nome = txtNome.getText().trim();
        String email = txtEmail.getText().trim();
        String telefone = txtTelefone.getText().trim();

        String cep = txtCep.getText().trim();
        String rua = txtRua.getText().trim();
        String numero = txtNumero.getText().trim();
        String complemento = txtComplemento.getText().trim();
        String bairro = txtBairro.getText().trim();
        String cidade = txtCidade.getText().trim();
        String estado = txtEstado.getText().trim();

        MetodoPagamento pagamento = (MetodoPagamento) cmbPagamento.getSelectedItem();

        if (nome.isEmpty() || email.isEmpty() || telefone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha nome, email e telefone.", "Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!email.contains("@")) {
            JOptionPane.showMessageDialog(this, "Email inválido. Precisa conter o símbolo: @", "Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cep.isEmpty() || rua.isEmpty() || numero.isEmpty() || bairro.isEmpty() || cidade.isEmpty() || estado.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha os dados obrigatórios do endereço.", "Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (pagamento == null) {
            JOptionPane.showMessageDialog(this, "Selecione um método de pagamento.", "Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Endereco endereco = new Endereco(cep, rua, numero, complemento, bairro, cidade, estado);

        int confirmacao = JOptionPane.showConfirmDialog(
                this,
                "Total: " + lblTotal.getText().replace("Total: ", "") + "\nConfirmar compra?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION
        );
        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            sistema.finalizarCompra(cliente, nome, email, telefone, endereco, pagamento);
            compraFinalizada = true;
            JOptionPane.showMessageDialog(this, "Compra realizada com sucesso!");
            dispose();
        } catch (EstoqueInsuficienteException ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Estoque", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void addLinha(JPanel panel, GridBagConstraints gbc, int y, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    private static GridBagConstraints separador(GridBagConstraints base, int y) {
        GridBagConstraints gbc = (GridBagConstraints) base.clone();
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        return gbc;
    }
}




