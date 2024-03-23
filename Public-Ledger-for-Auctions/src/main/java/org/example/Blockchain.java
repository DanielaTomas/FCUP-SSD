package org.example;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class Blockchain {
        private List<Block> chain;
        private List<Transaction> pendingTransactions;
        private final int difficulty;

        public Blockchain() {
            this.chain = new ArrayList<>();
            this.pendingTransactions = new ArrayList<>();
            this.difficulty = 3;
            Block genesisBlock = createGenesisBlock();
            this.chain.add(genesisBlock);
        }

        private Block createGenesisBlock() {
            List<Transaction> transactions = new ArrayList<>();
            KeyPair senderKeyPair = Transaction.generateKeyPair();
            KeyPair receiverKeyPair = Transaction.generateKeyPair();
            Transaction transaction = new Transaction(senderKeyPair.getPublic(), receiverKeyPair.getPublic(), 0);
            transaction.signTransaction(senderKeyPair.getPrivate());
            transactions.add(transaction);
            return new Block(0, "0", transactions, System.currentTimeMillis());
        }

        public void addTransaction(Transaction transaction) {
            if(transaction == null || !transaction.verifySignature()) return;
            pendingTransactions.add(transaction);
        }

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
