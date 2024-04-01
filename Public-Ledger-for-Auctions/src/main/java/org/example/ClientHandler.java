package org.example;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Class ClientHandler: Handles the client-side channel events */
public class ClientHandler  extends ChannelInboundHandlerAdapter {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    private Node node;
    private NodeInfo targetNodeInfo;
    private List<NodeInfo> nearNodesInfo;

    /**
     * Constructs a new ClientHandler.
     *
     * @param node           The local node.
     * @param targetNodeInfo Information about the target node.
     */
    public ClientHandler(Node node, NodeInfo targetNodeInfo) {
        this.targetNodeInfo = targetNodeInfo;
        this.node = node;
        this.nearNodesInfo = new ArrayList<>();
    }

    /**
     * Called when the channel becomes active.
     *
     * @param ctx The channel handler context.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        ByteBuf msg = Utils.serialize(node.getNodeInfo());
        ctx.writeAndFlush(msg);
        logger.info("Sent node info to node " + targetNodeInfo.getIpAddr() + ":" + targetNodeInfo.getPort());
    }

    /**
     * Called when a message is received from the server.
     *
     * @param ctx The channel handler context.
     * @param msg The received message.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof NodeInfo nodeInfo) {
            logger.info("Received node info from server: " + nodeInfo);
            nearNodesInfo.add(nodeInfo);
        } else {
            logger.warning("Received unknown message type from server: " + msg.getClass().getName());
        }
    }

    /**
     * Called when an exception is caught in the channel.
     *
     * @param ctx   The channel handler context.
     * @param cause The exception caught.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.log(Level.SEVERE, "Exception caught in client channel", cause);
        ctx.close();
    }

    /**
     * Gets the list of near nodes information.
     *
     * @return The list of near nodes information.
     */
    public List<NodeInfo> getNearNodesInfo() {
        return this.nearNodesInfo;
    }
}

/*
public class ClientHandler  extends ChannelInboundHandlerAdapter {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ByteBuf message = Unpooled.copiedBuffer("Hello, Netty!", StandardCharsets.UTF_8);
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        logger.info("Received from server: " + byteBuf.toString(StandardCharsets.UTF_8));
        byteBuf.release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.log(Level.SEVERE, "Exception caught in client channel", cause);
        ctx.close();
    }
}
 */