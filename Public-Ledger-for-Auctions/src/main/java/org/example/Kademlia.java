package org.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Class Kademlia */
public class Kademlia {
    public enum MessageType {
        PING, FIND_NODE, FIND_VALUE, STORE
    }
    private static final Logger logger = Logger.getLogger(Kademlia.class.getName());

    public void joinNetwork(Node node, NodeInfo bootstrapNodeInfo) {
        List<NodeInfo> nearNodes = findNode(node, bootstrapNodeInfo);
        for(NodeInfo nearNodeInfo : nearNodes) {
            node.updateRoutingTable(nearNodeInfo);
            List<NodeInfo> additionalNearNodes = findNode(node, nearNodeInfo);
            while(!additionalNearNodes.isEmpty()) {
                List<NodeInfo> nextNearNodes = new ArrayList<>();
                for (NodeInfo nextNearNodeInfo : additionalNearNodes) {
                    node.updateRoutingTable(nextNearNodeInfo);
                    List<NodeInfo> nextAdditionalNearNodes = findNode(node, nextNearNodeInfo);
                    nextNearNodes.addAll(nextAdditionalNearNodes);
                }
                additionalNearNodes = nextNearNodes;
            }
        }
    }

    private List<NodeInfo> findNode(Node node, NodeInfo targetNodeInfo) {
        List<NodeInfo> nearNodesInfo = new ArrayList<>();
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            connectToNode(targetNodeInfo, group, channel -> {
                ClientHandler clientHandler = new ClientHandler(node, targetNodeInfo, MessageType.FIND_NODE);
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

    public void ping(NodeInfo targetNodeInfo) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            connectToNode(targetNodeInfo, group, channel -> {
                ClientHandler clientHandler = new ClientHandler(null, targetNodeInfo, MessageType.PING);
                channel.pipeline().addLast(clientHandler);
            });
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted while sending ping to node", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending ping to node", e);
        } finally {
            group.shutdownGracefully().addListener(future -> {
                if (!future.isSuccess()) {
                    logger.log(Level.SEVERE, "Error shutting down event loop group", future.cause());
                }
            });
        }
    }

    public void findValue(String key) {
    }

    public void store(String key, String value) {
    }

    private void connectToNode(NodeInfo targetNodeInfo, EventLoopGroup group, MessagePassingQueue.Consumer<Channel> channelConsumer) throws InterruptedException {
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
