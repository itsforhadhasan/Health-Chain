package com.aoop.healthchain;

import com.aoop.healthchain.chat.ChatClient;
import com.aoop.healthchain.chat.ChatMessage;
import com.aoop.healthchain.model.Doctor;
import com.aoop.healthchain.model.DoctorData;
import com.aoop.healthchain.model.User;
import com.aoop.healthchain.model.UserData;
import com.aoop.healthchain.util.DatabaseConnection;
import com.aoop.healthchain.util.FXMLScene;
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
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class PatientChatController implements Initializable {
    
    @FXML private ListView<DoctorData> doctorsListView;
    @FXML private TextField searchDoctorsField;
    @FXML private ImageView selectedDoctorImage;
    @FXML private Label selectedDoctorName;
    @FXML private Label selectedDoctorStatus;
    @FXML private VBox messagesContainer;
    @FXML private TextField messageInputField;
    @FXML private Button sendButton;
    @FXML private ScrollPane messagesScrollPane;
    
    private UserData currentUser;
    private DoctorData selectedDoctor;
    private ChatClient chatClient;
    private ObservableList<DoctorData> doctorsList = FXCollections.observableArrayList();
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupUI();
        setupEventHandlers();
        loadDoctors();
    }
    
    public void setCurrentUser(UserData user) {
        this.currentUser = user;
        if (user != null) {
            initializeChatClient();
        }
    }
    
    private void setupUI() {
        // Setup doctors list view
        doctorsListView.setCellFactory(param -> new DoctorListCell());
        doctorsListView.setItems(doctorsList);
        
        // Setup search functionality
        searchDoctorsField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterDoctors(newValue);
        });
        
        // Disable send button initially
        sendButton.setDisable(true);
        messageInputField.setDisable(true);
    }
    
    private void setupEventHandlers() {
        // Doctor selection
        doctorsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectDoctor(newValue);
            }
        });
    }
    
    private void initializeChatClient() {
        if (currentUser != null) {
            chatClient = new ChatClient(currentUser.id().toString(), this::handleIncomingMessage);
            if (chatClient.connect()) {
                System.out.println("Connected to chat server");
            } else {
                showAlert("Connection Error", "Failed to connect to chat server", Alert.AlertType.ERROR);
            }
        }
    }
    
    private void loadDoctors() {
        try {
            DatabaseConnection dbConnection = new DatabaseConnection();
            Doctor doctorModel = new Doctor(dbConnection.getConnection());
            List<DoctorData> doctors = doctorModel.findAll();
            doctorsList.clear();
            doctorsList.addAll(doctors);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load doctors", Alert.AlertType.ERROR);
        }
    }
    
    private void filterDoctors(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            doctorsListView.setItems(doctorsList);
        } else {
            ObservableList<DoctorData> filteredList = FXCollections.observableArrayList();
            for (DoctorData doctor : doctorsList) {
                if (doctor.name().toLowerCase().contains(searchText.toLowerCase()) ||
                    doctor.specialization().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredList.add(doctor);
                }
            }
            doctorsListView.setItems(filteredList);
        }
    }
    
    private void selectDoctor(DoctorData doctor) {
        this.selectedDoctor = doctor;
        
        // Update UI
        selectedDoctorName.setText("Dr. " + doctor.name());
        selectedDoctorStatus.setText("Online"); // TODO: Implement real online status
        
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
        if (selectedDoctor == null || currentUser == null) return;
        
        // TODO: Load chat history from database
        // For now, we'll just show a welcome message
        addMessage("Welcome! How can I help you today?", false, true);
    }
    
    @FXML
    private void handleSendMessage() {
        String messageText = messageInputField.getText().trim();
        if (messageText.isEmpty() || selectedDoctor == null || chatClient == null) return;
        
        // Send message using the correct userId for the doctor
        chatClient.sendMessage(String.valueOf(selectedDoctor.userId()), messageText);
        
        // Do NOT add the message to the UI here. 
        // The server will echo it back and handleIncomingMessage will display it.
        
        // Clear input field
        messageInputField.clear();
    }
    
    private void handleIncomingMessage(ChatMessage message) {
        Platform.runLater(() -> {
            if (currentUser == null || selectedDoctor == null) {
                return; // Not in a chat session
            }

            String currentUserId = String.valueOf(currentUser.id());
            String selectedDoctorUserId = String.valueOf(selectedDoctor.userId());

            // Determine if the message is from the current user or the selected doctor
            boolean isFromMe = message.getSenderId().equals(currentUserId);
            boolean isFromSelectedDoctor = message.getSenderId().equals(selectedDoctorUserId);

            // Only display the message if it's part of the current conversation
            if ((isFromMe && message.getReceiverId().equals(selectedDoctorUserId)) || 
                (isFromSelectedDoctor && message.getReceiverId().equals(currentUserId))) {
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
    
    // Custom cell for doctor list
    private class DoctorListCell extends ListCell<DoctorData> {
        @Override
        protected void updateItem(DoctorData doctor, boolean empty) {
            super.updateItem(doctor, empty);
            
            if (empty || doctor == null) {
                setGraphic(null);
                setText(null);
            } else {
                HBox cell = new HBox(10);
                cell.setStyle("-fx-padding: 10; -fx-alignment: CENTER_LEFT;");
                
                ImageView avatar = new ImageView(new Image(getClass().getResourceAsStream("/com/aoop/healthchain/images/default_male.png")));
                avatar.setFitHeight(40);
                avatar.setFitWidth(40);
                
                VBox info = new VBox(2);
                Label name = new Label("Dr. " + doctor.name());
                name.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
                Label specialization = new Label(doctor.specialization());
                specialization.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12;");
                
                info.getChildren().addAll(name, specialization);
                cell.getChildren().addAll(avatar, info);
                
                setGraphic(cell);
                setText(null);
            }
        }
    }
} 