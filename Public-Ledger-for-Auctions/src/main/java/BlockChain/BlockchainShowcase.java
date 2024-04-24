package BlockChain;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;

public class BlockchainShowcase {


    public static void main(String[] args){
        Blockchain blockchain = new Blockchain();//empty constructor automatically creates the genesis block

        Miner miner = new Miner();

        Block genesisBlock = blockchain.getLastBlock();

        miner.mine(genesisBlock,blockchain);

        System.out.println("\n"+ "BLOCKCHAIN:\n"+ blockchain);
        System.out.println("Miner's reward: " + miner.getReward());

    }
}
