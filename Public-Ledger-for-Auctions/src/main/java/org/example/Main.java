package org.example;

public class Main {
    public static void main(String[] args) { //TODO colocar mais do que um bootstrap node
        if (args.length < 1 || args.length > 2) {
            System.err.println("Error: Wrong number of arguments. Usage: java Main.java <port> [BootstrapNodeIps]");
            System.exit(1);
        }

        String ip = Utils.getAddress();
        int port;

        try {
            port = Integer.parseInt(args[0]);
            Node node = new Node(new NodeInfo(ip, port));

            if (args.length == 2) { // Node
                NodeInfo bootstrapNodeInfo = new NodeInfo(args[1], port);
                node.updateRoutingTable(bootstrapNodeInfo);
                new Kademlia().joinNetwork(node, bootstrapNodeInfo);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid integer input");
        }
    }
}