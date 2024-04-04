package org.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Class Kademlia */
public class Kademlia {

    /**
     * Enum for message types used in Kademlia.
     */
    public enum MessageType {
        PING, FIND_NODE, FIND_VALUE, STORE
    }
    private static final Logger logger = Logger.getLogger(Kademlia.class.getName());

    /**
     * Joins the Kademlia network.
     *
     * @param node              The local node.
     * @param bootstrapNodeInfo Information about the bootstrap node.
     */
    public void joinNetwork(Node node, NodeInfo bootstrapNodeInfo) {
        List<NodeInfo> nearNodes = findNode(node.getNodeInfo(), bootstrapNodeInfo);
        for(NodeInfo nearNodeInfo : nearNodes) {
            node.updateRoutingTable(nearNodeInfo);
            List<NodeInfo> additionalNearNodes = findNode(node.getNodeInfo(), nearNodeInfo);
            while(!additionalNearNodes.isEmpty()) {
                List<NodeInfo> nextNearNodes = new ArrayList<>();
                for (NodeInfo nextNearNodeInfo : additionalNearNodes) {
                    node.updateRoutingTable(nextNearNodeInfo);
                    List<NodeInfo> nextAdditionalNearNodes = findNode(node.getNodeInfo(), nextNearNodeInfo);
                    nextNearNodes.addAll(nextAdditionalNearNodes);
                }
                additionalNearNodes = nextNearNodes;
            }
        }
    }

    /**
     * Finds the closest nodes to the target node by sending a FIND_NODE message.
     *
     * @param nodeInfo       Information about the local node.
     * @param targetNodeInfo Information about the target node.
     * @return List of near nodes.
     */
    private List<NodeInfo> findNode(NodeInfo nodeInfo, NodeInfo targetNodeInfo) {
        return connectAndHandle(nodeInfo, targetNodeInfo, MessageType.FIND_NODE);
    }

    /**
     * Sends a ping message to the target node.
     *
     * @param nodeInfo       Information about the local node.
     * @param targetNodeInfo Information about the target node.
     */
    public void ping(NodeInfo nodeInfo, NodeInfo targetNodeInfo) {
        connectAndHandle(nodeInfo, targetNodeInfo, MessageType.PING);
    }

    public void findValue(String key) { //TODO
    }

    public void store(String key, String value) { //TODO
    }

    /**
     * Connects to a target node, sends a message, and handles the response.
     *
     * @param nodeInfo        Information about the local node.
     * @param targetNodeInfo  Information about the target node.
     * @param messageType     The type of message to send.
     * @return List of near nodes.
     */
    private List<NodeInfo> connectAndHandle(NodeInfo nodeInfo, NodeInfo targetNodeInfo, MessageType messageType) {
        List<NodeInfo> nearNodesInfo = new ArrayList<>();
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            connectToNode(nodeInfo, targetNodeInfo, group, channel -> {
                ClientHandler clientHandler = new ClientHandler(nodeInfo, targetNodeInfo, messageType);
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
        return nearNodesInfo;
    }

    /**
     * Connects to a target node and sets up the channel.
     *
     * @param nodeInfo        Information about the local node.
     * @param targetNodeInfo  Information about the target node.
     * @param group           The event loop group.
     * @param channelConsumer Consumer function to set up the channel pipeline.
     * @throws InterruptedException If the connection is interrupted.
     */
    private void connectToNode(NodeInfo nodeInfo, NodeInfo targetNodeInfo, EventLoopGroup group, MessagePassingQueue.Consumer<Channel> channelConsumer) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        channelConsumer.accept(ch);
                    }
                });

        bootstrap.localAddress(nodeInfo.getPort());
        ChannelFuture cf = bootstrap.connect(targetNodeInfo.getIpAddr(), targetNodeInfo.getPort()).sync();
        logger.info("Connection established to node " + targetNodeInfo.getIpAddr() + ":" + targetNodeInfo.getPort());
        try {
            if (!cf.channel().closeFuture().await(8, TimeUnit.SECONDS)) {
                System.err.println("Error: Channel did not close within 8 seconds.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logger.info("Connection closed with node " + targetNodeInfo.getIpAddr() + ":" + targetNodeInfo.getPort());
    }
}
