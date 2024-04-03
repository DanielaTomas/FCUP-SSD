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
    //private Kademlia.MessageType messageType;

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
        //this.messageType = messageType;
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
        /*TODO
        int nodeInfoSerializedLength = Utils.serialize(node.getNodeInfo()).readableBytes();
        int requiredBytes = nodeInfoSerializedLength + Integer.BYTES; // Calculate required bytes for the additional integer
        // Ensure the ByteBuf has enough capacity
        ByteBuf msg = ctx.alloc().buffer(requiredBytes);
        msg.writeInt(messageType.ordinal());
        msg.writeBytes(Utils.serialize(node.getNodeInfo()));
        msg.writeInt(messageType.ordinal());
        switch (messageType) {
            case FIND_NODE:
                break;
            case PING:
                ctx.writeAndFlush("PING");
                logger.info("Pinging " + targetNodeInfo.getIpAddr() + ":" + targetNodeInfo.getPort());
                break;
            default:
                logger.warning("Received unknown message type: " + messageType);
                break;
        }
        msg.release();*/
    }

    /**
     * Called when a message is received from the server.
     *
     * @param ctx The channel handler context.
     * @param msg The received message.
     * @throws IOException            If an I/O error occurs.
     * @throws ClassNotFoundException If the class of the serialized object cannot be found.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException, ClassNotFoundException {
        if (msg instanceof ByteBuf bytebuf) {
            Object deserializedObject = Utils.deserialize(bytebuf);
            if (deserializedObject instanceof ArrayList) {
                ArrayList<NodeInfo> nodeInfoList = (ArrayList<NodeInfo>) deserializedObject;
                logger.info("Received near nodes info from server: " + nodeInfoList);
                nearNodesInfo.addAll(nodeInfoList);
            }
            else {
                logger.warning("Received unknown message type from server: " + deserializedObject.getClass().getName());
            }
        }
        else {
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
     * Gets the list of near nodes' information.
     *
     * @return The list of near nodes' information.
     */
    public List<NodeInfo> getNearNodesInfo() {
        return this.nearNodesInfo;
    }
}