package Kademlia;

import java.util.*;

/** Class Node: Represents a node in a peer-to-peer network. */
public class Node {
    private NodeInfo nodeInfo;
    private List<NodeInfo> routingTable;
    private Map<String,Object> storage;


    /**
     * Constructs a node with the specified parameters.
     *
     * @param nodeInfo id, ip and port of the node.
     */
    public Node(NodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
        this.routingTable = new ArrayList<>();
        this.storage = new HashMap<String, Object>();
    }

    /**
     * Find a NodeInfo in the routing table by ID.
     *
     * @param nodeId The ID of the NodeInfo to find.
     * @return The NodeInfo with the given ID, or null if not found.
     */
    public NodeInfo findNodeInfoById(String nodeId) {
        for (NodeInfo nodeInfo : routingTable) {
            if (nodeInfo.getNodeId().equals(nodeId)) {
                return nodeInfo;
            }
        }
        return null;
    }

    /**
     * Updates the routing table with information about another node.
     *
     * @param nodeInfo Information about the node to be added to the routing table.
     */
    public void updateRoutingTable(NodeInfo nodeInfo) {
        routingTable.add(nodeInfo);
    }

    /**
     * Stores a key-value pair in the storage.
     *
     * @param key   The key to store.
     * @param value The value corresponding to the key.
     */
    public void storeKeyValue(String key, Object value) {
        storage.put(key,value);
    }

    /**
     * Finds the value associated with the given key in the storage.
     *
     * @param key The key to search for.
     * @return The value associated with the given key, or null if the key is not found.
     */
    public Object findValueByKey(String key) {
        return storage.get(key);
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
    public List<NodeInfo> getRoutingTable() {
        return routingTable;
    }
}
