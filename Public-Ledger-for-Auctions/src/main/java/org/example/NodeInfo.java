package org.example;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Class NodeInfo: Represents information about a node in the network */
public class NodeInfo {
    private static final Logger logger = Logger.getLogger(NodeInfo.class.getName());
    private String nodeId;
    private String ipAddr;
    private int port;

    /**
     * Constructs a node with the specified parameters.
     *
     * @param ipAddr The IP address of the node.
     * @param port   The port on which the node listens.
     */
    public NodeInfo(String ipAddr, int port) {
        this.nodeId = generateNodeId(ipAddr, port);
        this.ipAddr = ipAddr;
        this.port = port;
    }

    /**
     * Generates a node ID based on the IP address and port.
     *
     * @param ipAddress The IP address of the node.
     * @param port The port on which the node listens.
     * @return The generated node ID.
     */
    public static String generateNodeId(String ipAddress, int port) {
        String input = ipAddress + ":" + port;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(input.getBytes());
            String hexString = Utils.getHexString(hash);
            return hexString.substring(0, 40); // truncate to first 20 bytes (160 bits); 40 hexadecimal characters = 160 bits
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, "Error generating node ID", e);
            return null;
        }
    }

    /**
     * Gets the node ID.
     *
     * @return The node ID.
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * Gets the IP address of the node.
     *
     * @return The IP address.
     */
    public String getIpAddr() {
        return ipAddr;
    }

    /**
     * Gets the port on which the node listens.
     *
     * @return The port number.
     */
    public int getPort() {
        return port;
    }
}
