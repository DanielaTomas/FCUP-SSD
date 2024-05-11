package BlockChain;
import Auctions.CryptoUtils;
import Auctions.Wallet;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/** Class Transaction: Represents a transaction between two parties in a blockchain. */
public class Transaction implements Serializable {
    //private String auctionId;
    private PublicKey senderPublicKey;
    private PublicKey receiverPublicKey;
    private double amount;
    private byte[] signature;

    /**
     * Constructs a transaction with the specified sender, receiver, and amount.
     *
     * @param receiver The public key of the receiver.
     * @param amount   The amount of the transaction.
     */
    public Transaction(PublicKey receiver, double amount) {
        this.senderPublicKey = Wallet.getInstance().getPublicKey();
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
        this.signature = CryptoUtils.sign(privateKey, (senderPublicKey.toString() + receiverPublicKey.toString() + amount).getBytes());
    }

    /**
     * Verifies the signature of the transaction.
     *
     * @return True if the signature is valid, false otherwise.
     */
    public boolean verifySignature() {
        return CryptoUtils.verifySignature(senderPublicKey, (senderPublicKey.toString() + receiverPublicKey.toString() + amount).getBytes(), signature);
    }

    /**
     * Returns a string representation of the transaction.
     *
     * @return A string representing the transaction details.
     */
    @Override
    public String toString() {
        return "\n\tTransaction Details:\n" +
                //"\t\tAuction: " + auctionId + "\n" +
                "\t\tSender: " + senderPublicKey.toString() + "\n" +
                "\t\tReceiver: " + receiverPublicKey.toString() + "\n" +
                "\t\tAmount: " + amount + "\n" +
                "\t\tSignature: " + (signature != null ? Arrays.toString(signature) : "null") + "\n";
    }

    /**
     * Custom serialization method for writing object state.
     *
     * @param out The ObjectOutputStream to write object state to.
     * @throws IOException If an I/O error occurs while writing the object.
     */
    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        //out.writeObject(auctionId);
        out.writeObject(senderPublicKey.getEncoded());
        out.writeObject(receiverPublicKey.getEncoded());
        out.writeDouble(amount);
        out.writeObject(signature);
    }

    /**
     * Custom deserialization method for reading object state.
     *
     * @param in The ObjectInputStream to read object state from.
     * @throws IOException            If an I/O error occurs while reading the object.
     * @throws ClassNotFoundException If the class of a serialized object cannot be found.
     */
    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException {
        //this.auctionId = (String) in.readObject();
        byte[] senderKey = (byte[]) in.readObject();
        byte[] receiverKey = (byte[]) in.readObject();
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.senderPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(senderKey));
        this.receiverPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(receiverKey));
        this.amount = in.readDouble();
        this.signature = (byte[]) in.readObject();
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

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }
}
