package Trabalho_2.chat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Mensagem implements Serializable {
    private String remetente;
    private String destinatario; // null para broadcast
    private String conteudo;
    private LocalDateTime DataEHorario;

    public Mensagem(String remetente, String destinatario, String conteudo) {
        this.remetente = remetente;
        this.destinatario = destinatario;
        this.conteudo = conteudo;
        this.DataEHorario = LocalDateTime.now();
    }

    // Getters
    public String getRemetente() { return remetente; }
    public String getDestinatario() { return destinatario; }
    public String getConteudo() { return conteudo; }
    public LocalDateTime getHorario() { return DataEHorario; }

    // Setters para processamento no servidor
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String destino = (destinatario == null) ? "Todos" : destinatario;
        return "[" + DataEHorario.format(formatter) + "] " + remetente + " -> " + destino + ": " + conteudo;
    }
}
