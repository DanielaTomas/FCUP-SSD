package org.example;

import java.util.*;

/** Class Node: Represents a node in a peer-to-peer network. */
public class Node {
    private NodeInfo nodeInfo;
    private Map<String, String> routingTable;
    private Set<String> storage;


    /**
     * Constructs a node with the specified parameters.
     *
     * @param nodeInfo id, ip and port of the node.
     */
    public Node(NodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
        this.routingTable = new HashMap<>();
        this.storage = new HashSet<>();
    }

    /**
     * Updates the routing table with information about another node.
     *
     * @param nodeInfo Information about the node to be added to the routing table.
     */
    public void updateRoutingTable(NodeInfo nodeInfo) {
        routingTable.put(nodeInfo.getNodeId(), nodeInfo.getIpAddr() + ":" + nodeInfo.getPort());
    }

    /**
     * Gets the information about this node.
     *
     * @return Information about this node.
     */
    public NodeInfo getNodeInfo() {
        return this.nodeInfo;
    }

    /**
     * Gets the routing table of this node.
     *
     * @return The routing table of this node.
     */
    public Map<String, String> getRoutingTable() {
        return routingTable;
    }
}
