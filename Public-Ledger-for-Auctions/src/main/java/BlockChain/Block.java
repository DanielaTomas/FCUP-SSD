package BlockChain;

import org.example.Utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
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
     */
    public Block(int index, String previousHash, List<Transaction> transactions) {
        this.index = index;
        this.previousHash = previousHash;
        this.transactions = transactions;
        this.timestamp = new Date().getTime();
        //this.data = data;
        calculateHash();
        this.nonce = 0;
    }

    /**
     * Calculates the hash of the block.
     *
     * @return The calculated hash value.
     */
    public void calculateHash() {
        String input = previousHash + timestamp + nonce + transactions.toString();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            this.hash = BlockChainUtils.getHexString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public void incrementNonce() {this.nonce++; }

    @Override
    public String toString() {
        return "index:"+this.index+"\n" +
                "Previous Hash:"+this.previousHash+"\n" +
                "Time:"+this.timestamp+"\n" +
                "Hash:"+this.hash+"\n" +
                "Nonce:"+this.nonce+"\n" +
                "" + this.transactions + "\n";
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final Block other = (Block) obj;

        return index == other.index
                && timestamp == other.timestamp
                && hash.equals(other.hash)
                && previousHash.equals(other.previousHash)
                && transactions.equals(other.transactions);
    }
}
