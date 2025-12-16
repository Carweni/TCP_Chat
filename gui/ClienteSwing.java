package Trabalho_2.gui;

import javax.swing.*;

import Trabalho_2.chat.Mensagem;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * Classe que implementa a interface gráfica do cliente de chat usando Swing.
 * A janela possui área de exibição de mensagens, campo de entrada e botões de comando.
 */

public class ClienteSwing extends JFrame {
    // Configurações de conexão com o servidor:
    private static final String HOST = "127.0.0.1";
    private static final int PORTA = 12345;
    
    // Formatação para data e horário das mensagens enviadas:
    private static final DateTimeFormatter FORMATO_HORARIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    // Componentes da interface gráfica:
    private JTextArea areaTexto;
    private JTextField campoEntrada;
    private JButton botaoEnviar;
    private JButton botaoConectar;
    private JTextField campoNome;
    private JLabel labelStatus;
    
    // Componentes de rede:
    private Socket socket;
    private ObjectInputStream entrada;
    private ObjectOutputStream saida;
    private String nomeUsuario;
    private boolean conectado = false;  // Flag que indica se está conectado ao servidor.
    
    // Método construtor:
    public ClienteSwing() {
        // Configurações básicas da janela:
        setTitle("Chat TCP - Cliente GUI");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // Centraliza na tela.
        
        //Inicializa os componentes da interface:
        initComponents(); // Cria e configura componentes.
        setupLayout();    // Organiza layout.
        setupEventListeners();  // Configura eventos dos botões.
    }
    
