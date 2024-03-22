package org.example;
import java.security.*;

public class Transaction {
    private PublicKey senderPublicKey;
    private PublicKey receiverPublicKey;
    private double amount;
    private byte[] signature;

    public Transaction(PublicKey sender, PublicKey receiver, double amount) {
        this.senderPublicKey = sender;
        this.receiverPublicKey = receiver;
        this.amount = amount;
        this.signature = null;
    }

    public void signTransaction(PrivateKey privateKey) {
        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(privateKey);
            byte[] transactionData = (senderPublicKey.toString() + receiverPublicKey.toString() + Double.toString(amount)).getBytes();
            sign.update(transactionData);
            this.signature = sign.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean verifySignature() {
        if (signature == null) return false;
        try {
            Signature verifySign = Signature.getInstance("SHA256withRSA");
            verifySign.initVerify(senderPublicKey);
            byte[] transactionData = (senderPublicKey.toString() + receiverPublicKey.toString() + Double.toString(amount)).getBytes();
            verifySign.update(transactionData);
            return verifySign.verify(signature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048); // Key size
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public PublicKey getSenderPublicKey() {
        return this.senderPublicKey;
    }

    public PublicKey getReceiverPublicKey() {
        return this.receiverPublicKey;
    }

    public double getAmount() {
        return this.amount;
    }

    public byte[] getSignature() {
        return signature;
    }
}
