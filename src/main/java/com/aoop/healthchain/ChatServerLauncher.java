package com.aoop.healthchain;

import com.aoop.healthchain.chat.ChatServer;

public class ChatServerLauncher {
    public static void main(String[] args) {
        System.out.println("Starting Health Chain Chat Server...");
        ChatServer server = new ChatServer();
        server.start();
    }
} 