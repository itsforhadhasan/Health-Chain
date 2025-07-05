package com.aoop.healthchain;

import com.aoop.healthchain.chat.ChatClient;
import com.aoop.healthchain.chat.ChatMessage;
import com.aoop.healthchain.model.User;
import com.aoop.healthchain.model.UserData;
import com.aoop.healthchain.util.DatabaseConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DoctorChatController implements Initializable {
    
    @FXML private ListView<UserData> patientsListView;
    @FXML private TextField searchPatientsField;
    @FXML private ImageView selectedPatientImage;
    @FXML private Label selectedPatientName;
    @FXML private Label selectedPatientStatus;
    @FXML private VBox messagesContainer;
    @FXML private TextField messageInputField;
    @FXML private Button sendButton;
    @FXML private ScrollPane messagesScrollPane;
    
    private UserData currentDoctor;
    private UserData selectedPatient;
    private ChatClient chatClient;
    private ObservableList<UserData> patientsList = FXCollections.observableArrayList();
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupUI();
        setupEventHandlers();
        loadPatients();
    }
    
    public void setCurrentDoctor(UserData doctor) {
        this.currentDoctor = doctor;
        if (doctor != null) {
            initializeChatClient();
        }
    }
    
    private void setupUI() {
        // Setup patients list view
        patientsListView.setCellFactory(param -> new PatientListCell());
        patientsListView.setItems(patientsList);
        
        // Setup search functionality
        searchPatientsField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterPatients(newValue);
        });
        
        // Disable send button initially
        sendButton.setDisable(true);
        messageInputField.setDisable(true);
    }
    
    private void setupEventHandlers() {
        // Patient selection
        patientsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectPatient(newValue);
            }
        });
    }
    
    private void initializeChatClient() {
        if (currentDoctor != null) {
            chatClient = new ChatClient(String.valueOf(currentDoctor.id()), this::handleIncomingMessage);
            if (chatClient.connect()) {
                System.out.println("Doctor connected to chat server");
            } else {
                showAlert("Connection Error", "Failed to connect to chat server", Alert.AlertType.ERROR);
            }
        }
    }
    
    private void loadPatients() {
        try {
            DatabaseConnection dbConnection = new DatabaseConnection();
            User userModel = new User(dbConnection.getConnection());
            List<UserData> allUsers = userModel.getAllUsers();
            
            // Filter only patients (users with PATIENT role)
            List<UserData> patients = allUsers.stream()
                .filter(user -> "PATIENT".equals(user.role()))
                .collect(Collectors.toList());
            
            patientsList.clear();
            patientsList.addAll(patients);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load patients", Alert.AlertType.ERROR);
        }
    }
    
    private void filterPatients(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            patientsListView.setItems(patientsList);
        } else {
            ObservableList<UserData> filteredList = FXCollections.observableArrayList();
            for (UserData patient : patientsList) {
                if (patient.fullName().toLowerCase().contains(searchText.toLowerCase()) ||
                    patient.email().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredList.add(patient);
                }
            }
            patientsListView.setItems(filteredList);
        }
    }
    
    private void selectPatient(UserData patient) {
        this.selectedPatient = patient;
        
        // Update UI
        selectedPatientName.setText(patient.fullName());
        selectedPatientStatus.setText("Online"); // TODO: Implement real online status
        
        // Enable messaging
        sendButton.setDisable(false);
        messageInputField.setDisable(false);
        messageInputField.requestFocus();
        
        // Clear previous messages
        messagesContainer.getChildren().clear();
        
        // Load chat history
        loadChatHistory();
    }
    
    private void loadChatHistory() {
        if (selectedPatient == null || currentDoctor == null) return;
        
        // TODO: Load chat history from database
        // For now, we'll just show a welcome message
        addMessage("Hello! I'm Dr. " + currentDoctor.fullName() + ". How can I help you today?", false, true);
    }
    
    @FXML
    private void handleSendMessage() {
        String messageText = messageInputField.getText().trim();
        if (messageText.isEmpty() || selectedPatient == null || chatClient == null) return;
        
        // Send message
        chatClient.sendMessage(String.valueOf(selectedPatient.id()), messageText);
        
        // Do NOT add the message to the UI here. 
        // The server will echo it back and handleIncomingMessage will display it.

        // Clear input field
        messageInputField.clear();
    }
    
    private void handleIncomingMessage(ChatMessage message) {
        Platform.runLater(() -> {
            if (currentDoctor == null || selectedPatient == null) {
                return; // Not in a chat session
            }

            String currentDoctorId = String.valueOf(currentDoctor.id());
            String selectedPatientId = String.valueOf(selectedPatient.id());

            // Determine if the message is from the current user or the selected patient
            boolean isFromMe = message.getSenderId().equals(currentDoctorId);
            boolean isFromSelectedPatient = message.getSenderId().equals(selectedPatientId);

            // Only display the message if it's part of the current conversation
            if ((isFromMe && message.getReceiverId().equals(selectedPatientId)) || 
                (isFromSelectedPatient && message.getReceiverId().equals(currentDoctorId))) {
                addMessage(message.getContent(), isFromMe, false);
            }
        });
    }
    
    private void addMessage(String content, boolean isFromMe, boolean isSystem) {
        HBox messageBox = new HBox(10);
        messageBox.setStyle("-fx-padding: 10; -fx-alignment: " + (isFromMe ? "CENTER_RIGHT" : "CENTER_LEFT"));
        
        VBox messageContent = new VBox(5);
        messageContent.setStyle("-fx-background-color: " + 
            (isSystem ? "#f0f0f0" : (isFromMe ? "#3b82f6" : "#e5e7eb")) + 
            "; -fx-background-radius: 10; -fx-padding: 10; -fx-max-width: 300;");
        
        Text messageText = new Text(content);
        messageText.setStyle("-fx-fill: " + (isFromMe ? "white" : "black") + "; -fx-font-size: 14;");
        messageText.setWrappingWidth(280);
        
        messageContent.getChildren().add(messageText);
        
        if (!isSystem) {
            Text timeText = new Text(java.time.LocalDateTime.now().format(timeFormatter));
            timeText.setStyle("-fx-fill: #6b7280; -fx-font-size: 12;");
            messageContent.getChildren().add(timeText);
        }
        
        messageBox.getChildren().add(messageContent);
        
        if (isFromMe) {
            messageBox.getChildren().add(0, new Region()); // Spacer
        }
        
        messagesContainer.getChildren().add(messageBox);
        
        // Scroll to bottom
        Platform.runLater(() -> {
            messagesScrollPane.setVvalue(1.0);
        });
    }
    
    @FXML
    private void handleCall() {
        showAlert("Call Feature", "Voice call feature coming soon!", Alert.AlertType.INFORMATION);
    }
    
    @FXML
    private void handleVideoCall() {
        showAlert("Video Call Feature", "Video call feature coming soon!", Alert.AlertType.INFORMATION);
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // Custom cell for patient list
    private class PatientListCell extends ListCell<UserData> {
        @Override
        protected void updateItem(UserData patient, boolean empty) {
            super.updateItem(patient, empty);
            
            if (empty || patient == null) {
                setGraphic(null);
                setText(null);
            } else {
                HBox cell = new HBox(10);
                cell.setStyle("-fx-padding: 10; -fx-alignment: CENTER_LEFT;");
                
                ImageView avatar = new ImageView(new Image(getClass().getResourceAsStream("/com/aoop/healthchain/images/default_male.png")));
                avatar.setFitHeight(40);
                avatar.setFitWidth(40);
                
                VBox info = new VBox(2);
                Label name = new Label(patient.fullName());
                name.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
                Label email = new Label(patient.email());
                email.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12;");
                
                info.getChildren().addAll(name, email);
                cell.getChildren().addAll(avatar, info);
                
                setGraphic(cell);
                setText(null);
            }
        }
    }
} 