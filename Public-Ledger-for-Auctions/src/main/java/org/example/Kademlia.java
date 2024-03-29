package org.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Class Kademlia */
public class Kademlia { //TODO testar tudo
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
        //TODO update targetNode routing table
        List<NodeInfo> nearNodesInfo = new ArrayList<>();
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(targetNodeInfo.getIpAddr(), targetNodeInfo.getPort()))
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<NodeInfo>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, NodeInfo nodeInfo) throws Exception {
                                    nearNodesInfo.add(nodeInfo);
                                }
                            });
                        }
                    });

            ChannelFuture channelFuture = bootstrap.connect().sync();
            channelFuture.channel().writeAndFlush(node.getNodeInfo());
            channelFuture.channel().closeFuture().sync();
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

    public void ping(NodeInfo targetNode) {
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
            ChannelFuture future = bootstrap.connect(targetNode.getIpAddr(), targetNode.getPort()).sync(); // connect to the target node
            Channel channel = future.channel();
            channel.writeAndFlush("PING"); // send ping message

            channel.closeFuture().sync(); // close the channel
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
}
