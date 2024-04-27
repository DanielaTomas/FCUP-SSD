package Main;

import Kademlia.*;

import java.util.Scanner;

public class PeerMainMenu implements Runnable {

    private Scanner scanner;
    private Kademlia kademlia;
    private Node node;

    public PeerMainMenu(Node node){
        this.scanner = new Scanner(System.in);
        this.kademlia = Kademlia.getInstance();
        this.node = node;
    }

    public String menu(){
        return "----------------------------------" + '\n' +
        " 1 - Find Node" + '\n' +
        " 2 - Store" + '\n' +
        " 3 - Find Value" + '\n' +
        " 4 - Ping" + '\n' +
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
                case "1":
                    System.out.println("IP: ");
                    input = scanner.nextLine();
                    System.out.println("Port: ");
                    int portFindNode = Integer.parseInt(scanner.nextLine());
                    kademlia.findNode(node.getNodeInfo(), new NodeInfo(input,portFindNode));
                    break;
                case "2":
                    System.out.println("Key: ");
                    input = scanner.nextLine();
                    System.out.println("Value: ");
                    String value = scanner.nextLine();
                    kademlia.store(node, input, value);
                    break;
                case "3":
                    System.out.println("Key: ");
                    input = scanner.nextLine();
                    kademlia.findValue(node, input);
                    break;
                case "4":
                    System.out.println("IP: ");
                    input = scanner.nextLine();
                    System.out.println("Port: ");
                    int portPing = Integer.parseInt(scanner.nextLine());
                    kademlia.ping(node.getNodeInfo(), new NodeInfo(input,portPing));
                    break;
                default:
                    System.out.println("Invalid input. Please try again.");
                    break;
            }

        }
    }
}
