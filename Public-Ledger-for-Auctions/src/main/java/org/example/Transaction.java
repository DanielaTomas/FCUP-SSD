package org.example;
import java.security.*;
import java.util.Arrays;

/** Class Transaction: Represents a transaction between two parties in a blockchain. */
public class Transaction {
    private PublicKey senderPublicKey;
    private PublicKey receiverPublicKey;
    private double amount;
    private byte[] signature;

    /**
     * Constructs a transaction with the specified sender, receiver, and amount.
     *
     * @param sender   The public key of the sender.
     * @param receiver The public key of the receiver.
     * @param amount   The amount of the transaction.
     */
    public Transaction(PublicKey sender, PublicKey receiver, double amount) {
        this.senderPublicKey = sender;
        this.receiverPublicKey = receiver;
        this.amount = amount;
        this.signature = null;
    }


    /**
     * Signs the transaction using the provided private key.
     *
     * @param privateKey The private key to sign the transaction.
     */
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

    /**
     * Verifies the signature of the transaction.
     *
     * @return True if the signature is valid, false otherwise.
     */
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

    /**
     * Generates a new RSA key pair.
     *
     * @return The generated RSA key pair.
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048); // Key size
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a string representation of the transaction.
     *
     * @return A string representing the transaction details.
     */
    @Override
    public String toString() {
        return "\n\tTransaction Details:\n" +
                "\t\tSender: " + senderPublicKey.toString() + "\n" +
                "\t\tReceiver: " + receiverPublicKey.toString() + "\n" +
                "\t\tAmount: " + amount + "\n" +
                "\t\tSignature: " + (signature != null ? Arrays.toString(signature) : "null") + "\n";
    }

    /**
     * Retrieves the sender's public key.
     *
     * @return The sender's public key.
     */
    public PublicKey getSenderPublicKey() {
        return this.senderPublicKey;
    }

    /**
     * Retrieves the receiver's public key.
     *
     * @return The receiver's public key.
     */
    public PublicKey getReceiverPublicKey() {
        return this.receiverPublicKey;
    }

    /**
     * Retrieves the amount of the transaction.
     *
     * @return The amount of the transaction.
     */
    public double getAmount() {
        return this.amount;
    }

    /**
     * Retrieves the signature of the transaction.
     *
     * @return The signature of the transaction.
     */
    public byte[] getSignature() {
        return signature;
    }
}
