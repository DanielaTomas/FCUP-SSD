package Kademlia;
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

import static Kademlia.Kademlia.MessageType;

/** Class ServerHandler: Handles the server-side channel events */
public class ServerHandler extends ChannelInboundHandlerAdapter {
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
            MessageType messageType = MessageType.values()[bytebuf.readInt()];
            logger.info("Received " + messageType + " packet from: " + packet.sender());
            switch (messageType) {
                case FIND_NODE, FIND_VALUE:
                    findNodeHandler(ctx, bytebuf, messageType, packet.sender());
                    break;
                case PING:
                    pingHandler(ctx, bytebuf, messageType, packet.sender());
                    break;
                case STORE:
                    storeHandler(ctx, bytebuf, messageType, packet.sender());
                    break;
                case NOTIFY:
                    notifyHandler(ctx, bytebuf, messageType, packet.sender());
                    break;
                case LATEST_BLOCK:
                    latestBlockHandler(ctx, bytebuf, messageType, packet.sender());
                    break;
                default:
                    logger.warning("Received unknown message type: " + messageType);
                    break;
            }
            bytebuf.release();
            //ctx.close();
        } else {
            logger.warning("Received unknown message type from node: " + msg.getClass().getName());
        }
    }

    /**
     * Handles the LATEST_BLOCK messages from the client.
     *
     * @param ctx           The ChannelHandlerContext for the channel.
     * @param bytebuf       The ByteBuf containing the latest block information.
     * @param messageType   The type of message received.
     * @param sender        The InetSocketAddress of the sender node.
     */
    private void latestBlockHandler(ChannelHandlerContext ctx, ByteBuf bytebuf, Kademlia.MessageType messageType, InetSocketAddress sender) {
        int latestBlockLength = bytebuf.readInt();
        ByteBuf latestBlockBytes = bytebuf.readBytes(latestBlockLength);
        String latestBlockStr = latestBlockBytes.toString(StandardCharsets.UTF_8);
        logger.info("Received " + latestBlockStr + " request from node " + ctx.channel().remoteAddress());

        ByteBuf responseMsg = ctx.alloc().buffer();
        responseMsg.writeInt(messageType.ordinal());
        Kademlia kademlia = Kademlia.getInstance();
        String latestBlock = kademlia.getLatestBlockHash().toString();
        ByteBuf responseBuf = Unpooled.wrappedBuffer(latestBlock.getBytes());
        responseMsg.writeInt(responseBuf.readableBytes());
        responseMsg.writeBytes(responseBuf);
        String success = "Sent latest block hash: " + latestBlock + " to node: " + sender.getAddress().getHostAddress() + ":" + sender.getPort();
        Utils.sendPacket(ctx, responseMsg, sender, messageType, success);

        latestBlockBytes.release();
    }

    /**
     * Handles NOTIFY messages from the client
     *
     * @param ctx           The ChannelHandlerContext for the channel.
     * @param bytebuf       The ByteBuf containing the new block hash information.
     * @param messageType   The type of message received.
     * @param sender        The InetSocketAddress of the sender node.
     */
    private void notifyHandler(ChannelHandlerContext ctx, ByteBuf bytebuf, Kademlia.MessageType messageType, InetSocketAddress sender) {
        int keyLength = bytebuf.readInt();
        String key = bytebuf.readCharSequence(keyLength, StandardCharsets.UTF_8).toString();

        logger.info("Received new block hash: " + key + " from node: " + sender);
        sendAck(ctx, messageType, sender);

        Kademlia kademlia = Kademlia.getInstance();
        kademlia.notifyNewBlockHash(myNode,key);
    }

    /**
     * Handles FIND_NODE messages from the client.
     *
     * @param ctx          The channel handler context.
     * @param bytebuf      The received ByteBuf.
     * @param messageType  The type of the message.
     * @param sender       The address of the sender node.
     * @throws IOException            If an I/O error occurs.
     * @throws ClassNotFoundException If the class of the serialized object cannot be found.
     */
    private void findNodeHandler(ChannelHandlerContext ctx, ByteBuf bytebuf, MessageType messageType, InetSocketAddress sender) throws IOException, ClassNotFoundException {
        int nodeInfoLength = bytebuf.readInt();
        ByteBuf nodeInfoBytes = bytebuf.readBytes(nodeInfoLength);
        NodeInfo nodeInfo = (NodeInfo) Utils.deserialize(nodeInfoBytes);

        if(messageType == MessageType.FIND_VALUE) {
            if(findValueHandler(ctx,bytebuf,nodeInfo, messageType, sender)) return;
            messageType = MessageType.FIND_NODE;
        } else {
            logger.info("Received node info from node: " + nodeInfo);
        }

        myNode.updateRoutingTable(nodeInfo);
        List<NodeInfo> nearNodes = Utils.findClosestNodes(myNode.getRoutingTable(), nodeInfo.getNodeId(), K);

        String success = "Sent near nodes info to node " + nodeInfo.getIpAddr() + ":" + nodeInfo.getPort();
        sendSerializedMessage(ctx,messageType,sender,nearNodes,success);

        nodeInfoBytes.release();
    }

    /**
     * Handles FIND_VALUE messages from the client.
     *
     * @param ctx          The channel handler context.
     * @param bytebuf      The received ByteBuf.
     * @param messageType  The type of the message.
     * @param sender       The address of the sender node
     */
    private boolean findValueHandler(ChannelHandlerContext ctx, ByteBuf bytebuf, NodeInfo nodeInfo, MessageType messageType, InetSocketAddress sender) throws IOException {
        int keyLength = bytebuf.readInt();
        String key = bytebuf.readCharSequence(keyLength, StandardCharsets.UTF_8).toString();
        logger.info("Received key " + key + " and node info from node: " + nodeInfo);
        Object value = myNode.findValueByKey(key);
        if(value != null) {
            String success = "Responded to FIND_VALUE from node " + sender;
            sendSerializedMessage(ctx, messageType, sender, value, success);
            return true;
        }
        return false;
    }

    /**
     * Send acknowledgement messages to the client.
     *
     * @param ctx          The channel handler context.
     * @param messageType  The type of the message.
     * @param sender       The address of the sender node
     */
    private void sendSerializedMessage(ChannelHandlerContext ctx, MessageType messageType, InetSocketAddress sender, Object msg, String success) throws IOException {
        ByteBuf responseMsg = ctx.alloc().buffer();
        responseMsg.writeInt(messageType.ordinal());
        ByteBuf responseBuf = Utils.serialize(msg);
        responseMsg.writeInt(responseBuf.readableBytes());
        responseMsg.writeBytes(responseBuf);
        Utils.sendPacket(ctx, responseMsg, sender, messageType, success);
    }

    /**
     * Handles STORE messages from the client.
     *
     * @param ctx          The channel handler context.
     * @param bytebuf      The received ByteBuf.
     * @param messageType  The type of the message.
     * @param sender       The address of the sender node
     */
    private void storeHandler(ChannelHandlerContext ctx, ByteBuf bytebuf, MessageType messageType, InetSocketAddress sender) throws IOException, ClassNotFoundException {
        int keyLength = bytebuf.readInt();
        String key = bytebuf.readCharSequence(keyLength, StandardCharsets.UTF_8).toString();
        int valueLength = bytebuf.readInt();
        if (bytebuf.readableBytes() < valueLength) {
            throw new IOException("Insufficient bytes for value (expected: " + valueLength + ", available: " + bytebuf.readableBytes() + ")");
        }
        ByteBuf valueBytes = bytebuf.readBytes(valueLength);
        Object value = Utils.deserialize(valueBytes);
        logger.info("Received STORE request for key: " + key + ", value: " + value);

        myNode.storeKeyValue(key,value);
        logger.info("key: " + key + ", value: " + value + " stored");

        sendAck(ctx,messageType,sender);
    }

    /**
     * Handles PING messages from the client.
     *
     * @param ctx          The channel handler context.
     * @param bytebuf      The received ByteBuf.
     * @param messageType  The type of the message.
     * @param sender       The address of the sender node
     */
    private void pingHandler(ChannelHandlerContext ctx, ByteBuf bytebuf, MessageType messageType, InetSocketAddress sender) {
        int pingLength = bytebuf.readInt();
        ByteBuf pingBytes = bytebuf.readBytes(pingLength);
        String pingStr = pingBytes.toString(StandardCharsets.UTF_8);
        logger.info("Received " + pingStr + " from node " + ctx.channel().remoteAddress());

        sendAck(ctx,messageType,sender);
        pingBytes.release();
    }

    /**
     * Send acknowledgement messages to the client.
     *
     * @param ctx          The channel handler context.
     * @param messageType  The type of the message.
     * @param sender       The address of the sender node
     */
    private void sendAck(ChannelHandlerContext ctx, MessageType messageType, InetSocketAddress sender) {
        ByteBuf responseMsg = ctx.alloc().buffer();
        responseMsg.writeInt(messageType.ordinal());
        String ack = messageType + "ACK";
        ByteBuf responseBuf = Unpooled.wrappedBuffer(ack.getBytes());
        responseMsg.writeInt(responseBuf.readableBytes());
        responseMsg.writeBytes(responseBuf);
        String success = "Responded to " + messageType + " from node " + sender;
        Utils.sendPacket(ctx, responseMsg, sender, messageType, success);
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