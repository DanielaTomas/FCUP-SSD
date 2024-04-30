package Kademlia;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/** Class Kademlia */
public class Kademlia {

    private static final Logger logger = Logger.getLogger(Kademlia.class.getName());
    private static final int K = 2; //TODO ajustar valor
    private static Kademlia instance;

    /**
     * Enum for message types used in Kademlia.
     */
    public enum MessageType {
        PING, FIND_NODE, FIND_VALUE, STORE
    }

    /**
     * Private constructor for the Kademlia class. This constructor is private to enforce the Singleton design pattern.
     */
    private Kademlia() {}

    /**
     * Gets the singleton instance of the Kademlia class.
     *
     * @return The singleton instance of the Kademlia class.
     */
    public static Kademlia getInstance(){
        if(instance == null){
            instance = new Kademlia();
        }

        return instance;
    }

    /**
     * Joins the Kademlia network.
     *
     * @param myNode              The local node.
     * @param bootstrapNodeInfo Information about the bootstrap node.
     */
    public void joinNetwork(Node myNode, NodeInfo bootstrapNodeInfo) {
        logger.info("Kademlia - Trying to contact bootstrap");
        List<NodeInfo> nearNodes = findNode(myNode.getNodeInfo(), bootstrapNodeInfo);
        for(NodeInfo nearNodeInfo : nearNodes) {
            myNode.updateRoutingTable(nearNodeInfo);
            List<NodeInfo> additionalNearNodesInfo = findNode(myNode.getNodeInfo(), nearNodeInfo);
            while(!additionalNearNodesInfo.isEmpty()) {
                List<NodeInfo> nextNearNodes = new ArrayList<>();
                for (NodeInfo nextNearNodeInfo : additionalNearNodesInfo) {
                    if(!myNode.getRoutingTable().contains(nextNearNodeInfo)) {
                        myNode.updateRoutingTable(nextNearNodeInfo);
                        List<NodeInfo> nextAdditionalNearNodes = findNode(myNode.getNodeInfo(), nextNearNodeInfo);
                        nextNearNodes.addAll(nextAdditionalNearNodes);
                    }
                }
                additionalNearNodesInfo = nextNearNodes;
            }
        }
    }

    /**
     * Finds the closest nodes to the target node by sending a FIND_NODE message.
     *
     * @param myNodeInfo       Information about the local node.
     * @param targetNodeInfo Information about the target node.
     * @return List of near nodes.
     */
    public List<NodeInfo> findNode(NodeInfo myNodeInfo, NodeInfo targetNodeInfo) {
        logger.info("Kademlia - Starting FIND_NODE RPC");
        return (List<NodeInfo>) connectAndHandle(myNodeInfo, targetNodeInfo, null, null, MessageType.FIND_NODE);
    }

    /**
     * Sends a ping message to the target node.
     *
     * @param myNodeInfo       Information about the local node.
     * @param targetNodeId   ID of the target node .
     * @param routingTable
     */
    public void ping(NodeInfo myNodeInfo, String targetNodeId , List<NodeInfo> routingTable) {
        logger.info("Kademlia - Starting PING RPC");
        for (NodeInfo targetNodeInfo : routingTable) {
            if (targetNodeInfo.getNodeId().equals(targetNodeId) ){
                logger.info("Kademlia - Found node: " + targetNodeInfo);
                connectAndHandle(myNodeInfo, targetNodeInfo, null, null, MessageType.PING);
            }
        }
    }

    /**
     * Finds the value corresponding to a key in the Kademlia network.
     *
     * @param myNode The local node.
     * @param key The key to find.
     */
    public Object findValue(Node myNode, String key) { //FIXME ?
        logger.info("Kademlia - Starting FIND_VALUE RPC");
        String storedValue = myNode.findValueByKey(key);
        if(storedValue != null) {
            logger.info("Stored value: " + storedValue);
            return storedValue;
        }

        NodeInfo keyInfo = myNode.findNodeById(key);
        if(keyInfo != null) return findNode(myNode.getNodeInfo(), keyInfo);
        return null;
    }

