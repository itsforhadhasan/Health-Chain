package com.aoop.healthchain.chat;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String messageId;
    private String senderId;
    private String receiverId;
    private String content;
    private LocalDateTime timestamp;
    private MessageType type;

    public enum MessageType {
        TEXT, IMAGE, FILE, SYSTEM
    }

    public ChatMessage(String senderId, String receiverId, String content) {
        this.messageId = java.util.UUID.randomUUID().toString();
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.type = MessageType.TEXT;
    }

    public ChatMessage(String senderId, String receiverId, String content, MessageType type) {
        this(senderId, receiverId, content);
        this.type = type;
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "messageId='" + messageId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", type=" + type +
                '}';
    }
} 