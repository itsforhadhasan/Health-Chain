package com.aoop.healthchain.model;

public record DoctorData(
    int id,
    int userId,
    String name,
    String email,
    String specialization,
    String phone,
    String address,
    String city,
    String state,
    String zip,
    String licenseNumber,
    String issueDate,
    String expiryDate,
    String status,
    String createdAt,
    String updatedAt,
    String hospitalName
) {} 