    /**
     * Stores a key-value pair in the Kademlia network.
     *
     * @param myNode  The local node.
     * @param key   The key to store.
     * @param value The value corresponding to the key.
     */
    public void store(Node myNode, String key, String value) { //FIXME ?
        logger.info("Kademlia - Starting STORE RPC");
        NodeInfo keyInfo = myNode.findNodeById(key);
        NodeInfo targetNodeInfo = findNodeForKey(myNode, keyInfo);
        if (targetNodeInfo != null) {
            if (targetNodeInfo.getNodeId().equals(myNode.getNodeInfo().getNodeId())) {
                myNode.storeKeyValue(key, value);
                logger.info("key: " + key + ", value: " + value + " stored");
            } else {
                connectAndHandle(myNode.getNodeInfo(), targetNodeInfo, key, value, MessageType.STORE);
            }
        }
        else {
            System.err.println("Error: Unable to find a node to store the key-value pair.");
        }
    }

    /**
     * Finds the node in the network that is closest to the given key.
     * This method is used for key-based routing in the Kademlia DHT protocol.
     *
     * @param myNode       The local node performing the search.
     * @param keyInfo    Information about the key (usually represented as a NodeInfo object).
     * @return The NodeInfo object representing the node closest to the given key, or null if the keyInfo parameter is null or the routing table is empty.
     */
    private NodeInfo findNodeForKey(Node myNode, NodeInfo keyInfo) {
        if(keyInfo == null) return null;
        NodeInfo closestNode = myNode.getNodeInfo();
        int closestDistance = Utils.calculateDistance(myNode.getNodeInfo().getNodeId(), keyInfo.getNodeId());
        List<NodeInfo> routingTable = myNode.getRoutingTable();

        List<NodeInfo> nearNodesInfo = Utils.findClosestNodes(routingTable, keyInfo, K);

        for (NodeInfo nearNodeInfo : nearNodesInfo) {
            List<NodeInfo> keyNearNodes = findNode(keyInfo, nearNodeInfo);
            for(NodeInfo keyNearNode : keyNearNodes) {
                if(!routingTable.contains(keyNearNode)) {
                    myNode.updateRoutingTable(keyNearNode);
                }
                int distance = Utils.calculateDistance(keyNearNode.getNodeId(), keyInfo.getNodeId());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestNode = nearNodeInfo;
                }
            }
        }
        return closestNode;
    }

    /**
     * Connects to a target node, sends a message, and handles the response.
     *
     * @param myNodeInfo        Information about the local node.
     * @param targetNodeInfo  Information about the target node.
     * @param key             The key for the message (optional, used in STORE message).
     * @param value           The value for the message (optional, used in STORE message).
     * @param messageType     The type of message to send.
     * @return List of near nodes.
     */
    private Object connectAndHandle(NodeInfo myNodeInfo, NodeInfo targetNodeInfo, String key, String value, MessageType messageType) {
        List<NodeInfo> nearNodesInfo = new ArrayList<>();
        AtomicReference<String> storedValue = new AtomicReference<>(null);;
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            connectToNode(myNodeInfo, targetNodeInfo, group, channel -> {
                ClientHandler clientHandler = new ClientHandler(myNodeInfo, targetNodeInfo, key, value, messageType, nearNodesInfo);
                channel.pipeline().addLast(clientHandler);
                if (messageType == MessageType.FIND_VALUE) storedValue.set(clientHandler.getStoredValue());
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.severe("Interrupted while connecting to node: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Error connecting to node: " + e.getMessage());
        } finally {
            group.shutdownGracefully().addListener(future -> {
                if (!future.isSuccess()) {
                    logger.severe("Error shutting down event loop group: " + future.cause().getMessage());
                }
            });
        }
        if(messageType == MessageType.FIND_NODE) return nearNodesInfo;
        else if(messageType == MessageType.FIND_VALUE) return storedValue;
        return null;
    }

    /**
     * Connects to a target node and sets up the channel.
     *
     * @param myNodeInfo        Information about the local node.
     * @param targetNodeInfo  Information about the target node.
     * @param group           The event loop group.
     * @param channelConsumer Consumer function to set up the channel pipeline.
     * @throws InterruptedException If the connection is interrupted.
     */
    private void connectToNode(NodeInfo myNodeInfo, NodeInfo targetNodeInfo, EventLoopGroup group, MessagePassingQueue.Consumer<Channel> channelConsumer) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.AUTO_CLOSE, true)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel ch) throws Exception {
                        channelConsumer.accept(ch);
                    }
                });

        //bootstrap.localAddress(myNodeInfo.getPort()); //TODO
        ChannelFuture channelFuture = bootstrap.connect(targetNodeInfo.getIpAddr(), targetNodeInfo.getPort()).sync();
        logger.info("Connection established to node " + targetNodeInfo.getIpAddr() + ":" + targetNodeInfo.getPort());
        channelFuture.channel().closeFuture().await(3, TimeUnit.SECONDS);
    }
}
