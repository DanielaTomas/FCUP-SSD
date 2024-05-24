package Main;

import Auctions.Auction;
import Auctions.CryptoUtils;
import Auctions.Wallet;
import BlockChain.Block;
import BlockChain.Blockchain;
import BlockChain.Miner;
import BlockChain.Transaction;
import Kademlia.*;

import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Class PeerMainMenu: Interface for a peer node */
public class PeerMainMenu implements Runnable {

    private Scanner scanner;
    private Kademlia kademlia;
    private Blockchain blockchain;
    private Wallet wallet;
    private Node myNode;

    /**
     * Constructs a PeerMainMenu object for the specified node.
     *
     * @param myNode The node associated with this menu.
     */
    public PeerMainMenu(Node myNode) {
        this.scanner = new Scanner(System.in);
        this.kademlia = Kademlia.getInstance();
        this.blockchain = Blockchain.getInstance();
        this.wallet = Wallet.getInstance();
        this.myNode = myNode;
    }

    /**
     * Generates the text menu options for the peer node.
     *
     * @return The formatted menu string.
     */
    public String menu(){
        return "----------------------------------" + '\n' +
        " 0 - Print KBucket" + '\n' +
        " 1 - Find Node" + '\n' +
        " 2 - Store" + '\n' +
        " 3 - Find Value" + '\n' +
        " 4 - Ping" + '\n' +
        " 5 - Mine Block" + '\n' +
        " 6 - Create Auction" + '\n' +
        " 7 - Place Bid" + '\n' +
        " 8 - Subscribe Auction" + '\n' +
        " 99 - Exit" + '\n' +
        "----------------------------------";
    }

    /** Starts the Peer Main Menu. */
    @Override
    public void run() {
        String input;
        System.out.println(menu());
        while (true) {

            input = scanner.nextLine();

            switch (input){
                case "menu":
                    System.out.println(menu());
                    break;
                case "0": //Routing Table Info
                    for (NodeInfo nodeInfo : myNode.getRoutingTable()){
                        System.out.println(nodeInfo);
                    }
                    break;
                case "1": //FIND_NODE RPC
                    System.out.println("Node ID: ");
                    input = scanner.nextLine();
                    kademlia.findNode(myNode.getNodeInfo(), input, myNode.getRoutingTable());
                    break;
                case "2": //STORE RPC
                    System.out.println("Key: ");
                    input = scanner.nextLine();
                    Block block1 = this.createBlock();
                    blockchain.addBlock(block1);
                    kademlia.store(myNode, input, new ValueWrapper(block1));
                    break;
                case "3"://FIND_VALUE RPC
                    System.out.println("Key: ");
                    input = scanner.nextLine();
                    kademlia.findValue(myNode, input);
                    break;
                case "4": //PING RPC
                    System.out.println("Node ID: ");
                    input = scanner.nextLine();
                    kademlia.ping(myNode.getNodeInfo(), input, myNode.getRoutingTable());
                    break;
                case "5": // Mine block
                    System.out.println("Mining block...");
                    Block block2 = this.createBlock();
                    kademlia.store(myNode, block2.getHash(), new ValueWrapper(block2));
                    kademlia.notifyNewBlockHash(myNode.getNodeInfo(), myNode.getRoutingTable(), block2.getHash());
                    break;
                case "6": // Create Auction
                    System.out.println("Item: ");
                    input = scanner.nextLine();
                    System.out.println("Starting Price: ");
                    double startingPrice = Double.parseDouble(scanner.nextLine());
                    System.out.println("End time (yyyy-MM-dd HH:mm:ss): ");
                    String endTime = scanner.nextLine();
                    Auction newAuction = new Auction(wallet.getPublicKey(), input, startingPrice, endTime);
                    newAuction.addSubscriber(myNode.getNodeInfo().getNodeId());
                    kademlia.store(myNode, newAuction.getId(), new ValueWrapper(newAuction));
                    kademlia.broadcastNewAuction(myNode.getNodeInfo(),myNode.getRoutingTable(),newAuction.getId());
                    break;
                case "7": // Place Bid
                    System.out.println("Auction ID: ");
                    String auctionId = scanner.nextLine();
                    Auction auction = (Auction) kademlia.findValue(myNode, auctionId); //TODO verify array list
                    if(auction != null) {
                        System.out.println("Bid amount: ");
                        double bidAmount = Double.parseDouble(scanner.nextLine());

                        PublicKey myPublicKey = wallet.getPublicKey();
                        PrivateKey myPrivateKey = wallet.getPrivateKey();
                        byte[] signature = CryptoUtils.sign(myPrivateKey, (myPublicKey.toString() + bidAmount).getBytes());

                        if(auction.placeBid(myPublicKey, bidAmount, signature)) {
                            Transaction transaction = new Transaction(auction.getSellerPublicKey(), bidAmount);
                            transaction.signTransaction(myPrivateKey);
                            if(this.blockchain.addTransaction(transaction)) {
                                kademlia.notifyAuctionUpdate(myNode.getNodeInfo(), myNode.getRoutingTable(), auction);
                            }
                        }
                    } else {
                        System.out.println("Auction not found.");
                    }
                    break;
                case "8": // Subscribe Auction
                    System.out.println("Auction ID: ");
                    input = scanner.nextLine();
                    Auction auctionToSubscribe = (Auction) kademlia.findValue(myNode, input); //TODO verify array list
                    if (auctionToSubscribe != null) {
                        auctionToSubscribe.addSubscriber(myNode.getNodeInfo().getNodeId());
                        kademlia.notifyNewSubscriber(myNode.getNodeInfo(),myNode.getRoutingTable(),auctionToSubscribe);
                    } else {
                        System.out.println("Auction not found.");
                    }
                    break;
                case "99": //Quit safely, otherwise I won't be blamed if weird behaviour occurs
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid input. Please try again.");
                    break;
            }
        }
    }

    /**
     * Creates a new block with transactions and mines it.
     *
     * @return The mined block.
     */
    public Block createBlock() {
        Miner miner = new Miner();

        List<Transaction> transactions = new ArrayList<>();
        KeyPair receiverKeyPair = Wallet.generateKeyPair();
        Transaction transaction = new Transaction(receiverKeyPair.getPublic(), 0);
        transaction.signTransaction(wallet.getPrivateKey());
        transactions.add(transaction);

        Block block = new Block(1,blockchain.getLastBlock().getHash(),transactions);

        miner.mine(block);

        return block;
    }
}
