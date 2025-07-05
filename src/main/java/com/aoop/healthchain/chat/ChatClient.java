package com.aoop.healthchain.chat;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String userId;
    private Consumer<ChatMessage> messageHandler;
    private boolean connected = false;

    public ChatClient(String userId, Consumer<ChatMessage> messageHandler) {
        this.userId = userId;
        this.messageHandler = messageHandler;
    }

    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Register with the server
            out.writeObject("REGISTER:" + userId);
            out.flush();

            connected = true;
            startMessageListener();
            return true;
        } catch (IOException e) {
            System.err.println("Error connecting to chat server: " + e.getMessage());
            return false;
        }
    }

    private void startMessageListener() {
        Thread listener = new Thread(() -> {
            try {
                while (connected && !socket.isClosed()) {
                    try {
                        Object obj = in.readObject();
                        if (obj instanceof ChatMessage) {
                            ChatMessage message = (ChatMessage) obj;
                            // Handle message on JavaFX thread
                            javafx.application.Platform.runLater(() -> {
                                if (messageHandler != null) {
                                    messageHandler.accept(message);
                                }
                            });
                        }
                    } catch (EOFException e) {
                        break; // Server disconnected
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error in message listener: " + e.getMessage());
            } finally {
                connected = false;
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    public void sendMessage(String receiverId, String content) {
        if (!connected) {
            System.err.println("Not connected to chat server");
            return;
        }

        try {
            ChatMessage message = new ChatMessage(userId, receiverId, content);
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
            connected = false;
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    public String getUserId() {
        return userId;
    }
} 