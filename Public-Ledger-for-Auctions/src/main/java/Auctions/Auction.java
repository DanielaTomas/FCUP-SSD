package Auctions;

import java.security.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Auction {
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
    private AuctionService auctionService;
    //private List<PublicKey> bidders;

    public Auction(PublicKey sellerPublicKey, String item, double startingPrice, long endTime) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        this.auctionId = generateAuctionId(sellerPublicKey, item, startingPrice, endTime);
        this.sellerPublicKey = sellerPublicKey;
        this.item = item;
        this.startingPrice = startingPrice;
        this.endTime = endTime;
        this.currentBid = startingPrice;
        this.isOpen = true;
        this.signature = null;
        this.auctionService = new AuctionService(this);
    }

    public void placeBid(PublicKey bidderPublicKey, double bidAmount, byte[] signature) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
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

        this.auctionService.addTransactionToBlockchain(bidAmount);

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
