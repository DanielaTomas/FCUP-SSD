package Auctions;

import BlockChain.Blockchain;
import BlockChain.Transaction;

import java.io.*;
import java.security.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Auction implements Serializable {
    private static final Logger logger = Logger.getLogger(Auction.class.getName());

    private String auctionId;
    private PublicKey sellerPublicKey;
    private String item;
    private double startingPrice;
    private long endTime;
    private double currentBid;
    private PublicKey currentBidder;
    private boolean isOpen;
    private byte[] signature;
    private Blockchain blockchain;
    //private List<PublicKey> bidders;

    public Auction(PublicKey sellerPublicKey, String item, double startingPrice, long endTime) {
        this.auctionId = generateAuctionId(sellerPublicKey, item, startingPrice, endTime);
        this.sellerPublicKey = sellerPublicKey;
        this.item = item;
        this.startingPrice = startingPrice;
        this.endTime = endTime;
        this.currentBid = startingPrice;
        this.isOpen = true;
        this.signature = null;
        this.blockchain = Blockchain.getInstance();
    }

    public void placeBid(PublicKey bidderPublicKey, double bidAmount, byte[] signature) {
        byte[] data = (bidderPublicKey.toString() + bidAmount).getBytes();

        if (!CryptoUtils.verifySignature(bidderPublicKey, signature, data)) {
            logger.warning("Invalid bid signature.");
            return;
        }
        else if(!this.isOpen()) {
            logger.warning("Bid rejected. Auction is closed.");
            return;
        }
        else if (bidAmount <= this.currentBid) {
            logger.warning("Bid amount must be greater than current bid.");
            return;
        }

        this.currentBid = bidAmount;
        this.currentBidder = bidderPublicKey;

        Transaction transaction = new Transaction(sellerPublicKey, bidAmount);
        transaction.setSignature(signature);
        this.blockchain.addTransaction(transaction);

        //TODO Broadcast new bid
    }

    public boolean isOpen() {
        if(System.currentTimeMillis() < endTime) {
            this.closeAuction();
        }
        return isOpen;
    }

    public void closeAuction() {
        isOpen = false;
        logger.info("Auction closed. Winner: " + currentBidder + ", Winning bid: " + currentBid);
    }

    /**
     * Generates an auction ID based on the seller, item, starting price and end time.
     *
     * @param sellerPublicKey
     * @param item
     * @param startingPrice
     * @param endTime
     * @return The generated auction ID.
     */
    public static String generateAuctionId(PublicKey sellerPublicKey, String item, double startingPrice, long endTime) {
        String input = sellerPublicKey + ":" + item + ":" + startingPrice + ":" + endTime + ":" + Math.random();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(input.getBytes());
            String hexString = CryptoUtils.getHexString(hash);
            return hexString.substring(0, 40);
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, "Error generating auction ID", e);
            return null;
        }
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(auctionId);
        out.writeObject(sellerPublicKey);
        out.writeObject(item);
        out.writeDouble(startingPrice);
        out.writeLong(endTime);
        out.writeDouble(currentBid);
        out.writeObject(currentBidder);
        out.writeBoolean(isOpen);
        out.writeObject(signature);
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException, SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        auctionId = (String) ois.readObject();
        sellerPublicKey = (PublicKey) ois.readObject();
        item = (String) ois.readObject();
        startingPrice = ois.readDouble();
        endTime = ois.readLong();
        currentBid = ois.readDouble();
        currentBidder = (PublicKey) ois.readObject();
        isOpen = ois.readBoolean();
        signature = (byte[]) ois.readObject();
    }

    public String getId() {
        return this.auctionId;
    }

    public double getCurrentBid() {
        return currentBid;
    }

    public PublicKey getCurrentBidder() {
        return currentBidder;
    }

    public PublicKey getSellerPublicKey() {
        return sellerPublicKey;
    }
}
