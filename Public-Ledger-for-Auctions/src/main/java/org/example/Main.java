package org.example;

import java.util.Objects;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) { //TODO colocar mais do que um bootstrap node

        if (args.length < 1 || args.length > 2) {
            System.err.println("Error: Wrong number of arguments. Usage: java Main.java <port> [BootstrapNodeIps]");
            System.exit(1);
        }

        String ip = Utils.getAddress();
        int port;

        try {
            port = Integer.parseInt(args[0]);
            //System.setProperty("filename", port + ".log");
            Node node = new Node(new NodeInfo(ip, port));
            Kademlia kademlia = new Kademlia();

            if (args.length == 2) { // Node
                NodeInfo bootstrapNodeInfo = new NodeInfo(args[1], port);
                node.updateRoutingTable(bootstrapNodeInfo);
                kademlia.joinNetwork(node, bootstrapNodeInfo);
                Scanner in = new Scanner(System.in);
                System.out.println("Store? 'y' or 'n'");
                String n = in.nextLine();
                if(n.equals("y")) {
                    kademlia.store(node, "fd643adf39fe4118213594b1922713f565f579de", "gaaaaaas");
                }
            }

            try {
                new Server(port, node).start();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error starting the server", e);
            }

        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "Invalid port number", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}