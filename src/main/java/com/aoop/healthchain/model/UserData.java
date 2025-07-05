package com.aoop.healthchain.model;

// Data class to hold user information
public record UserData(
    Long id,
    String email,
    String fullName,
    String phoneNumber,
    String bloodGroup,
    String password,
    String role,
    boolean isVerified
) {
    // Static factory method to create a new user
    public static UserData createNew(
        String fullName,
        String email,
        String password,
        String role,
        boolean isVerified
    ) {
        return new UserData(
            null, // id will be assigned by database
            email,
            fullName,
            "", // phoneNumber
            "", // bloodGroup
            password,
            role,
            isVerified
        );
    }
}
