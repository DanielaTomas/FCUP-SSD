package org.example;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

public class Block {
    private int index;
    private String previousHash;
    private List<Transaction> transactions;
    private long timestamp;
    //private String data;
    private String hash;
    private int nonce;
    private String merkleRootHash;

    public Block(int index, String previousHash, List<Transaction> transactions, long timestamp) {
        this.index = index;
        this.previousHash = previousHash;
        this.transactions = transactions;
        this.timestamp = timestamp;
        //this.data = data;
        this.hash = calculateHash();
        this.nonce = 0;
    }

    public String calculateHash() {
        String input = previousHash + timestamp + nonce + transactions.toString();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getHash() {
        return this.hash;
    }

    public List<Transaction> getTransactions() {
        return this.transactions;
    }

    public String getPreviousHash() {
        return this.previousHash;
    }

    public int getIndex() {
        return this.index;
    }

    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0');
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block mined: " + hash);
    }
}
