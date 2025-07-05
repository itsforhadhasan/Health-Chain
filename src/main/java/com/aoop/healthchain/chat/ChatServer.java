package com.aoop.healthchain.chat;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 8080;
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final Map<String, List<ChatMessage>> chatHistory = new ConcurrentHashMap<>();
    private ServerSocket serverSocket;
    private boolean running = false;

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("Chat server started on port " + PORT);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting chat server: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error stopping chat server: " + e.getMessage());
        }
    }

    public void addClient(String userId, ClientHandler handler) {
        clients.put(userId, handler);
        System.out.println("Client connected: " + userId);
    }

    public void removeClient(String userId) {
        clients.remove(userId);
        System.out.println("Client disconnected: " + userId);
    }

    public void broadcastMessage(ChatMessage message) {
        // Store message in chat history
        String chatId = getChatId(message.getSenderId(), message.getReceiverId());
        chatHistory.computeIfAbsent(chatId, k -> new ArrayList<>()).add(message);

        // Send to receiver if online
        ClientHandler receiver = clients.get(message.getReceiverId());
        if (receiver != null) {
            receiver.sendMessage(message);
        }

        // Send confirmation to sender
        ClientHandler sender = clients.get(message.getSenderId());
        if (sender != null) {
            sender.sendMessage(message);
        }
    }

    public List<ChatMessage> getChatHistory(String userId1, String userId2) {
        String chatId = getChatId(userId1, userId2);
        return chatHistory.getOrDefault(chatId, new ArrayList<>());
    }

    private String getChatId(String userId1, String userId2) {
        // Create a consistent chat ID regardless of sender/receiver order
        return userId1.compareTo(userId2) < 0 ? 
            userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }

    public Set<String> getOnlineUsers() {
        return clients.keySet();
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
} 