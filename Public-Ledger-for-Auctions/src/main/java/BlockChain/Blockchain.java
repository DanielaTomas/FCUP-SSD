package BlockChain;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

/** Class Blockchain: Represents a blockchain containing a chain of blocks and pending transactions. */
public class Blockchain {
    private List<Block> chain;
    private List<Transaction> pendingTransactions;
    private final int difficulty;

    /**
     * Constructs a blockchain with default difficulty and creates the genesis block.
     */
    public Blockchain() {
        this.chain = new ArrayList<>();
        this.pendingTransactions = new ArrayList<>();
        this.difficulty = 3;
        Block genesisBlock = createGenesisBlock();
        this.chain.add(genesisBlock);
    }

    /**
     * Creates the genesis block of the blockchain.
     *
     * @return The genesis block.
     */
    private Block createGenesisBlock() {
        List<Transaction> transactions = new ArrayList<>();
        KeyPair senderKeyPair = Transaction.generateKeyPair();
        KeyPair receiverKeyPair = Transaction.generateKeyPair();
        Transaction transaction = new Transaction(senderKeyPair.getPublic(), receiverKeyPair.getPublic(), 0);
        transaction.signTransaction(senderKeyPair.getPrivate());
        transactions.add(transaction);
        return new Block(0, "0", transactions, System.currentTimeMillis());
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
     * Mines pending transactions and adds a new block to the blockchain.
     *
     * @param minerPublicKey The public key of the miner receiving the reward.
     */
    public void minePendingTransactions(PublicKey minerPublicKey) {
        Block block = new Block(chain.size(), getLastBlock().getHash(), pendingTransactions, System.currentTimeMillis());
        block.mineBlock(difficulty);
        chain.add(block);
        pendingTransactions.clear();
        KeyPair rewardKeyPair = Transaction.generateKeyPair();
        Transaction transaction = new Transaction(rewardKeyPair.getPublic(), minerPublicKey, 10);
        transaction.signTransaction(rewardKeyPair.getPrivate());
        addTransaction(transaction);
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
     * Retrieves the list of pending transactions.
     *
     * @return The list of pending transactions.
     */
    public List<Transaction> getPendingTransactions() {
        return this.pendingTransactions;
    }

    @Override
    public String toString() {
        String blockChain = "";
        for(Block block : this.chain)
            blockChain+=block.toString()+"\n";
        return blockChain;
    }
}
