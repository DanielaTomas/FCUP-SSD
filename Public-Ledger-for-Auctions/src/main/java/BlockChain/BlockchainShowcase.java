package BlockChain;

import java.security.*;
import java.util.ArrayList;
import java.util.List;

public class BlockchainShowcase {


    public static void main(String[] args) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        Blockchain blockchain = Blockchain.getInstance();//empty constructor automatically creates the genesis block

        Miner miner = new Miner();

        Block genesisBlock = blockchain.getLastBlock();

        miner.mine(genesisBlock);

        List<Transaction> transactions = new ArrayList<>();
        KeyPair senderKeyPair = Transaction.generateKeyPair();
        KeyPair receiverKeyPair = Transaction.generateKeyPair();
        Transaction transaction = new Transaction(senderKeyPair.getPublic(), receiverKeyPair.getPublic(), 0);
        transaction.signTransaction(senderKeyPair.getPrivate());
        transactions.add(transaction);

        Block block1 = new Block(1,blockchain.getLastBlock().getHash(),transactions);

        miner.mine(block1);

        blockchain.addBlock(block1);

        transactions = new ArrayList<>();
        senderKeyPair = Transaction.generateKeyPair();
        receiverKeyPair = Transaction.generateKeyPair();
        transaction = new Transaction(senderKeyPair.getPublic(), receiverKeyPair.getPublic(), 0);
        transaction.signTransaction(senderKeyPair.getPrivate());
        transactions.add(transaction);

        Block block2 = new Block(2,blockchain.getLastBlock().getHash(),transactions);

        miner.mine(block2);

        blockchain.addBlock(block2);

        System.out.println("\n"+ "BLOCKCHAIN:\n"+ blockchain);
        System.out.println("Miner's reward: " + miner.getReward());

    }
}
