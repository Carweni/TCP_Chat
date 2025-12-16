package Trabalho_2.servidor;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import Trabalho_2.chat.Mensagem;

/*
 * Classe responsável por implementar um servidor de chat TCP que permite a conexão entre
 * múltiplos cliente conectados simultaneamente. Aceita as conexões, gerencia usuários
 * conectados e envio de mensagens, além de listar usuários conectados.
 */
public class Servidor {
    private static final int PORTA = 12345;
    private ServerSocket serverSocket;

    private Map<String, ClienteHandler> clientes; // Mapa thread-safe para armazenar clientes conectados.
    private boolean rodando = false;
    
    // Método construtor:
    public Servidor() {
        clientes = new ConcurrentHashMap<>();
    }
    
    // Inicializa o servidor (cria o ServerSocket e entra em loop para aceitar conexões):
    public void iniciar() {
        try {
            serverSocket = new ServerSocket(PORTA);
            rodando = true;
            
            System.out.println("=== SERVIDOR DE CHAT TCP ===");
            System.out.println("Servidor iniciado na porta " + PORTA);
            System.out.println("Aguardando conexões...\n");
            
            // Loop principal - aceita conexões continuamente:
            while (rodando) {
                try {
                    Socket clienteSocket = serverSocket.accept(); // Bloqueia até uma nova conexão chegar.
                    ClienteHandler handler = new ClienteHandler(clienteSocket, this); // Cria um handler para este cliente.
                    new Thread(handler).start(); // Inicia uma nova thread para cada cliente.
                } catch (IOException e) {
                    if (rodando) {
                        System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar servidor: " + e.getMessage());
        }
    }
    
    // Adiciona um novo cliente à lista de clientes conectados:
    public synchronized void adicionarCliente(String nomeUsuario, ClienteHandler handler) {
        clientes.put(nomeUsuario, handler);

        // Imprime na tela um aviso de conexão do novo cliente:
        System.out.println("Cliente conectado: " + nomeUsuario + 
                          " (Total: " + clientes.size() + " clientes)");
        listarClientesAtivos();
    }
    
    // Remove um cliente da lista de clientes conectados:
    public synchronized void removerCliente(String nomeUsuario) {
        clientes.remove(nomeUsuario);

        // Imprime na tela (em broadcast) um aviso de que um usuário se desconectou:
        System.out.println("Cliente desconectado: " + nomeUsuario + 
                          " (Total: " + clientes.size() + " clientes)");
        listarClientesAtivos();
    }
    
    // Envia uma mensagem para todos os clientes conectados, exceto o remetente:
    public synchronized void broadcast(Mensagem mensagem, String remetente) {
        for (Map.Entry<String, ClienteHandler> entry : clientes.entrySet()) {
            if (!entry.getKey().equals(remetente)) {
                entry.getValue().enviarMensagem(mensagem);
            }
        }
    }
    
    // Envia mensagem privada para usuário específico:
    public synchronized void enviarMensagemPrivada(Mensagem mensagem) {
        ClienteHandler destinatario = clientes.get(mensagem.getDestinatario());
        if (destinatario != null) {
            destinatario.enviarMensagem(mensagem);
        } else {
            // Enviar mensagem de erro para o remetente caso o destinatário não exista:
            ClienteHandler remetente = clientes.get(mensagem.getRemetente());
            if (remetente != null) {
                Mensagem erro = new Mensagem("SISTEMA", mensagem.getRemetente(),
                    "Usuário '" + mensagem.getDestinatario() + "' não encontrado!");
                remetente.enviarMensagem(erro);
            }
        }
    }
    
    // Gera uma string com a lista de todos os usuários conectados (resposta ao comando /usuarios):
    public synchronized String listarUsuarios() {
        if (clientes.isEmpty()) {
            return "Nenhum usuário conectado.";
        }
        
        StringBuilder lista = new StringBuilder("Usuários conectados:\n");
        for (String usuario : clientes.keySet()) {
            lista.append("• ").append(usuario).append("\n");
        }
        return lista.toString();
    }
    
    // Exibe clientes ativos no console do servidor:
    private void listarClientesAtivos() {
        if (clientes.isEmpty()) {
            System.out.println("Nenhum cliente conectado.\n");
        } else {
            System.out.print("Clientes ativos: ");
            System.out.println(String.join(", ", clientes.keySet()) + "\n");
        }
    }
    
    // Verifica se um usuário com determinado nome já está conectado:
    public synchronized boolean usuarioExiste(String nomeUsuario) {
        return clientes.containsKey(nomeUsuario);
    }
    
    // Para o servidor (fecha o ServerSocket):
    public void parar() {
        rodando = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Erro ao fechar servidor: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        
        // Cria thread para permitir parada do servidor via console:
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Digite 'sair' para parar o servidor.");
            while (true) {
                String comando = scanner.nextLine();
                if ("sair".equalsIgnoreCase(comando)) {
                    System.out.println("Parando servidor...");
                    servidor.parar();
                    break;
                }
            }
            scanner.close();
        }).start();
        
        servidor.iniciar();  // Inicia server na thread principal.
    }
}