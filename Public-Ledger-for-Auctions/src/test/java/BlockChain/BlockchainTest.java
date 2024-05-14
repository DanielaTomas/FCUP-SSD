package BlockChain;
import Auctions.Wallet;
import BlockChain.Block;
import BlockChain.Blockchain;
import BlockChain.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class BlockchainTest {
    private Blockchain blockchain;
    private KeyPair AliceKeyPair;
    private KeyPair BobKeyPair;
    private KeyPair CharlieKeyPair;
    private KeyPair MinerKeyPair;

    private Transaction transaction1;
    private Transaction transaction2;

    @BeforeEach
    public void setup() throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        this.blockchain = Blockchain.getInstance();
        this.AliceKeyPair = Wallet.generateKeyPair();
        this.BobKeyPair = Wallet.generateKeyPair();
        this.CharlieKeyPair = Wallet.generateKeyPair();
        this.MinerKeyPair = Wallet.generateKeyPair();
        this.transaction1 = new Transaction(BobKeyPair.getPublic(), 5);
        this.transaction2 = new Transaction(CharlieKeyPair.getPublic(), 10);
    }

    @Test
    public void genesisBlockCreation() {
        Block genesisBlock = blockchain.getLastBlock();
        Assertions.assertEquals(0, genesisBlock.getIndex());
        Assertions.assertEquals("0", genesisBlock.getPreviousHash());
        Assertions.assertEquals(1, blockchain.getChain().size());
    }

    @Test
    public void validSignature() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        this.sign_and_add_transactions();
        Assertions.assertTrue(transaction1.verifySignature());
        Assertions.assertTrue(transaction2.verifySignature());
    }

    @Test
    public void invalidSignature() {
        Assertions.assertFalse(transaction1.verifySignature());
    }

    @Test
    public void addingTransactions() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        this.sign_and_add_transactions();
        Assertions.assertEquals(2, blockchain.getPendingTransactions().size());
    }

    @Test
    public void miningPendingTransactions() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        this.sign_and_add_transactions();
        //blockchain.minePendingTransactions(MinerKeyPair.getPublic());
        Assertions.assertEquals(2, blockchain.getChain().size());
        Assertions.assertEquals(1, blockchain.getPendingTransactions().size());
    }

    @Test
    public void debugPrint() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        this.sign_and_add_transactions();
        //blockchain.minePendingTransactions(MinerKeyPair.getPublic());

        System.out.println("Blockchain:");
        for (Block block : blockchain.getChain()) {
            System.out.println("\tBlock Index: " + block.getIndex());
            System.out.println("\tBlock Hash: " + block.getHash());
            System.out.println("\tPrevious Hash: " + block.getPreviousHash());
            System.out.println("\tTransactions: " + block.getTransactions().toString());
            System.out.println("\tTimestamp: " + convertTime(block.getTimestamp()));
        }

        System.out.println("Pending Transactions:");
        for (Transaction pendingTransaction : blockchain.getPendingTransactions()) {
            System.out.println("\tSender: " + pendingTransaction.getSenderPublicKey());
            System.out.println("\tReceiver: " + pendingTransaction.getReceiverPublicKey());
            System.out.println("\tAmount: " + pendingTransaction.getAmount());
            System.out.println("\tSignature: " + Arrays.toString(pendingTransaction.getSignature()));
        }
    }

    public String convertTime(long time){
        Date date = new Date(time);
        Format format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return format.format(date);
    }

    public void sign_and_add_transactions() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        transaction1.signTransaction(AliceKeyPair.getPrivate());
        transaction2.signTransaction(BobKeyPair.getPrivate());
        blockchain.addTransaction(transaction1);
        blockchain.addTransaction(transaction2);
    }

}