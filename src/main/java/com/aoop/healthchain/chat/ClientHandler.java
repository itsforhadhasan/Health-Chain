package com.aoop.healthchain.chat;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ChatServer server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String userId;

    public ClientHandler(Socket socket, ChatServer server) {
        this.clientSocket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

            // First message should be the user ID for registration
            String registrationMessage = (String) in.readObject();
            if (registrationMessage.startsWith("REGISTER:")) {
                this.userId = registrationMessage.substring(9);
                server.addClient(userId, this);
                System.out.println("User registered: " + userId);
            }

            // Handle incoming messages
            while (!clientSocket.isClosed()) {
                try {
                    Object obj = in.readObject();
                    if (obj instanceof ChatMessage) {
                        ChatMessage message = (ChatMessage) obj;
                        server.broadcastMessage(message);
                    }
                } catch (EOFException e) {
                    break; // Client disconnected
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    public void sendMessage(ChatMessage message) {
        try {
            if (out != null && !clientSocket.isClosed()) {
                out.writeObject(message);
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("Error sending message to " + userId + ": " + e.getMessage());
            closeConnection();
        }
    }

    public void closeConnection() {
        try {
            if (userId != null) {
                server.removeClient(userId);
            }
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    public String getUserId() {
        return userId;
    }
} 