package com.example.webapp.strategy;
public class CardPaymentStrategy implements PaymentStrategy {

    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;

    public CardPaymentStrategy(String cardNumber, String cardHolderName, String expiryDate, String cvv) {
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }

    @Override
    public String pay(double amount) {
        // Simulate card payment processing
        return "Paid Rs. " + amount + " successfully using Card (" + maskCardNumber(cardNumber) + ")";
    }
    private String maskCardNumber(String cardNumber) {
        if (cardNumber.length() >= 4)
            return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        return "****";
    }
}
