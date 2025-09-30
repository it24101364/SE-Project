package com.example.webapp.dto; // or a dto package

public class ProductSalesDTO {
    private String productName;
    private double totalAmount;

    public ProductSalesDTO(String productName, double totalAmount) {
        this.productName = productName;
        this.totalAmount = totalAmount;
    }

    public String getProductName() {
        return productName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}
