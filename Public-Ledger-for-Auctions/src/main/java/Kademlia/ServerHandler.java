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

/** Class ServerHandler: Handles the client-side channel events */
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
            logger.info("Received packet from: " + packet.sender());
            MessageType messageType = MessageType.values()[bytebuf.readInt()];
            switch (messageType) {
                case FIND_NODE, FIND_VALUE:
                    findNodeHandler(ctx, bytebuf, messageType, packet.sender());
                    break;
                case PING:
                    pingHandler(ctx, bytebuf, messageType, packet.sender());
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
            logger.info("Received node info from client: " + nodeInfo);
        }

        myNode.updateRoutingTable(nodeInfo);
        List<NodeInfo> nearNodes = Utils.findClosestNodes(myNode.getRoutingTable(), nodeInfo.getNodeId(), K);

        String success = "Sent near nodes info to client " + nodeInfo.getIpAddr() + ":" + nodeInfo.getPort();
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
        logger.info("Received key " + key + " and node info from client: " + nodeInfo);
        Object value = myNode.findValueByKey(key);
        if(value != null) {
            String success = "Responded to FIND_VALUE from client " + sender;
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
        logger.info("Received " + pingStr + " from client " + ctx.channel().remoteAddress());

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
        String success = "Responded to " + messageType + " from client " + sender;
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