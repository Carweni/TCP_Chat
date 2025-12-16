package Trabalho_2.servidor;

import java.io.*;
import java.net.Socket;

import Trabalho_2.chat.Mensagem;

/*
 * Classe responsável por gerenciar a comunicação com um cliente específico.
 * Cada instância desta classe representa uma conexão ativa com um cliente.
 * Implementa Runnable para executar em thread separada, permitindo múltiplos clientes simultâneos.
 */
public class ClienteHandler implements Runnable {
    private Socket socket;
    private Servidor servidor;
    private ObjectInputStream entrada;
    private ObjectOutputStream saida;
    private String nomeUsuario;
    
    // Método construtor - recebe o socket da conexão estabelecida com o cliente e a instância do servidor principal.
    public ClienteHandler(Socket socket, Servidor servidor) {
        this.socket = socket;
        this.servidor = servidor;
    }
    
    // Gerencia configuração, autenticação e comunicação da conexão:
    @Override
    public void run() {
        try {
            // Configura streams:
            saida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());
            
            // Solicita e valida nome do usuário:
            solicitarNomeUsuario();
            
            // Loop principal de recebimento de mensagens (escuta mensagens até a desconexão):
            Mensagem mensagem;
            while ((mensagem = (Mensagem) entrada.readObject()) != null) {
                processarMensagem(mensagem);
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Conexão perdida com cliente: " + 
                              (nomeUsuario != null ? nomeUsuario : "desconhecido"));
        } finally {
            desconectar();
        }
    }
    
    // Solicita nome, valida unicidade e confirma entrada do usuário no chat:
    private void solicitarNomeUsuario() throws IOException, ClassNotFoundException {
        // Solicita nome:
        Mensagem solicitacao = new Mensagem("SISTEMA", null, "SOLICITAR_NOME");
        saida.writeObject(solicitacao);
        
        // Recebe primeira resposta (nome enviado):
        Mensagem resposta = (Mensagem) entrada.readObject();
        String nomeDesejado = resposta.getConteudo();
        
        // Verificar se nome já existe, e se sim, continua solicitando até receber nome válido:
        while (servidor.usuarioExiste(nomeDesejado) || nomeDesejado.trim().isEmpty() || nomeDesejado.contains(" ")) {
            Mensagem erro = new Mensagem("SISTEMA", null, 
                "Nome já em uso ou inválido (vazio ou contém espaço). Digite outro nome:");
            saida.writeObject(erro);
            resposta = (Mensagem) entrada.readObject();
            nomeDesejado = resposta.getConteudo();
        }
        
        this.nomeUsuario = nomeDesejado;
        servidor.adicionarCliente(nomeUsuario, this);  // Registra cliente no servidor.
        
        // Confirma conexão para o cliente:
        Mensagem confirmacao = new Mensagem("SISTEMA", nomeUsuario, 
            "Bem-vindo ao chat, " + nomeUsuario + "!");
        saida.writeObject(confirmacao);
        
        // Notifica outros usuários:
        Mensagem notificacao = new Mensagem("SISTEMA", null, 
            nomeUsuario + " entrou no chat!");
        servidor.broadcast(notificacao, nomeUsuario);
    }
    
    // Processa mensagens recebidas, identificando tipo de mensagem ou comando e tomando a ação devida:
    private void processarMensagem(Mensagem mensagem) {
        String conteudo = mensagem.getConteudo();
        
        // Comando para listar usuários:
        if (conteudo.startsWith("/usuarios")) {
            String lista = servidor.listarUsuarios();
            Mensagem resposta = new Mensagem("SISTEMA", mensagem.getRemetente(), lista);  // Cria resposta direcionada apenas ao solicitante.
            enviarMensagem(resposta);
            return;
        }
        
        // Comando para mensagem privada:
        if (conteudo.startsWith("/privado:")) {
            String conteudoPrivado = conteudo.substring(9); // Remove "/privado:"
            mensagem.setConteudo(conteudoPrivado);
            servidor.enviarMensagemPrivada(mensagem);
            return;
        }
        
        // Mensagem para todos (broadcast):
        if (mensagem.getDestinatario() == null) {
            servidor.broadcast(mensagem, mensagem.getRemetente());
        }
    }
    
    // Método para enviar mensagem ao cliente específico:
    public void enviarMensagem(Mensagem mensagem) {
        try {
            saida.writeObject(mensagem);
        } catch (IOException e) {
            System.err.println("Erro ao enviar mensagem para " + nomeUsuario);
            desconectar();
        }
    }
    
    // Remove usuário que quer se desconectar do servidor, notifica os demais e fecha recursos de rede:
    private void desconectar() {
        try {
            if (nomeUsuario != null) {
                servidor.removerCliente(nomeUsuario);
                
                // Notifica outros usuários:
                Mensagem despedida = new Mensagem("SISTEMA", null, 
                    nomeUsuario + " saiu do chat!");
                servidor.broadcast(despedida, nomeUsuario);
            }
            
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Erro ao desconectar cliente: " + e.getMessage());
        }
    }
}