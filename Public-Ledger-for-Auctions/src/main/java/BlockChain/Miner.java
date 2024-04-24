package BlockChain;

public class Miner {
    private double reward;


    /**
     * Mines the block with the given difficulty.
     *
     * @param b The most recent block in the chain.
     */
    public boolean PoW(Block b) {
        String target = new String(new char[Constants.DIFICULTY]).replace('\0', '0');
        String hash = b.getHash();

        return !hash.substring(0, Constants.DIFICULTY).equals(target);
    }

    public Block mine(Block b){
        while (PoW(b)){
            b.incrementNonce();
            b.calculateHash();
        }
        return b;
    }


}
