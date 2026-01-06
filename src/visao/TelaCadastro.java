package visao;

import modelo.Cliente;
import sistema.SistemaLoja;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class TelaCadastro extends JFrame {
    private SistemaLoja sistema;
    private JTextField txtLogin;
    private JPasswordField txtSenha;
    private JPasswordField txtConfirmarSenha;
    private JTextField txtNome;

    public TelaCadastro(SistemaLoja sistema) {
        this.sistema = sistema;
        setTitle("Cadastro de Cliente");
        setSize(350, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Login:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtLogin = new JTextField();
        add(txtLogin, gbc);

        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtSenha = new JPasswordField();
        add(txtSenha, gbc);

        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(new JLabel("Confirmar Senha:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtConfirmarSenha = new JPasswordField();
        add(txtConfirmarSenha, gbc);

        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtNome = new JTextField();
        add(txtNome, gbc);

        
        JPanel painelBotoes = new JPanel(new FlowLayout());
        JButton btnCadastrar = new JButton("Cadastrar");
        JButton btnCancelar = new JButton("Cancelar");
        painelBotoes.add(btnCadastrar);
        painelBotoes.add(btnCancelar);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(painelBotoes, gbc);

        
        btnCadastrar.addActionListener(e -> cadastrarCliente());
        btnCancelar.addActionListener(e -> dispose());
    }

    private void cadastrarCliente() {
        String login = txtLogin.getText().trim();
        String senha = new String(txtSenha.getPassword());
        String confirmarSenha = new String(txtConfirmarSenha.getPassword());
        String nome = txtNome.getText().trim();

        
        if (login.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Login não pode ser vazio!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Senha não pode ser vazia!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!senha.equals(confirmarSenha)) {
            JOptionPane.showMessageDialog(this, "As senhas não coincidem!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nome não pode ser vazio!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Cliente novoCliente = new Cliente(login, senha, nome);
            sistema.cadastrarUsuario(novoCliente);
            JOptionPane.showMessageDialog(this, "Cliente cadastrado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}

