package com.example.webapp.dto; // or a dto package

public class MonthlySalesDTO {
    private String monthName;
    private double totalAmount;

    public MonthlySalesDTO(String monthName, double totalAmount) {
        this.monthName = monthName;
        this.totalAmount = totalAmount;
    }

    public String getMonthName() {
        return monthName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}
