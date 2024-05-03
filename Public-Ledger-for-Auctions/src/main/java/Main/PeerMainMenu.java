package Main;

import BlockChain.Block;
import BlockChain.Blockchain;
import BlockChain.Miner;
import BlockChain.Transaction;
import Kademlia.*;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PeerMainMenu implements Runnable {

    private Scanner scanner;
    private Kademlia kademlia;
    private Blockchain blockchain;
    private Node myNode;

    public PeerMainMenu(Node myNode){
        this.scanner = new Scanner(System.in);
        this.kademlia = Kademlia.getInstance();
        this.blockchain = Blockchain.getInstance();
        this.myNode = myNode;
    }

    public String menu(){
        return "----------------------------------" + '\n' +
        " 0 - Print KBucket" + '\n' +
        " 1 - Find Node" + '\n' +
        " 2 - Store" + '\n' +
        " 3 - Find Value" + '\n' +
        " 4 - Ping" + '\n' +
        " 99 - Exit" + '\n' +
        "----------------------------------";
    }

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

                    Block block = this.createBlock();

                    blockchain.addBlock(block);
                    kademlia.store(myNode, input, block);
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
                case "99": //Quit safely, otherwise i won't be blamed if weird behaviour occurs
                    System.exit(0);
                default:
                    System.out.println("Invalid input. Please try again.");
                    break;
            }

        }
    }

    public Block createBlock(){
        Miner miner = new Miner();

        Block genesisBlock = blockchain.getLastBlock();

        miner.mine(genesisBlock,blockchain);

        List<Transaction> transactions = new ArrayList<>();
        KeyPair senderKeyPair = Transaction.generateKeyPair();
        KeyPair receiverKeyPair = Transaction.generateKeyPair();
        Transaction transaction = new Transaction(senderKeyPair.getPublic(), receiverKeyPair.getPublic(), 0);
        transaction.signTransaction(senderKeyPair.getPrivate());
        transactions.add(transaction);

        Block block = new Block(1,blockchain.getLastBlock().getHash(),transactions);

        miner.mine(block, blockchain);

        return block;
    }
}
