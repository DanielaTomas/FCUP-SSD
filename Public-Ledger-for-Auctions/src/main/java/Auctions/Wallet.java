package Auctions;

import java.security.*;

public class Wallet {

    private final KeyPair keyPair;
    private static Wallet instance;

    private Wallet() {
        keyPair = generateKeyPair();
    }

    public static Wallet getInstance(){
        if(instance == null){
            instance = new Wallet();
        }
        return instance;
    }

    public byte[] sign(byte[] data) {
        return CryptoUtils.sign(getPrivateKey(), data);
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

    public PublicKey getPublicKey() {
        return this.keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return this.keyPair.getPrivate();
    }

    public KeyPair getKeyPair() {
        return this.keyPair;
    }
}
