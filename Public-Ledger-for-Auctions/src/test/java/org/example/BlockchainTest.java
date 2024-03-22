package org.example;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockchainTest {
    private Blockchain blockchain;
    private KeyPair AliceKeyPair;
    private KeyPair BobKeyPair;
    private KeyPair CharlieKeyPair;
    private Transaction transaction1;
    private Transaction transaction2;

    @BeforeEach
    public void setup() {
        this.blockchain = new Blockchain();
        this.AliceKeyPair = Transaction.generateKeyPair();
        this.BobKeyPair = Transaction.generateKeyPair();
        this.CharlieKeyPair = Transaction.generateKeyPair();

        this.transaction1 = new Transaction(AliceKeyPair.getPublic(), BobKeyPair.getPublic(), 5);
        blockchain.addTransaction(transaction1);
        this.transaction2 = new Transaction(BobKeyPair.getPublic(), CharlieKeyPair.getPublic(), 10);
        blockchain.addTransaction(transaction2);
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
    public void validSignature() {
        transaction1.signTransaction(AliceKeyPair.getPrivate());
        assertTrue(transaction1.verifySignature());
    }

    @Test
    public void invalidSignature() {
        assertFalse(transaction1.verifySignature());
    }

    @Test
    public void miningPendingTransactions() {
        blockchain.minePendingTransactions(Transaction.generateKeyPair().getPublic());
        Assertions.assertEquals(2, blockchain.getChain().size());
        Assertions.assertEquals(1, blockchain.getPendingTransactions().size());
    }

    @Test
    public void debugPrint() {


    }

}