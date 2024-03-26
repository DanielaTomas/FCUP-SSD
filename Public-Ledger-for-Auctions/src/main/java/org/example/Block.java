package org.example;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/** Class Block: Represents a block in a blockchain. */
public class Block {
    private int index;
    private String previousHash;
    private List<Transaction> transactions;
    private long timestamp;
    //private String data;
    private String hash;
    private int nonce;
    //private String merkleRootHash;

    /**
     * Constructs a block with the given parameters and calculates its hash.
     *
     * @param index         The index of the block in the blockchain.
     * @param previousHash  The hash of the previous block in the blockchain.
     * @param transactions  The list of transactions contained in the block.
     * @param timestamp     The timestamp indicating when the block was created.
     */
    public Block(int index, String previousHash, List<Transaction> transactions, long timestamp) {
        this.index = index;
        this.previousHash = previousHash;
        this.transactions = transactions;
        this.timestamp = timestamp;
        //this.data = data;
        this.hash = calculateHash();
        this.nonce = 0;
    }

    /**
     * Calculates the hash of the block.
     *
     * @return The calculated hash value.
     */
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

    /**
     * Mines the block with the given difficulty.
     *
     * @param difficulty The difficulty level of mining.
     */
    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0');
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block mined: " + hash);
    }

    /**
     * Retrieves the hash of the block.
     *
     * @return The hash of the block.
     */
    public String getHash() {
        return this.hash;
    }

    /**
     * Retrieves the list of transactions contained in the block.
     *
     * @return The list of transactions.
     */
    public List<Transaction> getTransactions() {
        return this.transactions;
    }

    /**
     * Retrieves the hash of the previous block in the blockchain.
     *
     * @return The hash of the previous block.
     */
    public String getPreviousHash() {
        return this.previousHash;
    }

    /**
     * Retrieves the index of the block in the blockchain.
     *
     * @return The index of the block.
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * Retrieves the timestamp indicating when the block was created.
     *
     * @return The timestamp of the block.
     */
    public long getTimestamp() {
        return this.timestamp;
    }
}
