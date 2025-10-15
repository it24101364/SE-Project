package com.example.webapp.strategy;

public class PaymentContext {
    private PaymentStrategy paymentStrategy;

    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    public String executePayment(double amount) {
        if (paymentStrategy == null) {
            throw new IllegalStateException("No payment strategy selected");
        }
        return paymentStrategy.pay(amount);
    }
}
