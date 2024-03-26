package org.example;

import java.net.InetSocketAddress;
import java.util.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Class Node: Represents a node in a peer-to-peer network. */
public class Node {
    private static final Logger logger = Logger.getLogger(Node.class.getName());
    private String nodeId; //TODO 160-bit UID based on SHA-1
    private String ipAddr;
    private int port;
    private Map<String, String> routingTable;
    private Set<String> storage;


    /**
     * Constructs a node with the specified parameters.
     *
     * @param nodeId The ID of the node.
     * @param ipAddr The IP address of the node.
     * @param port   The port on which the node listens.
     */
    public Node(String nodeId, String ipAddr, int port) {
        this.nodeId = nodeId;
        this.ipAddr = ipAddr;
        this.port = port;
        this.routingTable = new HashMap<>();
        this.storage = new HashSet<>();
    }

    public void joinNetwork(Node bootstrapNode) {
        List<Node> nearNodes = bootstrapNode.findNode(this);
        if (nearNodes != null) {
            for (Node nearNode : nearNodes) {
                this.updateRoutingTable(nearNode);
                nearNode.updateRoutingTable(this);
            }
        }
    }

    public void updateRoutingTable(Node node) {
        routingTable.put(node.getNodeId(), node.getIpAddr() + ":" + node.getPort());
    }

    public List<Node> findNode(Node targetNode) { //TODO test
        this.updateRoutingTable(targetNode);
        EventLoopGroup group = new NioEventLoopGroup();
        List<Node> nearNodes = new ArrayList<>();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(ipAddr, port))
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<Node>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, Node node) throws Exception {
                                    nearNodes.add(node);
                                }
                            });
                        }
                    });

            ChannelFuture channelFuture = bootstrap.connect().sync();
            channelFuture.channel().writeAndFlush(targetNode);
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            group.shutdownGracefully();
        }
        return nearNodes;
    }

    public void ping(Node targetNode) { //TODO test
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                        }
                    });

            // connect to the target node
            ChannelFuture future = bootstrap.connect(targetNode.getIpAddr(), targetNode.getPort()).sync();
            Channel channel = future.channel();

            channel.writeAndFlush("PING"); // send ping message

            channel.closeFuture().sync(); // close the channel
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted while sending ping to node", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending ping to node", e);
        } finally {
            group.shutdownGracefully();
        }
    }

    public void findValue(String key) {
    }

    public void store(String key, String value) {
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public int getPort() {
        return port;
    }

    public Map<String, String> getRoutingTable() {
        return routingTable;
    }
}
