package Auctions;

import BlockChain.Blockchain;
import BlockChain.Transaction;
import Kademlia.Kademlia;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public class AuctionService {
    //private List<Auction> auctions;
    private Auction auction;
    private Blockchain blockchain;
    private Kademlia kademlia;

    /**
     * Constructs an AuctionService.
     */
    public AuctionService(Auction auction) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        this.auction = auction;
        this.blockchain = Blockchain.getInstance();
        this.kademlia = Kademlia.getInstance();
    }

    public void addTransactionToBlockchain(double amount) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Transaction transaction = new Transaction(auction.getCurrentBidder(), auction.getSellerPublicKey(), amount);
        //signature = this.kademlia.signTransaction(currentBidder, transaction);
        this.blockchain.addTransaction(transaction);
    }


/*
    public Auction createAuction(PublicKey sellerPublicKey, String item, double startingPrice, long endTime) {
        Auction auction = new Auction(sellerPublicKey, item, startingPrice, endTime);
        auctions.add(auction);
        System.out.println("Broadcasting CreateAuction for: " + auction.getId());

        //TODO Broadcast new bid

        return auction;
    }
*/

}
