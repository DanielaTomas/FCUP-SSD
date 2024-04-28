package Main;

import Kademlia.*;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Class Peer: Contains the main class representing a peer in the Kademlia network. */
public class Peer {

    private static final Logger logger = Logger.getLogger(Peer.class.getName());
    String ip;
    int port;

    /**
     * Constructor for the Peer class.
     *
     * @param ip The IP address of the peer.
     * @param port The port number of the peer.
     */
    Peer(String ip, int port){
       this.ip = ip;
       this.port = port;
    }

    public static void main(String[] args) { //TODO colocar mais do que um bootstrap node
        if (args.length < 1 || args.length > 2) {
            System.err.println("Error: Wrong number of arguments. Usage: java Main.java <port> [BootstrapNodeIps]");
            System.exit(1);
        }

        Peer myself = new Peer(Utils.getAddress(), Integer.parseInt(args[0]));
        System.out.println(myself);

        try {

            //System.setProperty("filename", port + ".log");
            Node myNode = new Node(new NodeInfo(myself.ip, myself.port));
            Kademlia kademlia = Kademlia.getInstance();

            if (args.length == 2) { // Node
                String bootstrapIp = args[1];
                NodeInfo bootstrapNodeInfo = new NodeInfo(bootstrapIp, myself.port);
                myNode.updateRoutingTable(bootstrapNodeInfo);
                kademlia.joinNetwork(myNode, bootstrapNodeInfo);
            }

            try {
                new Thread(new Server(myself.port, myNode)).start();
                logger.log(Level.FINE,"Kademlia server running!");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error starting the server", e);
            }

            try {
                new Thread(new PeerMainMenu(myNode)).start();
                logger.log(Level.FINE,"Kademlia client running!");
            }catch (Exception e){
                logger.log(Level.SEVERE, "Error starting the client", e);
            }

        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "Invalid port number", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Returns a string representation of the Peer object.
     *
     * @return A string representation of the object.
     */
    @Override
    public String toString() {
        return  "----------------------------------" + '\n' +
                "Peer Info " + '\n' +
                "ip = " + ip + '\n' +
                "port = " + port + '\n' +
                "----------------------------------";
    }
}
