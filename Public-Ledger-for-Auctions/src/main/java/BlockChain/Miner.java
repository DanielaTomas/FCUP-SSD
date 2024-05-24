package BlockChain;

/** Class Miner: Represents a miner in a blockchain. */
public class Miner {
    private double reward;

    /*
     * Mines pending transactions and adds a new block to the blockchain.
     *
     * @param minerPublicKey The public key of the miner receiving the reward.
     */
    /*
    public void minePendingTransactions(PublicKey minerPublicKey) {
        Block block = new Block(chain.size(), getLastBlock().getHash(), pendingTransactions);
        block.mineBlock(difficulty);
        chain.add(block);
        pendingTransactions.clear();
        KeyPair rewardKeyPair = Transaction.generateKeyPair();
        Transaction transaction = new Transaction(rewardKeyPair.getPublic(), minerPublicKey, 10);
        transaction.signTransaction(rewardKeyPair.getPrivate());
        addTransaction(transaction);
    }*/

    /**
     * Mines the block with the given difficulty.
     *
     * @param b The most recent block in the chain.
     */
    private boolean PoW(Block b) {
        String target = new String(new char[Constants.DIFFICULTY]).replace('\0', '0');
        String hash = b.getHash();

        return !hash.substring(0, Constants.DIFFICULTY).equals(target);
    }

    /**
     * Mines a block until the proof of work (PoW) meets the target difficulty.
     *
     * @param b The block to be mined.
     */
    public void mine(Block b) {
        while (PoW(b)){
            b.incrementNonce();
            b.calculateHash();
        }
        reward += Constants.MINER_REWARD;
    }

    /**
     * Retrieves the total reward accumulated by the miner.
     *
     * @return The total reward earned by the miner.
     */
    public double getReward(){
        return reward;
    }


}
