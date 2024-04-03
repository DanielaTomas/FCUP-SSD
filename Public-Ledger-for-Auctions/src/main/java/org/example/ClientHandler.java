package org.example;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    private Kademlia.MessageType messageType;

    /**
     * Constructs a new ClientHandler.
     *
     * @param node           The local node.
     * @param targetNodeInfo Information about the target node.
     */
    public ClientHandler(Node node, NodeInfo targetNodeInfo, Kademlia.MessageType messageType) {
        this.targetNodeInfo = targetNodeInfo;
        this.node = node;
        this.nearNodesInfo = new ArrayList<>();
        this.messageType = messageType;
    }

    /**
     * Called when the channel becomes active.
     *
     * @param ctx The channel handler context.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        ByteBuf msg = ctx.alloc().buffer();
        msg.writeInt(messageType.ordinal());
        switch(messageType) {
            case FIND_NODE:
                ByteBuf nodeInfoBuf = Utils.serialize(node.getNodeInfo());
                msg.writeInt(nodeInfoBuf.readableBytes());
                msg.writeBytes(nodeInfoBuf);
                ctx.writeAndFlush(msg);
                logger.info("Sent node info to node " + targetNodeInfo.getIpAddr() + ":" + targetNodeInfo.getPort());
                break;
            case PING:
                ByteBuf pingBuf = Unpooled.wrappedBuffer("PING".getBytes());
                msg.writeInt(pingBuf.readableBytes());
                msg.writeBytes(pingBuf);
                ctx.writeAndFlush(msg);
                logger.info("Pinging " + targetNodeInfo.getIpAddr() + ":" + targetNodeInfo.getPort());
                break;
            case FIND_VALUE:
                //TODO
                break;
            case STORE:
                //TODO
                break;
            default:
                logger.warning("Received unknown message type: " + messageType);
                break;
        }
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
            Kademlia.MessageType messageType = Kademlia.MessageType.values()[bytebuf.readInt()];
            switch (messageType) {
                case FIND_NODE:
                    findNodeHandler(bytebuf);
                    break;
                case PING:
                    int pingAckLength = bytebuf.readInt();
                    ByteBuf pingAckBytes = bytebuf.readBytes(pingAckLength);
                    String pingAck = pingAckBytes.toString(StandardCharsets.UTF_8);;
                    logger.info("Received " + pingAck + " from " + ctx.channel().remoteAddress());
                    pingAckBytes.release();
                    break;
                case FIND_VALUE:
                    //TODO
                    break;
                case STORE:
                    //TODO
                    break;
                default:
                    logger.warning("Received unknown message type: " + messageType);
                    break;
            }
            bytebuf.release();
        }
        else {
            logger.warning("Received unknown message type from server: " + msg.getClass().getName());
        }
    }

    private void findNodeHandler(ByteBuf bytebuf) throws IOException, ClassNotFoundException {
        int nodeInfoListLength = bytebuf.readInt();
        ByteBuf nodeInfoListBytes = bytebuf.readBytes(nodeInfoListLength);
        Object deserializedObject = Utils.deserialize(nodeInfoListBytes);
        if (deserializedObject instanceof ArrayList) {
            ArrayList<NodeInfo> nodeInfoList = (ArrayList<NodeInfo>) deserializedObject;
            logger.info("Received near nodes info from server: " + nodeInfoList);
            nearNodesInfo.addAll(nodeInfoList);
            nodeInfoListBytes.release();
        } else {
            logger.warning("Received unknown message type from server: " + deserializedObject.getClass().getName());
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