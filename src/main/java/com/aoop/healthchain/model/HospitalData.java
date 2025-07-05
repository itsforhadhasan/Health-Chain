package com.aoop.healthchain.model;

public record HospitalData(
    int id,
    String name,
    String email,
    String phone,
    String address,
    String city,
    String state,
    String zip,
    String website,
    String licenseNumber,
    int establishmentYear,
    int bedCount,
    String status,
    int userId,
    String userEmail,
    String userRole,
    String createdAt,
    String updatedAt
) {
    // Constructor with default values for new hospital creation
    public static HospitalData createNew(
        String name,
        String email,
        String phone,
        String address,
        String city,
        String state,
        String zip,
        String website,
        String licenseNumber,
        int establishmentYear,
        int bedCount
    ) {
        return new HospitalData(
            0, // id will be assigned by database
            name,
            email,
            phone,
            address,
            city,
            state,
            zip,
            website,
            licenseNumber,
            establishmentYear,
            bedCount,
            "ACTIVE", // default status
            0, // userId will be assigned when creating user
            null, // userEmail will be assigned when creating user
            null, // userRole will be assigned when creating user
            null, // createdAt will be assigned by database
            null  // updatedAt will be assigned by database
        );
    }
}
