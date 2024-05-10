package BlockChain;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.List;

/** Class Transaction: Represents a transaction between two parties in a blockchain. */
public class Transaction implements Serializable  {
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
     * Custom serialization method for writing object state.
     *
     * @param out The ObjectOutputStream to write object state to.
     * @throws IOException If an I/O error occurs while writing the object.
     */
    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
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
        byte[] senderKey = (byte[]) in.readObject();
        byte[] receiverKey = (byte[]) in.readObject();
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.senderPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(senderKey));
        this.receiverPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(receiverKey));
        this.amount = in.readDouble();
        this.signature = (byte[]) in.readObject();
    }
    /*
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        byte[] senderKeyBytes = (byte[]) in.readObject();
        byte[] receiverKeyBytes = (byte[]) in.readObject();
        int signatureLength = in.readInt();
        signature = new byte[signatureLength];
        in.readFully(signature);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            senderPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(senderKeyBytes));
            receiverPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(receiverKeyBytes));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new IOException("Error reconstructing public keys", e);
        }
    }*/

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
