package org.example;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Class ClientHandler: Handles the client-side channel events */
public class ClientHandler  extends ChannelInboundHandlerAdapter {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    private NodeInfo nodeInfo;
    private NodeInfo targetNodeInfo;
    private List<NodeInfo> nearNodesInfo;
    private Kademlia.MessageType messageType;
    private String key;
    private String value;

    /**
     * Constructs a new ClientHandler.
     *
     * @param nodeInfo       The local node info.
     * @param targetNodeInfo Information about the target node.
     * @param key            The key for the message.
     * @param value          The value for the message.
     * @param messageType    The type of the message.
     * @param nearNodesInfo  Information about the near nodes.
     */
    public ClientHandler(NodeInfo nodeInfo, NodeInfo targetNodeInfo, String key, String value, Kademlia.MessageType messageType, List<NodeInfo> nearNodesInfo) {
        this.targetNodeInfo = targetNodeInfo;
        this.nodeInfo = nodeInfo;
        this.nearNodesInfo = nearNodesInfo;
        this.messageType = messageType;
        this.key = key;
        this.value = value;
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
                ByteBuf nodeInfoBuf = Utils.serialize(nodeInfo);
                msg.writeInt(nodeInfoBuf.readableBytes());
                msg.writeBytes(nodeInfoBuf);
                ctx.writeAndFlush(new DatagramPacket(msg, new InetSocketAddress(targetNodeInfo.getIpAddr(), targetNodeInfo.getPort())));
                logger.info("Sent node info to node " + targetNodeInfo.getIpAddr() + ":" + targetNodeInfo.getPort());
                break;
            case PING:
                ByteBuf pingBuf = Unpooled.wrappedBuffer("PING".getBytes());
                msg.writeInt(pingBuf.readableBytes());
                msg.writeBytes(pingBuf);
                ctx.writeAndFlush(new DatagramPacket(msg, new InetSocketAddress(targetNodeInfo.getIpAddr(), targetNodeInfo.getPort())));
                logger.info("Pinging " + targetNodeInfo.getIpAddr() + ":" + targetNodeInfo.getPort());
                break;
            case FIND_VALUE:
                //TODO
                break;
            case STORE:
                msg.writeInt(key.length());
                msg.writeCharSequence(key, StandardCharsets.UTF_8);
                msg.writeInt(value.length());
                msg.writeCharSequence(value, StandardCharsets.UTF_8);
                ctx.writeAndFlush(new DatagramPacket(msg, new InetSocketAddress(targetNodeInfo.getIpAddr(), targetNodeInfo.getPort())));
                logger.info("Sent STORE request for key: " + key + ", value: " + value + " to node " + targetNodeInfo.getIpAddr() + ":" + targetNodeInfo.getPort());
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
        if (msg instanceof DatagramPacket packet) {
            ByteBuf bytebuf = packet.content();
            Kademlia.MessageType messageType = Kademlia.MessageType.values()[bytebuf.readInt()];
            switch (messageType) {
                case FIND_NODE:
                    findNodeHandler(bytebuf);
                    break;
                case PING, STORE:
                    pingAndStoreHandler(ctx,bytebuf);
                    break;
                case FIND_VALUE:
                    //TODO
                    break;
                default:
                    logger.warning("Received unknown message type: " + messageType);
                    break;
            }
            bytebuf.release();
            //ctx.close();
        }
        else {
            logger.warning("Received unknown message type from server: " + msg.getClass().getName());
        }
    }

    /**
     * Handles the response from the server for FIND_NODE messages.
     *
     * @param bytebuf The received ByteBuf.
     * @throws IOException            If an I/O error occurs.
     * @throws ClassNotFoundException If the class of the serialized object cannot be found.
     */
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
     * Handles the response from the server for PING and STORE messages.
     *
     * @param ctx     The channel handler context.
     * @param bytebuf The received ByteBuf.
     */
    private void pingAndStoreHandler(ChannelHandlerContext ctx, ByteBuf bytebuf) {
        int ackLength = bytebuf.readInt();
        ByteBuf ackBytes = bytebuf.readBytes(ackLength);
        String ack = ackBytes.toString(StandardCharsets.UTF_8);;
        logger.info("Received " + ack + " from " + ctx.channel().remoteAddress());
        ackBytes.release();
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