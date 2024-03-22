package org.example;
//TODO
// import java.security.PrivateKey;
// import java.security.PublicKey;
public class Transaction {
    private String sender;
    private String receiver;
    private double amount;

    public Transaction(String sender, String receiver, double amount) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
    }

    public String getSender() {
        return this.sender;
    }

    public String getReceiver() {
        return this.receiver;
    }

    public double getAmount() {
        return this.amount;
    }
}
