# Sistema de Chat TCP

Sistema de chat em tempo real implementado em Java utilizando protocolo TCP com arquitetura cliente-servidor. Permite múltiplos usuários conectados simultaneamente, suportando mensagens públicas (broadcast) e privadas através de interface gráfica.

## Pré-requisitos

- Java Development Kit (JDK) 8 ou superior
- IDE Java (opcional): Eclipse, IntelliJ IDEA, NetBeans, VS Code

## Estrutura do Projeto

```
Trabalho_2/
├── servidor/
│   ├── Servidor.java
│   └── ClienteHandler.java
├── gui/
│   └── ClienteSwing.java
└── chat/
    └── Mensagem.java
```

## Instruções de Compilação

### Via Linha de Comando:
- **Navegue até o diretório pai do projeto**
- **Compile todas as classes:**
```bash
javac Trabalho_2/servidor/*.java
javac Trabalho_2/gui/*.java
javac Trabalho_2/chat/*.java
```

### Via IDE:
- **Importe o projeto na IDE**
- **Compile o projeto usando as ferramentas da IDE**

## Instruções de Execução

### Iniciando o Servidor:

**Via linha de comando:**
```bash
java Trabalho_2.servidor.Servidor
```

**Via IDE:**
- Execute a classe `Servidor.java`

**Saída esperada:**
```
=== SERVIDOR DE CHAT TCP ===
Servidor iniciado na porta 12345
Aguardando conexões...

Digite 'sair' para parar o servidor.
```

### Iniciando a Interface do Usuário:

**Via linha de comando:**
```bash
java Trabalho_2.gui.ClienteSwing
```

**Via IDE:**
- Execute a classe `ClienteSwing.java`

## Conectando ao Chat:

1. Janela do cliente será aberta
2. Digite seu nome de usuário (ou use o nome gerado aleatoriamente)
3. Clique em "Conectar" (ou pressione Enter)
4. Aguarde confirmação de conexão

## Como Usar o Chat:

### 1) Enviar Mensagens Públicas:
- Digite sua mensagem no campo inferior
- Pressione Enter ou clique em "Enviar"
- A mensagem será enviada para todos os usuários conectados

### 2) Enviar Mensagens Privadas:
- Use o formato: `/privado nome_usuario sua_mensagem`
- Exemplo: `/privado Fulano Olá, como você está?`
- Pressione Enter para enviar

### 3) Listar Usuários Conectados:
- **Opção 1:** Clique no botão "Listar Usuários"
- **Opção 2:** Digite `/usuarios` no campo de mensagem

### 4) Desconectar:
- **Opção 1:** Clique no botão "Desconectar"
- **Opção 2:** Feche a janela do cliente

## Alterações de Configurações

Para funcionar em diferentes hosts e portas na rede local, modifique as constantes:

**Na classe ClienteSwing.java:**
```java
private static final String HOST = "127.0.0.1";  // Altere para novo IP do servidor
private static final int PORTA = 12345;  // Altere para nova porta
```

**Na classe Servidor.java:**
```java
private static final int PORTA = 12345;  // Altere para nova porta
```

## Funcionalidades

- Conexão simultânea de múltiplos usuários
- Mensagens públicas (broadcast)
- Mensagens privadas entre usuários
- Listagem de usuários conectados
- Interface gráfica intuitiva
- Timestamps automáticos
- Validação de nomes únicos
- Notificações de entrada/saída de usuários

## Configuração Padrão

- **Host:** 127.0.0.1 (localhost)
- **Porta:** 12345
- **Protocolo:** TCP

## 📝 Comandos Disponíveis

| Comando | Descrição |
|---------|-----------|
| `/usuarios` | Lista todos os usuários conectados |
| `/privado <usuário> <mensagem>` | Envia mensagem privada |
| Botão "Ajuda" | Exibe menu com funcionalidades |
| Botão "Desconectar" | Sair do chat |
