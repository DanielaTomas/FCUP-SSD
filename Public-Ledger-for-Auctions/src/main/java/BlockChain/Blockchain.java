package BlockChain;

import Auctions.Auction;
import Auctions.Wallet;

import java.security.*;
import java.util.ArrayList;
import java.util.List;

/** Class Blockchain: Represents a blockchain containing a chain of blocks and pending transactions. */
public class Blockchain {
    private static Blockchain instance; // Singleton design pattern, seems like a good idea for this class

    private List<Block> chain;
    private List<Transaction> pendingTransactions;
    private final int difficulty;
    private Wallet wallet;

    /**
     * Constructs a blockchain with default difficulty and creates the genesis block.
     */
    private Blockchain() {
        this.chain = new ArrayList<>();
        this.pendingTransactions = new ArrayList<>();
        this.difficulty = Constants.DIFFICULTY;
        this.wallet = Wallet.getInstance();
        Block genesisBlock = createGenesisBlock();
        this.chain.add(genesisBlock);
    }

    /**
     * Gets the singleton instance of the Blockchain class.
     *
     * @return The singleton instance of the Blockchain class.
     */
    public static Blockchain getInstance() {
        if(instance == null){
            instance = new Blockchain();
        }

        return instance;
    }

    /**
     * Creates the genesis block of the blockchain.
     *
     * @return The genesis block.
     */
    private Block createGenesisBlock() {
        List<Transaction> transactions = new ArrayList<>();
        KeyPair receiverKeyPair = Wallet.generateKeyPair();
        Transaction transaction = new Transaction(receiverKeyPair.getPublic(), 0);
        transaction.signTransaction(wallet.getPrivateKey());
        transactions.add(transaction);
        return new Block(0, Constants.GENESIS_PREV_HASH, transactions);
    }

    /**
     * Adds a transaction to the list of pending transactions.
     *
     * @param transaction The transaction to add.
     */
    public void addTransaction(Transaction transaction) {
        if(transaction == null || !transaction.verifySignature()) return;
        pendingTransactions.add(transaction);
    }

    /**
     * Retrieves the last block in the blockchain.
     *
     * @return The last block in the blockchain.
     */
    public Block getLastBlock() {
        return this.chain.get(chain.size() - 1);
    }

    /**
     * Retrieves the chain of blocks in the blockchain.
     *
     * @return The list of blocks forming the blockchain.
     */
    public List<Block> getChain() {
        return this.chain;
    }

    /**
     * Adds the given block to the chain
     *
     */
    public void addBlock(Block block) {
        this.chain.add(block);
    }

    /**
     * Retrieves the list of pending transactions.
     *
     * @return The list of pending transactions.
     */
    public List<Transaction> getPendingTransactions() {
        return this.pendingTransactions;
    }

    @Override
    public String toString() {
        StringBuilder blockChain = new StringBuilder();
        for(Block block : this.chain)
            blockChain.append(block.toString()).append("\n");
        return blockChain.toString();
    }
}
