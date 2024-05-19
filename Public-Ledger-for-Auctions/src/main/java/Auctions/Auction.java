package Auctions;

import Kademlia.Kademlia;

import java.io.*;
import java.security.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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
    private String endTimeString;
    private Timer timer;
    private StringBuilder storedNodeId;

    /**
     * Constructor for creating a new auction.
     *
     * @param sellerPublicKey The public key of the seller.
     * @param item The item being auctioned.
     * @param startingPrice The starting price of the auction.
     * @param endTimeString The end time of the auction in the format "yyyy-MM-dd HH:mm:ss".
     */
    public Auction(PublicKey sellerPublicKey, String item, double startingPrice, String endTimeString) {
        this.auctionId = generateAuctionId(sellerPublicKey, item, startingPrice, endTimeString);
        this.sellerPublicKey = sellerPublicKey;
        this.item = item;
        this.startingPrice = startingPrice;
        this.currentBid = startingPrice;
        this.isOpen = true;
        this.subscribers = new ArrayList<>();
        this.endTimeString = endTimeString;
        this.storedNodeId = new StringBuilder();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime time = LocalDateTime.parse(endTimeString, formatter);
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/London"));
            this.endTime = ChronoUnit.MILLIS.between(now, time);
            if(this.endTime <= 0) {
                throw new IllegalArgumentException();
            }
            this.startAuctionTimer();
        } catch (DateTimeParseException e) {
            logger.warning("Invalid end time format. Please use yyyy-MM-dd HH:mm:ss");
        } catch (IllegalArgumentException e) {
            logger.warning("End time cannot be in the past");
        }
    }

    /**
     * Places a bid on the auction.
     *
     * @param bidderPublicKey The public key of the bidder.
     * @param bidAmount The amount of the bid.
     * @param signature The digital signature of the bid.
     * @return True if the bid is successfully placed, otherwise false.
     */
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

    /**
     * Starts the auction timer to close the auction at the end time.
     */
    private void startAuctionTimer() {
        logger.info("Starting Auction " + auctionId);

        this.timer = new Timer();
        long timeout = this.endTime;
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                closeAuction();
            }
        }, timeout);
    }

    /**
     * Cancels the auction timer.
     */
    private void cancelAuctionTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Checks if the auction is open.
     *
     * @return True if the auction is open, otherwise false.
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Closes the auction and logs the winner.
     */
    public void closeAuction() {
        isOpen = false;
        logger.info("Auction closed. Winner: " + currentBidder + ", Winning bid: " + currentBid);
        cancelAuctionTimer();
        //TODO notify subscribers
        //Kademlia kademlia = Kademlia.getInstance();
        //kademlia.notifyAuctionUpdate(myNode.getNodeInfo(),myNode.getRoutingTable(),this);
    }

    /**
     * Generates an auction ID based on the seller, item, starting price and end time.
     *
     * @param sellerPublicKey The public key of the seller.
     * @param item The item being auctioned.
     * @param startingPrice The starting price of the auction.
     * @param endTime The end time of the auction.
     * @return The generated auction ID.
     */
    public static String generateAuctionId(PublicKey sellerPublicKey, String item, double startingPrice, String endTime) {
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

    /**
     * Adds a subscriber to the auction.
     *
     * @param nodeId The ID of the node subscribing to the auction.
     */
    public void addSubscriber(String nodeId) {
        if(!isSubscriber(nodeId)) {
            subscribers.add(nodeId);
        } else {
            logger.info("You are already subscribed to this auction.");
        }
    }

    /**
     * Checks if a node is a subscriber to the auction.
     *
     * @param nodeId The ID of the node.
     * @return True if the node is a subscriber, otherwise false.
     */
    public boolean isSubscriber(String nodeId) {
        return subscribers.contains(nodeId);
    }

    /**
     * Converts the auction details to a string.
     *
     * @return The string representation of the auction.
     */
    @Override
    public String toString() {
        return "Auction ID: " + auctionId + "\n" +
                "Seller Public Key: " + sellerPublicKey + "\n" +
                "Item: " + item + "\n" +
                "Starting Price: " + startingPrice + "\n" +
                "End Time: " + endTimeString + "\n" +
                "Current Bid: " + currentBid + "\n" +
                "Current Bidder: " + currentBidder + "\n" +
                "Is Open: " + isOpen + "\n" +
                "Subscribers: " + subscribers + "\n" +
                "Stored in node " + storedNodeId + "\n";
    }

    /**
     * Custom serialization method.
     *
     * @param out The ObjectOutputStream to write the object to.
     * @throws IOException If an I/O error occurs.
     */
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
        out.writeObject(storedNodeId);
    }

    /**
     * Custom deserialization method.
     *
     * @param in The ObjectInputStream to read the object from.
     * @throws IOException If an I/O error occurs.
     * @throws ClassNotFoundException If the class of a serialized object cannot be found.
     * @throws SignatureException If an error occurs during signature verification.
     * @throws NoSuchAlgorithmException If the cryptographic algorithm is not available.
     * @throws InvalidKeyException If the key is invalid.
     */
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
        storedNodeId = (StringBuilder) in.readObject();
    }

    /**
     * Gets the auction ID.
     *
     * @return The auction ID.
     */
    public String getId() {
        return this.auctionId;
    }

    /**
     * Gets the current bid amount.
     *
     * @return The current bid amount.
     */
    public double getCurrentBid() {
        return currentBid;
    }

    /**
     * Gets the public key of the current highest bidder.
     *
     * @return The public key of the current highest bidder.
     */
    public PublicKey getCurrentBidder() {
        return currentBidder;
    }

    /**
     * Gets the public key of the seller.
     *
     * @return The public key of the seller.
     */
    public PublicKey getSellerPublicKey() {
        return sellerPublicKey;
    }

    /**
     * Gets the list of subscribers to the auction.
     *
     * @return The list of subscribers.
     */
    public List<String> getSubscribers() {
        return subscribers;
    }

    /**
     * Gets the ID of the node storing the auction.
     *
     * @return The ID of the node storing the auction.
     */
    public String getStoredNodeId() {
        return storedNodeId.toString();
    }

    /**
     * Sets the current bid amount.
     *
     * @param currentBid The current bid amount.
     */
    public void setCurrentBid(double currentBid) {
        this.currentBid = currentBid;
    }

    /**
     * Sets the current highest bidder.
     *
     * @param currentBidder The current highest bidder.
     */
    public void setCurrentBidder(PublicKey currentBidder) {
        this.currentBidder = currentBidder;
    }

    /**
     * Sets the ID of the node storing the auction.
     *
     * @param storedNodeId The ID of the node storing the auction.
     */
    public void setStoredNodeId(String storedNodeId) {
        this.storedNodeId = new StringBuilder(storedNodeId);
    }
}
