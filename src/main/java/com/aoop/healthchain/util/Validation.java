package com.aoop.healthchain.util;

public class Validation {
    public static boolean isEmailValid(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    public static boolean isValidPhoneNumber(String number) {
        // Assuming a valid phone number is 11 digits long and starting with 01 or +8801 ex. 01752424377 or +8801752424377
        return number.matches("^(01[3-9]\\d{8}|\\+8801[3-9]\\d{8})$");
    }
}
