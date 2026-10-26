# Health Chain

Health Chain is a JavaFX-based healthcare management application for hospitals, doctors, and patients. The system provides role-based dashboards for administrators, hospitals, doctors, and patients, along with appointment workflows, authentication, verification, and basic messaging features.

## Overview

This project was designed as a healthcare portal with a modular structure and database-backed data model. It includes:

- User registration and login
- Email verification and password recovery flow
- Role-based access (Admin, Hospital, Doctor, Patient)
- Hospital, doctor, and patient management dashboards
- Appointment handling
- Chat support between users and doctors
- Database migration utilities for schema setup

## Tech Stack

- Java 23
- JavaFX 17
- Maven
- MySQL
- JavaMail
- io.github.cdimascio:java-dotenv
- JUnit 5

## Project Structure

```text
Health-Chain-master/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com/aoop/healthchain/
│   │   │   │   ├── auth/
│   │   │   │   ├── chat/
│   │   │   │   ├── model/
│   │   │   │   ├── util/
│   │   │   │   ├── App.java
│   │   │   │   └── ...
│   │   │   └── database/
│   │   └── resources/
│   │       └── com/aoop/healthchain/
│   └── test/
├── pom.xml
├── .env
├── mykeystore.jks
├── mvnw
├── mvnw.cmd
└── README.md
```

## Prerequisites

Before running the project, make sure you have:

- JDK 23 or newer
- Maven (or use the bundled Maven wrapper)
- MySQL server running
- A configured environment file (`.env`)

## Environment Setup

The project reads database and email configuration from a `.env` file in the project root. Example configuration:

```env
DATABASE_HOSTNAME=localhost
DATABASE_PORT=3306
DATABASE_NAME=healthchain
DATABASE_USERNAME=root
DATABASE_SECRET=your_password
ENCRYPTION_KEYSTORE_PATH=mykeystore.jks
ENCRYPTION_SECRET=secret
ENCRYPTION_KEYSTORE_PASSWORD=secret
ENCRYPTION_ALIAS=myalias
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_MAIL=your_email@gmail.com
SMTP_SECRET=your_app_password
```

Create the database and update the values above to match your local environment.

## Build and Run

### Windows

```bash
mvnw.cmd clean javafx:run
```

### macOS/Linux

```bash
./mvnw clean javafx:run
```

This launches the JavaFX application and opens the login screen.

## Database Migration

The project includes database migration classes under `src/main/java/database`.

To apply migrations, run the `database.RunMigration` entry point from your IDE or a Java launcher. The migration layer creates tables for:

- users
- hospitals
- doctors
- appointments
- migration metadata

## Default Roles

The application supports the following user roles:

- ADMIN
- HOSPITAL
- DOCTOR
- PATIENT

The login flow routes each user to the corresponding dashboard after authentication.

## Notes

- The app uses JavaFX FXML views stored under `src/main/resources/com/aoop/healthchain`.
- Email verification is integrated into the registration/login flow.
- A chat server launcher is included for real-time chat functionality.

## License

This project is provided as-is for academic and learning purposes.

## Author

Forhad Hasan


