package Auctions;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.logging.Logger;

class Auction {
    private static final Logger logger = Logger.getLogger(Auction.class.getName());

    private String id;
    private PublicKey sellerPublicKey;
    private String item;
    private double startingPrice;
    private long endTime;
    private double currentBid;
    private PublicKey currentBidder;
    private boolean isOpen;
    private byte[] signature;
    //private List<Buyer> bidders;


    public Auction(String id, PublicKey sellerPublicKey, String item, double startingPrice, long endTime) {
        this.id = id;
        this.sellerPublicKey = sellerPublicKey;
        this.item = item;
        this.startingPrice = startingPrice;
        this.endTime = endTime;
        this.currentBid = startingPrice;
        this.isOpen = true;
        this.signature = null;
    }

    public void placeBid(PublicKey bidderPublicKey, double bidAmount, byte[] signature) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        byte[] data = (bidderPublicKey.toString() + bidAmount).getBytes();

        if (!CryptoUtils.verifySignature(bidderPublicKey, signature, data)) {
            logger.warning("Invalid bid signature.");
            return;
        }
        else if(!isOpen) {
            logger.warning("Bid rejected. Auction is closed.");
            return;
        }
        else if (bidAmount <= currentBid) {
            logger.warning("Bid amount must be greater than current bid.");
            return;
        }

        currentBid = bidAmount;
        currentBidder = bidderPublicKey;
    }

    public void closeAuction() {
        isOpen = false;
        logger.info("Auction closed. Winner: " + currentBidder + ", Winning bid: " + currentBid);
    }
}
