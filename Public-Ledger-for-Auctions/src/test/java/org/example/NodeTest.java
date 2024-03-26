package org.example;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NodeTest {

    private Node node1;
    private Node node2;
    private Node node3;
    private Node bootstrapNode;


    @BeforeEach
    public void setup() {
        this.node1 = new Node("node1", "127.0.0.1", 8080);
        this.node2 = new Node("node2", "127.0.0.1", 8081);
        this.node3 = new Node("node3", "127.0.0.1", 8082);
        this.bootstrapNode = new Node("bootstrapNode", "127.0.0.1", 8083);
    }

    @Test
    public void ping_successful() {
    }

    @Test
    public void joinNetwork() {
        Node mockNode = Mockito.mock(Node.class);
        Mockito.when(mockNode.findNode(Mockito.any())).thenReturn(List.of(node1, node2));

        node1.joinNetwork(mockNode);

        System.out.println("Routing table of Node 1:");
        System.out.println(node1.getRoutingTable());
        assertEquals(2, node1.getRoutingTable().size());
    }

    @Test
    public void ping() {
        //node1.updateRoutingTable(node2);
        //node1.ping(node2);
    }

    @Test
    public void testFindNode() {
        /*
        List<Node> nodes = List.of(node1, node2);
        Node mockNode = Mockito.mock(Node.class);
        Mockito.when(mockNode.findNode(Mockito.any())).thenReturn(nodes);

        node1.joinNetwork(mockNode);

        List<Node> foundNodes = node1.findNode(bootstrapNode);

        System.out.println("Nodes found by Node 1:");
        for (Node foundNode : foundNodes) {
            System.out.println(foundNode.getNodeId() + " - " + foundNode.getIpAddr() + ":" + foundNode.getPort());
        }
        */
    }

    @Test
    public void debugPrint() {
        /*
        //Create nodes
        Node bootstrapNode = new Node("BootstrapNode", "127.0.0.1", 8080);
        Node node1 = new Node("Node1", "127.0.0.1", 8081);
        Node node2 = new Node("Node2", "127.0.0.1", 8082);
        Node node3 = new Node("Node3", "127.0.0.1", 8083);

        // Set up network
        node1.joinNetwork(bootstrapNode);
        node2.joinNetwork(bootstrapNode);
        node3.joinNetwork(bootstrapNode);

        // Inspect routing tables
        System.out.println("Routing table of Node 1:");
        System.out.println(node1.getRoutingTable());

        System.out.println("Routing table of Node 2:");
        System.out.println(node2.getRoutingTable());

        System.out.println("Routing table of Node 3:");
        System.out.println(node3.getRoutingTable());
        */
    }
}
