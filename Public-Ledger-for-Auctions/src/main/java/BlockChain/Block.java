package BlockChain;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;

/** Class Block: Represents a block in a blockchain. */
public class Block implements Serializable {
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
     * Writes the object's state to a stream. Called by the serialization mechanism when serializing an object.
     *
     * @param out The ObjectOutputStream to write the object to.
     * @throws IOException If an I/O error occurs while writing the object.
     */
    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(index);
        out.writeObject(previousHash);
        out.writeObject(transactions);
        out.writeLong(timestamp);
        out.writeObject(hash);
        out.writeInt(nonce);
    }

    /**
     * Reads the object's state from a stream. Called by the serialization mechanism when deserializing an object.
     *
     * @param in The ObjectInputStream to read the object from.
     * @throws IOException            If an I/O error occurs while reading the object.
     * @throws ClassNotFoundException If the class of the serialized object cannot be found.
     */
    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        index = in.readInt();
        previousHash = (String) in.readObject();
        transactions = (List<Transaction>) in.readObject();
        timestamp = in.readLong();
        hash = (String) in.readObject();
        nonce = in.readInt();
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

    /**
     * Increments the nonce value of the block by one.
     */
    public void incrementNonce() {this.nonce++; }

    /**
     * Returns a string representation of the Block object.
     *
     * @return A string representation of the object.
     */
    @Override
    public String toString() {
        return "index:"+this.index+"\n" +
                "Previous Hash:"+this.previousHash+"\n" +
                "Time:"+BlockChainUtils.convertTime(this.timestamp)+"\n" +
                "Hash:"+this.hash+"\n" +
                "Nonce:"+this.nonce+"\n" +
                "Transactions:" + this.transactions + "\n";
    }

    /**
     * Checks if this Block object is equal to another object.
     *
     * @param obj The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
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
