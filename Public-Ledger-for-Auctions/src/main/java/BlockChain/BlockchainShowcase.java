package BlockChain;

import Auctions.Wallet;

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
        KeyPair receiverKeyPair = Wallet.generateKeyPair();
        Transaction transaction = new Transaction(receiverKeyPair.getPublic(), 0);
        transaction.signTransaction(Wallet.getInstance().getPrivateKey());
        transactions.add(transaction);

        Block block1 = new Block(1,blockchain.getLastBlock().getHash(),transactions);

        miner.mine(block1);

        blockchain.addBlock(block1);

        transactions = new ArrayList<>();
        receiverKeyPair = Wallet.generateKeyPair();
        transaction = new Transaction(receiverKeyPair.getPublic(), 0);
        transaction.signTransaction(Wallet.getInstance().getPrivateKey());
        transactions.add(transaction);

        Block block2 = new Block(2,blockchain.getLastBlock().getHash(),transactions);

        miner.mine(block2);

        blockchain.addBlock(block2);

        System.out.println("\n"+ "BLOCKCHAIN:\n"+ blockchain);
        System.out.println("Miner's reward: " + miner.getReward());

    }
}
