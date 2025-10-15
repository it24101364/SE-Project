package com.example.webapp.strategy;

public class CashOnDeliveryStrategy implements PaymentStrategy {

    @Override
    public String pay(double amount) {
        return "Payment of Rs. " + amount + " will be collected on delivery.";
    }
}
