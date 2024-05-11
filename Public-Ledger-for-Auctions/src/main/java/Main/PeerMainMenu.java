package Main;

import BlockChain.Block;
import BlockChain.Blockchain;
import BlockChain.Miner;
import BlockChain.Transaction;
import Kademlia.*;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Class PeerMainMenu: Interface for a peer node */
public class PeerMainMenu implements Runnable {

    private Scanner scanner;
    private Kademlia kademlia;
    private Blockchain blockchain;
    private Node myNode;

    /**
     * Constructs a PeerMainMenu object for the specified node.
     *
     * @param myNode The node associated with this menu.
     */
    public PeerMainMenu(Node myNode) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        this.scanner = new Scanner(System.in);
        this.kademlia = Kademlia.getInstance();
        this.blockchain = Blockchain.getInstance();
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

                    Block block1 = null;
                    try {
                        block1 = this.createBlock();
                    } catch (SignatureException | NoSuchAlgorithmException | InvalidKeyException e) {
                        throw new RuntimeException(e);
                    }

                    blockchain.addBlock(block1);
                    kademlia.store(myNode, input, block1);
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
                    Block block2 = null;
                    try {
                        block2 = this.createBlock();
                    } catch (SignatureException | NoSuchAlgorithmException | InvalidKeyException e) {
                        throw new RuntimeException(e);
                    }
                    kademlia.store(myNode, block2.getHash(), block2);
                    kademlia.notifyNewBlockHash(myNode, block2.getHash());
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
    public Block createBlock() throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        Miner miner = new Miner();

        List<Transaction> transactions = new ArrayList<>();
        KeyPair senderKeyPair = Transaction.generateKeyPair();
        KeyPair receiverKeyPair = Transaction.generateKeyPair();
        Transaction transaction = new Transaction(senderKeyPair.getPublic(), receiverKeyPair.getPublic(), 0);
        transaction.signTransaction(senderKeyPair.getPrivate());
        transactions.add(transaction);

        Block block = new Block(1,blockchain.getLastBlock().getHash(),transactions);

        miner.mine(block);

        return block;
    }
}