    private void initComponents() {
        // Área de exibição das mensagens:
        areaTexto = new JTextArea();
        areaTexto.setEditable(false);
        areaTexto.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        // Campo de entrada da mensagem:
        campoEntrada = new JTextField();
        campoEntrada.setEnabled(false); // Fica desabilitado até o usuário conectar.
        
        // Botões:
        botaoEnviar = new JButton("Enviar");
        botaoEnviar.setEnabled(false);       // Como está relacionado ao campo de entrada da menasgem, também fica desativado até o usuário conectar.
        botaoConectar = new JButton("Conectar");
        
        // Campo para inserção do nome do usuário:
        campoNome = new JTextField(15);
        campoNome.setText("Usuário" + (int)(Math.random() * 1000));  // Vem preenchido com um nome aleatório, que pode ser alterado.
        
        // Indicador de status (inicialmente, o usuário está desconectado e isso fica escrito em vermelho):
        labelStatus = new JLabel("Desconectado");
        labelStatus.setForeground(Color.RED);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Painel superior - área de conexão
        JPanel painelSuperior = new JPanel(new FlowLayout());  // Campo de nome e botão para conectar..
        painelSuperior.add(new JLabel("Nome:"));
        painelSuperior.add(campoNome);
        painelSuperior.add(botaoConectar);
        painelSuperior.add(new JLabel("Status:"));   // Mostra status (conectado/desconectado).
        painelSuperior.add(labelStatus);
        
        // Painel central - Área de texto com scroll (JScrollPane permite rolagem quando há muitas mensagens).
        JScrollPane scrollPane = new JScrollPane(areaTexto);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // Painel inferior - entrada de mensagem
        JPanel painelInferior = new JPanel(new BorderLayout());
        painelInferior.add(campoEntrada, BorderLayout.CENTER);
        painelInferior.add(botaoEnviar, BorderLayout.EAST);  // Botão de enviar na lateral direita.
        
        // Adiciona os painéis em seus respectivos lugares:
        add(painelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(painelInferior, BorderLayout.SOUTH);
        
        // Painel lateral com comandos, em layout vertical:
        JPanel painelComandos = new JPanel();
        painelComandos.setLayout(new BoxLayout(painelComandos, BoxLayout.Y_AXIS));
        painelComandos.setBorder(BorderFactory.createTitledBorder("Comandos"));
        
        // Botões para os comandos de listar usuários conectados, ajuda e desconectar:
        JButton btnUsuarios = new JButton("Listar Usuários");
        JButton btnAjuda = new JButton("Ajuda");
        JButton btnDesconectar = new JButton("Desconectar");
        
        // Enquanto o usuário não conectar, não poderá ver a lista de usuários nem desconectar:
        btnUsuarios.setEnabled(false);
        btnDesconectar.setEnabled(false);
        
        // Adiciona os botões na interface (espaço de 5 px):
        painelComandos.add(btnUsuarios);
        painelComandos.add(Box.createVerticalStrut(5));
        painelComandos.add(btnAjuda);
        painelComandos.add(Box.createVerticalStrut(5));
        painelComandos.add(btnDesconectar);
        
        // Adiciona o painel de comandos:
        add(painelComandos, BorderLayout.EAST);
        
        // Event listeners para botões de comando:
        btnUsuarios.addActionListener(e -> enviarComando("/usuarios"));
        btnAjuda.addActionListener(e -> mostrarAjuda());
        btnDesconectar.addActionListener(e -> desconectar());
    }
    
    private void setupEventListeners() {
        botaoConectar.addActionListener(e -> conectar());  // EVento do botão de conectar.

        campoNome.addActionListener(e ->conectar());  // Se pressionar Enter, também tenta conectar o usuário.
        
        botaoEnviar.addActionListener(e -> enviarMensagem()); // Evento do botão de enviar mensagem.
        
        campoEntrada.addActionListener(e -> enviarMensagem()); // Se pressionar Enter, também envia mensagem.
        
        // Listener para fechar janela:
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                desconectar();          // Desconecta do servidor.
                System.exit(0);  // Encerra aplicação.
            }
        });
    }
    
    // Estabelece conexão com o servidor e inicia o chat:
    private void conectar() {
        String nome = campoNome.getText().trim();

        // Valida se o nome foi preenchido (se estiver vazio, avisa o usuário):
        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite um nome de usuário!");
            return;
        }
        
        try {
            socket = new Socket(HOST, PORTA);
            saida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());
            conectado = true;
            nomeUsuario = nome;
            
            // Atualiza interface:
            atualizarInterface(true);
            areaTexto.append("Conectando ao servidor...\n");
            
            // Inicia thread para receber mensagens:
            Thread receptorMensagens = new Thread(this::receberMensagens);
            receptorMensagens.setDaemon(true);  // Será finalizada junto com o programa principal.
            receptorMensagens.start();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao conectar: " + e.getMessage());
            atualizarInterface(false);
        }
    }
    
    // ATualiza o estado visual da interface de acordo com o status de conexão:
    private void atualizarInterface(boolean conectado) {
        this.conectado = conectado;
        
        campoNome.setEnabled(!conectado);     // Nome só pode ser alterado quando desconectado.
        botaoConectar.setEnabled(!conectado); // Botão de conectar só pode ficar disponível quando desconectado.
        campoEntrada.setEnabled(conectado);   // Campo de mensagem só é disponível quando conectado.
        botaoEnviar.setEnabled(conectado);    // Só é possível enviar mensagem quando conectado.
        
        // Atualiza botões de comando:
        Component painelComandos = ((BorderLayout) getContentPane().getLayout()).getLayoutComponent(BorderLayout.EAST);
        if (painelComandos instanceof JPanel) {
            Component[] componentes = ((JPanel) painelComandos).getComponents();
            for (Component comp : componentes) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    // Habilita os botões de listar usuários e de desconectar quando o suuário estiver ativo:
                    if (btn.getText().equals("Listar Usuários") || btn.getText().equals("Desconectar")) {
                        btn.setEnabled(conectado);
                    }
                }
            }
        }
        
        // Atualiza indicador de status para "Conectado" e muda sua cor para verde:
        labelStatus.setText(conectado ? "Conectado" : "Desconectado");
        labelStatus.setForeground(conectado ? Color.GREEN : Color.RED);
        
        if (conectado) {
            campoEntrada.requestFocus();  // aumenta o foco do campo de entrada quando o usuário conecta.
        }
    }
    
    // Recebe e processa mensagens do servidor. Executa continuamente enquanto a conexão estiver ativa.
    private void receberMensagens() {
        try {
            Mensagem mensagem;
            while (conectado && (mensagem = (Mensagem) entrada.readObject()) != null) {
                final Mensagem msg = mensagem;
                
                SwingUtilities.invokeLater(() -> {  // Garante que a interface seja atualizada na thread correta.
                    // Processa mensagens do sistema:
                    if ("SISTEMA".equals(msg.getRemetente())) {
                        if ("SOLICITAR_NOME".equals(msg.getConteudo())) {
                            try {
                                Mensagem resposta = new Mensagem(nomeUsuario, null, nomeUsuario);
                                saida.writeObject(resposta);
                            } catch (IOException e) {
                                areaTexto.append("Erro ao enviar nome.\n");
                            }
                            return;
                        } else if (msg.getConteudo().startsWith("Nome já em uso")) { 
                            JOptionPane.showMessageDialog(this, msg.getConteudo());
                            desconectar();
                            return;
                        } else if (msg.getConteudo().startsWith("Bem-vindo")) {
                            areaTexto.append(msg.getConteudo() + "\n");
                            areaTexto.append("Digite suas mensagens abaixo. Use /privado <usuário> <mensagem> para mensagens privadas.\n\n");
                            return;
                        }
                    }
                    
                    // Exibir mensagem normal (já vem com horário do servidor):
                    areaTexto.append(msg.toString() + "\n");
                    areaTexto.setCaretPosition(areaTexto.getDocument().getLength());  // Rolagem automática para mostrar a mensagem mais recente.
                });
            }
        } catch (Exception e) {
            if (conectado) {
                SwingUtilities.invokeLater(() -> {
                    areaTexto.append("Conexão perdida com o servidor.\n");
                    atualizarInterface(false);
                });
            }
        }
    }
    
    // Método auxiliar para obter o horário atual formatado:
    private String obterHorarioAtual() {
        return LocalDateTime.now().format(FORMATO_HORARIO);
    }
    
    // Captura e envia mensagens de usuários. Distingue mensagens públicas e privadas, formata e exibe:
    private void enviarMensagem() {
        String texto = campoEntrada.getText().trim();
        if (texto.isEmpty() || !conectado) return;
        
        try {
            Mensagem mensagem;
            String horarioAtual = obterHorarioAtual();
            
            // Verificar se é mensagem privada:
            if (texto.startsWith("/privado ")) {
                String[] partes = texto.split(" ", 3); // Divide a entrada em /privado, destinatário e mensagem.
                if (partes.length >= 3) {
                    String destinatario = partes[1];
                    String conteudo = partes[2];
                    mensagem = new Mensagem(nomeUsuario, destinatario, "/privado:" + conteudo);

                    // Exibe data e hora do envio:
                    areaTexto.append("[" + horarioAtual + "] (Mensagem privada para " + destinatario + ") " + nomeUsuario + ": " + conteudo + "\n");
                } else {
                    // Se o comando estiver mal formatado, avisa o usuário:
                    areaTexto.append("Uso: /privado <usuário> <mensagem>\n");
                    campoEntrada.setText("");
                    return;
                }
            } else {
                // Mensagem pública
                mensagem = new Mensagem(nomeUsuario, null, texto);
                areaTexto.append("[" + horarioAtual + "] " + nomeUsuario + ": " + texto + "\n");
            }
            
            saida.writeObject(mensagem);  //Envia a mensagem ao servidor.
            campoEntrada.setText("");
            
            // Fazer scroll automático para mostrar a nova mensagem:
            areaTexto.setCaretPosition(areaTexto.getDocument().getLength());
            
        } catch (IOException e) {
            areaTexto.append("Erro ao enviar mensagem: " + e.getMessage() + "\n");
        }
    }
    
    // Processa os comandos especiais (ex. /usuarios) pelo campo de entrada também:
    private void enviarComando(String comando) {
        if (!conectado) return;
        
        try {
            Mensagem mensagem = new Mensagem(nomeUsuario, null, comando);
            saida.writeObject(mensagem);
        } catch (IOException e) {
            areaTexto.append("Erro ao enviar comando: " + e.getMessage() + "\n");
        }
    }
    
    // Exibe janela com os comandos disponíveis:
    private void mostrarAjuda() {
        String ajuda = """
                === COMANDOS DISPONÍVEIS ===
                
                /usuarios - Lista usuários conectados
                /privado <usuário> <mensagem> - Envia mensagem privada
                
                Para enviar mensagem pública, digite normalmente.
                
                === BOTÕES ===
                Listar Usuários - Mostra usuários online
                Desconectar - Sair do chat
                """;
        
        JOptionPane.showMessageDialog(this, ajuda, "Ajuda", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Encerra a conexão atualizando o status e fechando o socket:
    private void desconectar() {
        conectado = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            areaTexto.append("Desconectado do servidor.\n");
        } catch (IOException e) {
            areaTexto.append("Erro ao desconectar: " + e.getMessage() + "\n");
        } finally {
            atualizarInterface(false);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            } catch (Exception e) {
                
            }
            
            ClienteSwing cliente = new ClienteSwing();
            cliente.setVisible(true);
        });
    }
}