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

/** Class ClientHandler: Handles the client-side channel events */
public class ClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    private NodeInfo nodeInfo;
    private NodeInfo targetNodeInfo;
    private List<NodeInfo> nearNodesInfo;
    private Kademlia.MessageType messageType;
    private String key;
    private Object value;
    private Timer timer;
    private byte[] randomId;

    /**
     * Constructs a new ClientHandler.
     *
     * @param nodeInfo       The local node info.
     * @param targetNodeInfo Information about the target node.
     * @param key            The key for the message.
     * @param value          The value for the message.
     * @param messageType    The type of the message.
     * @param nearNodesInfo  Information about the near nodes. //TODO tirar do construtor
     */
    public ClientHandler(NodeInfo nodeInfo, NodeInfo targetNodeInfo, String key, Object value, Kademlia.MessageType messageType, List<NodeInfo> nearNodesInfo) {
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
        this.randomId = Utils.generateRandomId();
        msg.writeInt(randomId.length);
        msg.writeBytes(randomId);
        String success;
        switch(messageType) {
            case FIND_NODE, FIND_VALUE:
                ByteBuf nodeInfoBuf = Utils.serialize(nodeInfo);
                msg.writeInt(nodeInfoBuf.readableBytes());
                msg.writeBytes(nodeInfoBuf);
                if(messageType == Kademlia.MessageType.FIND_VALUE) {
                    msg.writeInt(key.length());
                    msg.writeCharSequence(key, StandardCharsets.UTF_8);
                    success = "Sent key: " + key + " and node info to node " + targetNodeInfo.getIpAddr() + ":" + targetNodeInfo.getPort();
                } else {
                    success = "Sent node info to node " + targetNodeInfo.getIpAddr() + ":" + targetNodeInfo.getPort();
                }
                Utils.sendPacket(ctx, msg, new InetSocketAddress(targetNodeInfo.getIpAddr(), targetNodeInfo.getPort()), messageType, success);
                break;
            case PING:
                ByteBuf pingBuf = Unpooled.wrappedBuffer("PING".getBytes());
                msg.writeInt(pingBuf.readableBytes());
                msg.writeBytes(pingBuf);
                success = "Pinging " + targetNodeInfo.getIpAddr() + ":" + targetNodeInfo.getPort();
                Utils.sendPacket(ctx, msg, new InetSocketAddress(targetNodeInfo.getIpAddr(), targetNodeInfo.getPort()), messageType, success);
                break;
            case STORE:
                msg.writeInt(key.length());
                msg.writeCharSequence(key, StandardCharsets.UTF_8);
                ByteBuf valueBuf = Utils.serialize(value);
                msg.writeInt(valueBuf.readableBytes());
                msg.writeBytes(valueBuf);
                success = "Sent STORE request for key: " + key + ", value: " + value + " to node " + targetNodeInfo.getIpAddr() + ":" + targetNodeInfo.getPort();
                Utils.sendPacket(ctx, msg, new InetSocketAddress(targetNodeInfo.getIpAddr(), targetNodeInfo.getPort()), messageType, success);
                break;
            case NOTIFY:
                msg.writeInt(key.length());
                msg.writeCharSequence(key, StandardCharsets.UTF_8);
                success = "New block hash: " + key + "\nNotifying " + targetNodeInfo.getIpAddr() + ":" + targetNodeInfo.getPort();
                Utils.sendPacket(ctx, msg, new InetSocketAddress(targetNodeInfo.getIpAddr(), targetNodeInfo.getPort()), messageType, success);
                break;
            case LATEST_BLOCK:
                ByteBuf latestBlockBuf = Unpooled.wrappedBuffer("LATEST_BLOCK".getBytes());
                msg.writeInt(latestBlockBuf.readableBytes());
                msg.writeBytes(latestBlockBuf);
                success = "Sent LATEST_BLOCK request to node " + targetNodeInfo.getIpAddr() + ":" + targetNodeInfo.getPort();
                Utils.sendPacket(ctx, msg, new InetSocketAddress(targetNodeInfo.getIpAddr(), targetNodeInfo.getPort()), messageType, success);
                break;
            case NEW_AUCTION:
                msg.writeInt(key.length());
                msg.writeCharSequence(key, StandardCharsets.UTF_8);
                success = "Sent new auction ID " + key + " to node " + targetNodeInfo.getIpAddr() + ":" + targetNodeInfo.getPort();
                Utils.sendPacket(ctx, msg, new InetSocketAddress(targetNodeInfo.getIpAddr(), targetNodeInfo.getPort()), messageType, success);
                break;
            default:
                logger.warning("Received unknown message type: " + messageType);
                break;
        }
        startTimeoutTimer();
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
        cancelTimeoutTimer();
        if (msg instanceof DatagramPacket packet) {
            ByteBuf bytebuf = packet.content();
            messageType = Kademlia.MessageType.values()[bytebuf.readInt()];
            int randomIdLength = bytebuf.readInt();
            byte[] receivedId = new byte[randomIdLength];
            bytebuf.readBytes(receivedId);
            if (!Arrays.equals(randomId, receivedId)) {
                logger.warning("Received message with unexpected ID. Potential address forgery attack.");
            }
            switch (messageType) {
                case FIND_NODE, FIND_VALUE:
                    findNodeHandler(ctx,bytebuf);
                    break;
                case PING, STORE, NOTIFY, NEW_AUCTION:
                    ackHandler(ctx,bytebuf);
                    break;
                case LATEST_BLOCK:
                    latestBlockHandler(ctx,bytebuf);
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
     * @param ctx The channel handler context.
     * @param bytebuf The received ByteBuf.
     * @throws IOException            If an I/O error occurs.
     * @throws ClassNotFoundException If the class of the serialized object cannot be found.
     */
    private void findNodeHandler(ChannelHandlerContext ctx, ByteBuf bytebuf) throws IOException, ClassNotFoundException {
        int messageLength = bytebuf.readInt();
        ByteBuf messageBytes = bytebuf.readBytes(messageLength);
        Object deserializedObject = Utils.deserialize(messageBytes);

        if(messageType == Kademlia.MessageType.FIND_VALUE) {
            value = deserializedObject;
            logger.info("Received value: " + value + " from " + ctx.channel().remoteAddress());
            return;
        }

        if (deserializedObject instanceof ArrayList) {
            ArrayList<NodeInfo> nodeInfoList = (ArrayList<NodeInfo>) deserializedObject;
            logger.info("Received near nodes info from server: " + nodeInfoList);
            nearNodesInfo.addAll(nodeInfoList);
        } else {
            logger.warning("Received unknown message type from server: " + deserializedObject.getClass().getName());
        }
        messageBytes.release();
    }

    /**
     * Handles the response from the server for PING and STORE messages.
     *
     * @param ctx     The channel handler context.
     * @param bytebuf The received ByteBuf.
     */
    private void ackHandler(ChannelHandlerContext ctx, ByteBuf bytebuf) {
        int ackLength = bytebuf.readInt();
        ByteBuf ackBytes = bytebuf.readBytes(ackLength);
        String ack = ackBytes.toString(StandardCharsets.UTF_8);
        logger.info("Received " + ack + " from " + ctx.channel().remoteAddress());
        ackBytes.release();
    }

    /**
     * Handles the response from the server for LATEST_BLOCK messages.
     *
     * @param ctx     The channel handler context.
     * @param bytebuf The received ByteBuf.
     */
    private void latestBlockHandler(ChannelHandlerContext ctx, ByteBuf bytebuf) {
        int latestBlockHashLength = bytebuf.readInt();
        ByteBuf latestBlockBytes = bytebuf.readBytes(latestBlockHashLength);
        String latestBlockHash = latestBlockBytes.toString(StandardCharsets.UTF_8);
        logger.info("Received latest block hash " + latestBlockHash + " from " + ctx.channel().remoteAddress());
        Kademlia kademlia = Kademlia.getInstance();
        kademlia.setLatestBlockHash(latestBlockHash);
        latestBlockBytes.release();
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
     * Starts a timeout timer for RPC calls.
     */
    private void startTimeoutTimer() {
        timer = new Timer();
        long timeout = 5000;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                logger.warning("Server didn't respond to " + messageType + " RPC within " + timeout + " ms");
            }
        }, timeout);
    }

    /**
     * Cancels the timeout timer if it is active.
     */
    private void cancelTimeoutTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
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