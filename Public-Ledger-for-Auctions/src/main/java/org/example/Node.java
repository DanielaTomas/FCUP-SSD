package org.example;

import java.util.HashMap;
import java.util.Map;

public class Node {
    private String nodeId;
    private Map<String, String> routingTable;

    public Node(String nodeId) {
        this.nodeId = nodeId;
        this.routingTable = new HashMap<>();
    }

    public void sendMessage(Node recipient, String message) {
        recipient.receiveMessage(message);
    }

    public void receiveMessage(String message) {
        System.out.println("Node " + nodeId + " received message: " + message);
    }

    public void updateRoutingTable(String nodeId, String ipAddress) {
        routingTable.put(nodeId, ipAddress);
    }
}
