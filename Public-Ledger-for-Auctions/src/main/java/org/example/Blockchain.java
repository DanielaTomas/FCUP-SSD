package org.example;

import java.util.ArrayList;
import java.util.List;

public class Blockchain {
        private List<Block> chain;
        private List<Transaction> pendingTransactions;
        private int difficulty;

        public Blockchain() {
            this.chain = new ArrayList<>();
            this.pendingTransactions = new ArrayList<>();
            this.difficulty = 3;
            Block genesisBlock = createGenesisBlock();
            this.chain.add(genesisBlock);
        }

        private Block createGenesisBlock() {
            List<Transaction> transactions = new ArrayList<>();
            transactions.add(new Transaction("Genesis", "Initial", 0));
            return new Block(0, "0", transactions, System.currentTimeMillis());
        }

        public void addTransaction(Transaction transaction) {
            if(transaction == null) return;
            pendingTransactions.add(transaction);
        }

        public void minePendingTransactions(String minerAddress) {
            Block block = new Block(chain.size(), getLastBlock().getHash(), pendingTransactions, System.currentTimeMillis());
            block.mineBlock(difficulty);
            chain.add(block);
            pendingTransactions.clear();
            addTransaction(new Transaction("Reward", minerAddress, 10));
        }

        public Block getLastBlock() {
            return this.chain.get(chain.size() - 1);
        }

        public List<Block> getChain() {
            return this.chain;
        }

    public List<Transaction> getPendingTransactions() {
        return this.pendingTransactions;
    }
}
