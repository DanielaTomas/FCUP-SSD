package Auctions;

import java.io.*;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
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
    private List<String> subscribers;

    public Auction(PublicKey sellerPublicKey, String item, double startingPrice, long endTime) {
        this.auctionId = generateAuctionId(sellerPublicKey, item, startingPrice, endTime);
        this.sellerPublicKey = sellerPublicKey;
        this.item = item;
        this.startingPrice = startingPrice;
        this.endTime = endTime;
        this.currentBid = startingPrice;
        this.isOpen = true;
        this.subscribers = new ArrayList<>();
    }

    public boolean placeBid(PublicKey bidderPublicKey, double bidAmount, byte[] signature) {
        byte[] data = (bidderPublicKey.toString() + bidAmount).getBytes();

        if (!CryptoUtils.verifySignature(bidderPublicKey, data, signature)) {
            logger.warning("Invalid bid signature.");
            return false;
        }
        else if(!this.isOpen()) {
            logger.warning("Bid rejected. Auction is closed.");
            return false;
        }
        else if (bidAmount <= this.currentBid) {
            logger.warning("Bid amount must be greater than current bid.");
            return false;
        }

        this.currentBid = bidAmount;
        this.currentBidder = bidderPublicKey;

        return true;
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
        //TODO broadcast
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

    public void addSubscriber(String nodeId) {
        if(!isSubscriber(nodeId)) {
            subscribers.add(nodeId);
        } else {
            logger.info("You are already subscribed to this auction.");
        }
    }

    public boolean isSubscriber(String nodeId) {
        return subscribers.contains(nodeId);
    }


    @Override
    public String toString() {
        return "Auction ID: " + auctionId + "\n" +
                "Seller Public Key: " + sellerPublicKey + "\n" +
                "Item: " + item + "\n" +
                "Starting Price: " + startingPrice + "\n" +
                "End Time: " + endTime + "\n" +
                "Current Bid: " + currentBid + "\n" +
                "Current Bidder: " + currentBidder + "\n" +
                "Is Open: " + isOpen + "\n" +
                "Subscribers: " + subscribers + "\n";
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
        out.writeObject(subscribers);
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException, SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        auctionId = (String) in.readObject();
        sellerPublicKey = (PublicKey) in.readObject();
        item = (String) in.readObject();
        startingPrice = in.readDouble();
        endTime = in.readLong();
        currentBid = in.readDouble();
        currentBidder = (PublicKey) in.readObject();
        isOpen = in.readBoolean();
        subscribers = (List<String>) in.readObject();
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

    public List<String> getSubscribers() {
        return subscribers;
    }
}
