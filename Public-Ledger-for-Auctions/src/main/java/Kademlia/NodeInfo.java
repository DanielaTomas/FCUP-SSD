package Kademlia;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Class NodeInfo: Represents information about a node in the network */
public class NodeInfo implements Serializable, Comparable<NodeInfo> {
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
        String input = ipAddress + ":" + port + ":" + Math.random();
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
     * Returns a string representation of the NodeInfo object.
     *
     * @return A string representation of the object.
     */
    @Override
    public String toString() {
        return "NodeInfo{" +
                "nodeId='" + nodeId + '\'' +
                ", ipAddr='" + ipAddr + '\'' +
                ", port=" + port +
                '}';
    }

    /**
     * Compares this NodeInfo object with another NodeInfo object based on nodeId.
     *
     * @param o The NodeInfo object to compare with.
     * @return An integer value indicating the comparison result.
     */
    @Override
    public int compareTo(NodeInfo o ) {
        return this.nodeId.compareTo(o.getNodeId());
    }

    /**
     * Checks if this NodeInfo object is equal to another object.
     *
     * @param obj The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        NodeInfo other = (NodeInfo) obj;
        return Objects.equals(nodeId, other.nodeId);
    }

    /**
     * Generates a hash code for this NodeInfo object.
     *
     * @return The hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }

    /**
     * Writes the object's state to a stream. Called by the serialization mechanism when serializing an object.
     *
     * @param out The ObjectOutputStream to write the object to.
     * @throws IOException If an I/O error occurs while writing the object.
     */
    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(nodeId);
        out.writeObject(ipAddr);
        out.writeInt(port);
    }

    /**
     * Reads the object's state from a stream. Called by the serialization mechanism when deserializing an object.
     *
     * @param in The ObjectInputStream to read the object from.
     * @throws IOException            If an I/O error occurs while reading the object.
     * @throws ClassNotFoundException If the class of the serialized object cannot be found.
     */
    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        nodeId = (String) in.readObject();
        ipAddr = (String) in.readObject();
        port = in.readInt();
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
