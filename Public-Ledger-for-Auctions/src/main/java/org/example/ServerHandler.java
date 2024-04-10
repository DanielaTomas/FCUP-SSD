package org.example;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.example.Kademlia.MessageType;

/** Class ServerHandler: Handles the client-side channel events */
class ServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = Logger.getLogger(ServerHandler.class.getName());

    private Node myNode;

    private static final int K = 2; //TODO ajustar valor

    /**
     * Constructor for ServerHandler.
     *
     * @param node The local node.
     */
    public ServerHandler(Node node) {
        this.myNode = node;
    }

    /**
     * Called when a message is received from a client.
     *
     * @param ctx The channel handler context.
     * @param msg The received message.
     * @throws IOException            If an I/O error occurs.
     * @throws ClassNotFoundException If the class of the serialized object cannot be found.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException, ClassNotFoundException { // handle incoming messages
        if (msg instanceof DatagramPacket packet) {
            ByteBuf bytebuf = packet.content();
            logger.info("Received packet from: " + packet.sender());
            MessageType messageType = MessageType.values()[bytebuf.readInt()];
            switch (messageType) {
                case FIND_NODE:
                    findNodeHandler(ctx, bytebuf, messageType, packet.sender());
                    break;
                case PING:
                    pingHandler(ctx, bytebuf, messageType, packet.sender());
                    break;
                case FIND_VALUE:
                    //TODO
                    break;
                case STORE:
                    storeHandler(ctx,bytebuf,messageType, packet.sender());
                    break;
                default:
                    logger.warning("Received unknown message type: " + messageType);
                    break;
            }
            bytebuf.release();
            //ctx.close();
        } else {
            logger.warning("Received unknown message type from client: " + msg.getClass().getName());
        }
    }

    /**
     * Handles STORE messages from the client.
     *
     * @param ctx          The channel handler context.
     * @param bytebuf      The received ByteBuf.
     * @param messageType  The type of the message.
     */
    private void storeHandler(ChannelHandlerContext ctx, ByteBuf bytebuf, MessageType messageType, InetSocketAddress sender) {
        int keyLength = bytebuf.readInt();
        String key = bytebuf.readCharSequence(keyLength, StandardCharsets.UTF_8).toString();
        int valueLength = bytebuf.readInt();
        String value = bytebuf.readCharSequence(valueLength, StandardCharsets.UTF_8).toString();
        logger.info("Received STORE request for key: " + key + ", value: " + value);

        myNode.storeKeyValue(key,value);
        logger.info("key: " + key + ", value: " + value + " stored");

        ByteBuf responseMsg = ctx.alloc().buffer();
        responseMsg.writeInt(messageType.ordinal());
        ByteBuf responseBuf = Unpooled.wrappedBuffer("STOREACK".getBytes());
        responseMsg.writeInt(responseBuf.readableBytes());
        responseMsg.writeBytes(responseBuf);
        ctx.writeAndFlush(new DatagramPacket(responseMsg, sender));
        logger.info("Responded to STORE from client " + sender);
    }

    /**
     * Handles FIND_NODE messages from the client.
     *
     * @param ctx          The channel handler context.
     * @param bytebuf      The received ByteBuf.
     * @param messageType  The type of the message.
     * @throws IOException            If an I/O error occurs.
     * @throws ClassNotFoundException If the class of the serialized object cannot be found.
     */
    private void findNodeHandler(ChannelHandlerContext ctx, ByteBuf bytebuf, MessageType messageType, InetSocketAddress sender) throws IOException, ClassNotFoundException {
        int nodeInfoLength = bytebuf.readInt();
        ByteBuf nodeInfoBytes = bytebuf.readBytes(nodeInfoLength);
        NodeInfo nodeInfo = (NodeInfo) Utils.deserialize(nodeInfoBytes);
        logger.info("Received node info from client: " + nodeInfo);

        myNode.updateRoutingTable(nodeInfo);
        List<NodeInfo> nearNodes = Utils.findClosestNodes(myNode.getRoutingTable(), nodeInfo, K);

        ByteBuf responseMsg = ctx.alloc().buffer();
        responseMsg.writeInt(messageType.ordinal());
        ByteBuf responseBuf = Utils.serialize(nearNodes);
        responseMsg.writeInt(responseBuf.readableBytes());
        responseMsg.writeBytes(responseBuf);
        ctx.writeAndFlush(new DatagramPacket(responseMsg, sender));
        logger.info("Sent near nodes info to client " + nodeInfo.getIpAddr() + ":" + nodeInfo.getPort());

        nodeInfoBytes.release();
    }

    /**
     * Handles PING messages from the client.
     *
     * @param ctx          The channel handler context.
     * @param bytebuf      The received ByteBuf.
     * @param messageType  The type of the message.
     */
    private void pingHandler(ChannelHandlerContext ctx, ByteBuf bytebuf, MessageType messageType, InetSocketAddress sender) {
        int pingLength = bytebuf.readInt();
        ByteBuf pingBytes = bytebuf.readBytes(pingLength);
        String pingStr = pingBytes.toString(StandardCharsets.UTF_8);
        logger.info("Received " + pingStr + " from client " + ctx.channel().remoteAddress());

        ByteBuf responseMsg = ctx.alloc().buffer();
        responseMsg.writeInt(messageType.ordinal());
        ByteBuf responseBuf = Unpooled.wrappedBuffer("PINGACK".getBytes());
        responseMsg.writeInt(responseBuf.readableBytes());
        responseMsg.writeBytes(responseBuf);
        ctx.writeAndFlush(new DatagramPacket(responseMsg, sender));
        logger.info("Responded to ping from client " + sender);
        pingBytes.release();
    }

    /**
     * Called when a read operation on the channel is complete.
     *
     * @param ctx The channel handler context.
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    /**
     * Called when an exception is caught in the channel.
     *
     * @param ctx   The channel handler context.
     * @param cause The exception caught.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.log(Level.SEVERE, "Exception caught in channel: " + ctx.channel().id(), cause);
        ctx.close();
    }
}