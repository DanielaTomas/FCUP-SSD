package org.example;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BlockchainTest {
    private Blockchain blockchain;

    @BeforeEach
    public void setup() {
        this.blockchain = new Blockchain();
        blockchain.addTransaction(new Transaction("Alice", "Bob", 5));
        blockchain.addTransaction(new Transaction("Bob", "Charlie", 10));
    }

    @Test
    public void genesisBlockCreation() {
        Block genesisBlock = blockchain.getLastBlock();
        Assertions.assertEquals(0, genesisBlock.getIndex());
        Assertions.assertEquals("0", genesisBlock.getPreviousHash());
        Assertions.assertEquals(1, blockchain.getChain().size());
    }

    @Test
    public void addingTransactions() {
        Assertions.assertEquals(2, blockchain.getPendingTransactions().size());
    }

    @Test
    public void miningPendingTransactions() {
        blockchain.minePendingTransactions("Miner1");
        Assertions.assertEquals(2, blockchain.getChain().size());
        Assertions.assertEquals(1, blockchain.getPendingTransactions().size());
    }

    @Test
    public void debugPrint() {
        blockchain.minePendingTransactions("Miner1");
        System.out.println("Blockchain:");
        for (Block block : blockchain.getChain()) {
            System.out.println("\tBlock " + block.getIndex() + "   " + block.getHash());
            for (Transaction transaction : block.getTransactions()) {
                System.out.println("\t\t" + transaction.getSender() + " sent " + transaction.getAmount() + " to " + transaction.getReceiver());
            }
        }
    }

}