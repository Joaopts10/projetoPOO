package visao;

import modelo.Pedido;
import modelo.Produto;
import sistema.SistemaLoja;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class TelaVendasAdmin extends JFrame {
    private final SistemaLoja sistema;

    private JLabel lblTotalVendido;
    private JLabel lblQtdPedidos;
    private JLabel lblTicketMedio;

    private DefaultTableModel tableModel;
    private JTable tablePedidos;
    private JTextArea txtDetalhe;

    public TelaVendasAdmin(SistemaLoja sistema) {
        this.sistema = sistema;

        setTitle("Painel de Vendas");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(criarTopoKpis(), BorderLayout.NORTH);
        add(criarCentroTabelaEDetalhe(), BorderLayout.CENTER);
        add(criarRodapeAcoes(), BorderLayout.SOUTH);

        carregarPedidos();
    }

    private JPanel criarTopoKpis() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        lblTotalVendido = criarCard(panel, "Total Vendido");
        lblQtdPedidos = criarCard(panel, "Qtd. Pedidos");
        lblTicketMedio = criarCard(panel, "Ticket Médio");

        return panel;
    }

    private JLabel criarCard(JPanel parent, String titulo) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createTitledBorder(titulo));
        JLabel value = new JLabel("-", SwingConstants.CENTER);
        value.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        card.add(value, BorderLayout.CENTER);
        parent.add(card);
        return value;
    }

    private JSplitPane criarCentroTabelaEDetalhe() {
        JPanel panelTabela = new JPanel(new BorderLayout(5, 5));
        panelTabela.setBorder(BorderFactory.createTitledBorder("Pedidos"));

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Data/Hora", "Login", "Nome", "Total", "Pagamento"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablePedidos = new JTable(tableModel);
        tablePedidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePedidos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalheSelecionado();
            }
        });
        panelTabela.add(new JScrollPane(tablePedidos), BorderLayout.CENTER);

        JPanel panelDetalhe = new JPanel(new BorderLayout(5, 5));
        panelDetalhe.setBorder(BorderFactory.createTitledBorder("Detalhe do Pedido"));
        txtDetalhe = new JTextArea();
        txtDetalhe.setEditable(false);
        txtDetalhe.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        panelDetalhe.add(new JScrollPane(txtDetalhe), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelTabela, panelDetalhe);
        split.setResizeWeight(0.65);
        split.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return split;
    }

    private JPanel criarRodapeAcoes() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAtualizar = new JButton("Atualizar");
        JButton btnFechar = new JButton("Fechar");

        btnAtualizar.addActionListener(e -> carregarPedidos());
        btnFechar.addActionListener(e -> dispose());

        panel.add(btnAtualizar);
        panel.add(btnFechar);
        return panel;
    }

    private void carregarPedidos() {
        ArrayList<Pedido> pedidos = sistema.getPedidos();

        double totalVendido = 0.0;
        for (Pedido p : pedidos) {
            totalVendido += p.getTotal();
        }
        int qtd = pedidos.size();
        double ticket = qtd == 0 ? 0.0 : (totalVendido / qtd);

        lblTotalVendido.setText("R$ " + String.format("%.2f", totalVendido));
        lblQtdPedidos.setText(String.valueOf(qtd));
        lblTicketMedio.setText("R$ " + String.format("%.2f", ticket));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        tableModel.setRowCount(0);
        for (Pedido p : pedidos) {
            String data = p.getDataHora() != null ? p.getDataHora().format(fmt) : "-";
            String nome = p.getNomeComprador() != null ? p.getNomeComprador() : "-";
            String pagamento = p.getMetodoPagamento() != null ? p.getMetodoPagamento().toString() : "-";
            tableModel.addRow(new Object[]{
                    p.getId(),
                    data,
                    p.getLoginCliente(),
                    nome,
                    "R$ " + String.format("%.2f", p.getTotal()),
                    pagamento
            });
        }

        txtDetalhe.setText("");
    }

    private void mostrarDetalheSelecionado() {
        int row = tablePedidos.getSelectedRow();
        if (row < 0) {
            txtDetalhe.setText("");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        Pedido pedido = null;
        for (Pedido p : sistema.getPedidos()) {
            if (p.getId() == id) {
                pedido = p;
                break;
            }
        }
        if (pedido == null) {
            txtDetalhe.setText("Pedido não encontrado.");
            return;
        }

        Map<Integer, Integer> qtdPorProduto = new LinkedHashMap<>();
        Map<Integer, Produto> produtoPorId = new LinkedHashMap<>();
        for (Produto prod : pedido.getItens()) {
            if (prod == null) {
                continue;
            }
            qtdPorProduto.put(prod.getId(), qtdPorProduto.getOrDefault(prod.getId(), 0) + 1);
            produtoPorId.put(prod.getId(), prod);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Pedido #").append(pedido.getId()).append("\n");
        sb.append("Login: ").append(pedido.getLoginCliente()).append("\n");
        if (pedido.getNomeComprador() != null) {
            sb.append("Nome: ").append(pedido.getNomeComprador()).append("\n");
        }
        if (pedido.getEmailComprador() != null) {
            sb.append("Email: ").append(pedido.getEmailComprador()).append("\n");
        }
        if (pedido.getTelefoneComprador() != null) {
            sb.append("Telefone: ").append(pedido.getTelefoneComprador()).append("\n");
        }
        if (pedido.getMetodoPagamento() != null) {
            sb.append("Pagamento: ").append(pedido.getMetodoPagamento()).append("\n");
        }
        sb.append("\n");

        sb.append("Endereço:\n");
        if (pedido.getEnderecoEntrega() != null) {
            sb.append(pedido.getEnderecoEntrega()).append("\n");
        } else {
            sb.append("-\n");
        }
        sb.append("\n");

        sb.append("Itens:\n");
        if (qtdPorProduto.isEmpty()) {
            sb.append("-\n");
        } else {
            for (Map.Entry<Integer, Integer> e : qtdPorProduto.entrySet()) {
                Produto prod = produtoPorId.get(e.getKey());
                String nome = prod != null ? prod.getNome() : "Produto";
                double preco = prod != null ? prod.getPreco() : 0.0;
                sb.append(e.getValue()).append("x ").append(nome)
                        .append(" (R$ ").append(String.format("%.2f", preco)).append(")\n");
            }
        }

        sb.append("\nTotal: R$ ").append(String.format("%.2f", pedido.getTotal())).append("\n");

        txtDetalhe.setText(sb.toString());
        txtDetalhe.setCaretPosition(0);
    }
}




