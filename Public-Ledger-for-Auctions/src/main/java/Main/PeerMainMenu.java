package Main;

import Kademlia.*;

import java.util.Scanner;

public class PeerMainMenu implements Runnable {

    private Scanner scanner;
    private Kademlia kademlia;
    private Node myNode;

    public PeerMainMenu(Node myNode){
        this.scanner = new Scanner(System.in);
        this.kademlia = Kademlia.getInstance();
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
                case "0":
                    myNode.getRoutingTable();
                    for ( NodeInfo nodeInfo : myNode.getRoutingTable()){
                        System.out.println(nodeInfo);
                    }
                    break;
                case "1":
                    System.out.println("Node ID: ");
                    input = scanner.nextLine();
                    kademlia.findNode(myNode.getNodeInfo(), null);// WHAT THE HEEEEEEEEEEEEEEELLLLLLLLLLLLLLL
                    break;
                case "2":
                    System.out.println("Key: ");
                    input = scanner.nextLine();
                    System.out.println("Value: ");
                    String value = scanner.nextLine();
                    kademlia.store(myNode, input, value);
                    break;
                case "3":
                    System.out.println("Key: ");
                    input = scanner.nextLine();
                    kademlia.findValue(myNode, input);
                    break;
                case "4":
                    System.out.println("Node ID: ");
                    input = scanner.nextLine();
                    kademlia.ping(myNode.getNodeInfo(), null); // WHAT THE HEEEEEEEEEEEEEEEEEEEELLLLLLLLLLLLLL
                    break;
                case "99":
                    System.exit(0);
                default:
                    System.out.println("Invalid input. Please try again.");
                    break;
            }

        }
    }
}
