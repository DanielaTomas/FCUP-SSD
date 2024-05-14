package Kademlia;

import Auctions.Auction;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static BlockChain.Constants.GENESIS_PREV_HASH;
import static Kademlia.Utils.findClosestNodes;

/** Class Kademlia */
public class Kademlia {

    private static final Logger logger = Logger.getLogger(Kademlia.class.getName());
    private static final int K = 2; //TODO ajustar valor
    private static Kademlia instance;
    private StringBuilder latestBlockHash;
    private StringBuilder auctionId; //TODO Set<StringBuilder>?

    /**
     * Enum for message types used in Kademlia.
     */
    public enum MessageType {
        PING, FIND_NODE, FIND_VALUE, STORE, NOTIFY, LATEST_BLOCK, NEW_AUCTION, NEW_BID
    }

    /**
     * Private constructor for the Kademlia class. This constructor is private to enforce the Singleton design pattern.
     */
    private Kademlia() {
        this.latestBlockHash = new StringBuilder(GENESIS_PREV_HASH);
        this.auctionId = null;
    }

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
     * @param bootstrapNodeId id of the bootstrap node.
     */
    public void joinNetwork(Node myNode, String bootstrapNodeId) {
        logger.info("Kademlia - Trying to contact bootstrap");
        List<NodeInfo> nearNodes = findNode(myNode.getNodeInfo(), bootstrapNodeId, myNode.getRoutingTable());

        connectAndHandle(myNode.getNodeInfo(), myNode.findNodeInfoById(bootstrapNodeId), null, null, MessageType.LATEST_BLOCK);

        myNode.getRoutingTable().remove(myNode.findNodeInfoById(bootstrapNodeId));

        for(NodeInfo nearNodeInfo : nearNodes) {
            myNode.updateRoutingTable(nearNodeInfo);
            List<NodeInfo> additionalNearNodesInfo = findNode(myNode.getNodeInfo(), nearNodeInfo.getNodeId(), myNode.getRoutingTable());
            while(!additionalNearNodesInfo.isEmpty()) {
                List<NodeInfo> nextNearNodes = new ArrayList<>();
                for (NodeInfo nextNearNodeInfo : additionalNearNodesInfo) {
                    if(!myNode.getRoutingTable().contains(nextNearNodeInfo)) {
                        myNode.updateRoutingTable(nextNearNodeInfo);
                        List<NodeInfo> nextAdditionalNearNodes = findNode(myNode.getNodeInfo(), nextNearNodeInfo.getNodeId(), myNode.getRoutingTable());
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
     * @param targetNodeId id of the target node.
     * @param routingTable routing table of the local node.
     *
     * @return List of near nodes.
     */
    public List<NodeInfo> findNode(NodeInfo myNodeInfo, String targetNodeId, Set<NodeInfo> routingTable) {
        logger.info("Kademlia - Starting FIND_NODE RPC");

        for (NodeInfo nodeInfo : routingTable) {
            if (nodeInfo.getNodeId().equals(targetNodeId)){
                logger.info("Kademlia - Found node: " + nodeInfo);
                return (List<NodeInfo>) connectAndHandle(myNodeInfo, nodeInfo, null, null, MessageType.FIND_NODE);
            }
        }

        List<NodeInfo> closestNodes = findClosestNodes(routingTable, targetNodeId, K);
        List<NodeInfo> nodeInfoList = new ArrayList<>();
        for (NodeInfo closestNode : closestNodes) {
            nodeInfoList.addAll((List<NodeInfo>) connectAndHandle(myNodeInfo, closestNode, null, null, MessageType.FIND_NODE));
        }

        for(NodeInfo nodeInfo : nodeInfoList) {
            if(nodeInfo.getNodeId().equals(targetNodeId)) {
                logger.info("Kademlia - Found node: " + nodeInfo);
                return (List<NodeInfo>) connectAndHandle(myNodeInfo, nodeInfo, null, null, MessageType.FIND_NODE);
            }
        }

        logger.info("Kademlia - Node not found");
        return nodeInfoList;
    }

    /**
     * Sends a ping message to the target node.
     *
     * @param myNodeInfo       Information about the local node.
     * @param targetNodeId   ID of the target node.
     * @param routingTable   Routing table of the local node.
     */
    public void ping(NodeInfo myNodeInfo, String targetNodeId , Set<NodeInfo> routingTable) {
        logger.info("Kademlia - Starting PING RPC");
        for (NodeInfo targetNodeInfo : routingTable) {
            if (targetNodeInfo.getNodeId().equals(targetNodeId) ){
                logger.info("Kademlia - Found node: " + targetNodeInfo);
                connectAndHandle(myNodeInfo, targetNodeInfo, null, null, MessageType.PING);
                return;
            }
        }
        logger.info("Kademlia - Node not found, PING failed");
    }

    /**
     * Finds the value corresponding to a key in the Kademlia network.
     *
     * @param myNode The local node.
     * @param key The key to find.
     */
    public Object findValue(Node myNode, String key) {
        logger.info("Kademlia - Starting FIND_VALUE RPC");
        Object storedValue = myNode.findValueByKey(key);
        if(storedValue != null) {
            logger.info("Stored value: " + storedValue);
            return storedValue;
        }

        findNode(myNode.getNodeInfo(),key,myNode.getRoutingTable());

        List<NodeInfo> keyNearNodes = findClosestNodes(myNode.getRoutingTable(), key, K);
        for (NodeInfo keyNearNode : keyNearNodes) {
            Object result = connectAndHandle(myNode.getNodeInfo(), keyNearNode, key, new ValueWrapper(null), MessageType.FIND_VALUE);
            logger.info("Kademlia - Value found: " + result);
            return result;
        }

        return keyNearNodes;
    }

    /**
     * Stores a key-value pair in the Kademlia network.
     *
     * @param myNode  The local node.
     * @param key   The key to store.
     * @param value The value corresponding to the key.
     */
    public void store(Node myNode, String key, ValueWrapper value) {
        logger.info("Kademlia - Starting STORE RPC");

        findNode(myNode.getNodeInfo(),key,myNode.getRoutingTable());
        List<NodeInfo> keyNearNodes = findClosestNodes(myNode.getRoutingTable(), key, K);

        NodeInfo targetNodeInfo = findNodeForKey(myNode.getNodeInfo(), key, keyNearNodes);
        if (targetNodeInfo != null) {
            if (targetNodeInfo.equals(myNode.getNodeInfo())) {
                myNode.storeKeyValue(key, value);
                logger.info("key: " + key + ", value: " + value + " stored");
            } else {
                connectAndHandle(myNode.getNodeInfo(), targetNodeInfo, key, value, MessageType.STORE);
            }
        }
        else {
            logger.severe("Error: Unable to find a node to store the key-value pair.");
        }
    }

    /**
     * Finds the node in the network that is closest to the given key.
     * This method is used for key-based routing in the Kademlia DHT protocol.
     *
     * @param myNodeInfo       The local node information.
     * @param key              id of the key.
     * @param keyNearNodes     key near nodes.
     * @return the node info closest to the given key.
     */
    private NodeInfo findNodeForKey(NodeInfo myNodeInfo, String key, List<NodeInfo> keyNearNodes) {
        NodeInfo closestNode = myNodeInfo;
        int closestDistance = Utils.calculateDistance(myNodeInfo.getNodeId(), key);

        for (NodeInfo keyNearNodeInfo : keyNearNodes) {
            int distance = Utils.calculateDistance(keyNearNodeInfo.getNodeId(), key);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestNode = keyNearNodeInfo;
            }
        }
        return closestNode;
    }

    /**
     * Notifies nodes in the network about a new block hash, if it is different from the latest known hash.
     *
     * @param myNodeInfo       The local node info.
     * @param routingTable  The local node routing table.
     * @param newBlockHash The new block hash to notify about.
     */
    public void notifyNewBlockHash(NodeInfo myNodeInfo, Set<NodeInfo> routingTable, String newBlockHash) {
        logger.info("Kademlia - Starting notify new block hash");
        if(!newBlockHash.contentEquals(this.latestBlockHash)) {
            latestBlockHash = new StringBuilder(newBlockHash);
            logger.info("New block hash updated");
            for (NodeInfo targetNodeInfo : routingTable) {
                connectAndHandle(myNodeInfo, targetNodeInfo, newBlockHash, null, MessageType.NOTIFY);
            }
        }
        else {
            logger.info("New block hash already updated");
        }
    }

    public void broadcastNewAuction(NodeInfo myNodeInfo, Set<NodeInfo> routingTable, String auctionId) {
        logger.info("Kademlia - Starting broadcast new auction");
        if(this.auctionId == null || !auctionId.contentEquals(this.auctionId)) {
            this.auctionId = new StringBuilder(auctionId);
            for(NodeInfo targetNodeInfo : routingTable) {
                connectAndHandle(myNodeInfo, targetNodeInfo, auctionId, null, MessageType.NEW_AUCTION);
            }
        } else {
            logger.info("New auction already broadcasted");
        }
    }

    public void notifyNewBid(NodeInfo myNodeInfo, Set<NodeInfo> routingTable, Auction auction) {
        logger.info("Kademlia - Starting notify new bid");
        for(String targetNodeId : auction.getSubscribers()) {
            findNode(myNodeInfo,targetNodeId,routingTable);
        }

        String auctionId = auction.getId();
        Double bid = auction.getCurrentBid();
        for(NodeInfo targetNodeInfo : routingTable) {
            if(auction.isSubscriber(targetNodeInfo.getNodeId())) {
                connectAndHandle(myNodeInfo, targetNodeInfo, auctionId, new ValueWrapper(bid), MessageType.NEW_BID);
            }
        }
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
    private Object connectAndHandle(NodeInfo myNodeInfo, NodeInfo targetNodeInfo, String key, ValueWrapper value, MessageType messageType) {
        List<NodeInfo> nearNodesInfo = new ArrayList<>();
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            connectToNode(myNodeInfo, targetNodeInfo, group, channel -> {
                ClientHandler clientHandler = new ClientHandler(myNodeInfo, targetNodeInfo, key, value, messageType, nearNodesInfo);
                channel.pipeline().addLast(clientHandler);
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
        else if(messageType == MessageType.FIND_VALUE) return value.getValue();
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
                    protected void initChannel(NioDatagramChannel ch) {
                        channelConsumer.accept(ch);
                    }
                });

        bootstrap.localAddress(myNodeInfo.getPort()+1); //TODO
        ChannelFuture channelFuture = bootstrap.connect(targetNodeInfo.getIpAddr(), targetNodeInfo.getPort()).sync();
        logger.info("Connection established to node " + targetNodeInfo.getIpAddr() + ":" + targetNodeInfo.getPort());
        channelFuture.channel().closeFuture().await(3, TimeUnit.SECONDS);
    }

    public StringBuilder getLatestBlockHash() {
        return this.latestBlockHash;
    }

    public void setLatestBlockHash(String latestBlockHash) {
        this.latestBlockHash = new StringBuilder(latestBlockHash);
    }
}
