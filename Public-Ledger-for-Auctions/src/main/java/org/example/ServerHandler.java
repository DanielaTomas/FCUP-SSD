package org.example;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.example.Kademlia.MessageType;

/** Class ServerHandler: Handles the client-side channel events */
class ServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = Logger.getLogger(ServerHandler.class.getName());

    private Node myNode;

    private static final int K = 1; //TODO mudar valor para >1

    /**
     * Constructor for ServerHandler.
     *
     * @param node The local node.
     */
    public ServerHandler(Node node) {
        this.myNode = node;
    }

    /**
     * Called when a new client connects to the server.
     *
     * @param ctx The channel handler context.
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("Client connected: " + ctx.channel().remoteAddress());
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
        if (msg instanceof ByteBuf bytebuf) {
            MessageType messageType = MessageType.values()[bytebuf.readInt()];
            switch (messageType) {
                case FIND_NODE:
                    findNodeHandler(ctx, bytebuf, messageType);
                    break;
                case PING:
                    pingHandler(ctx, bytebuf, messageType);
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
            ctx.close();
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
     * @throws IOException            If an I/O error occurs.
     * @throws ClassNotFoundException If the class of the serialized object cannot be found.
     */
    private void findNodeHandler(ChannelHandlerContext ctx, ByteBuf bytebuf, MessageType messageType) throws IOException, ClassNotFoundException {
        int nodeInfoLength = bytebuf.readInt();
        ByteBuf nodeInfoBytes = bytebuf.readBytes(nodeInfoLength);
        NodeInfo nodeInfo = (NodeInfo) Utils.deserialize(nodeInfoBytes);
        logger.info("Received node info from client: " + nodeInfo);

        myNode.updateRoutingTable(nodeInfo);
        List<NodeInfo> nearNodes = findClosestNodes(nodeInfo);

        ByteBuf responseMsg = ctx.alloc().buffer();
        responseMsg.writeInt(messageType.ordinal());
        ByteBuf responseBuf = Utils.serialize(nearNodes);
        responseMsg.writeInt(responseBuf.readableBytes());
        responseMsg.writeBytes(responseBuf);
        ctx.writeAndFlush(responseMsg);
        logger.info("Sent near nodes info to client " + nodeInfo.getIpAddr() + ":" + nodeInfo.getPort());

        nodeInfoBytes.release();
    }

    /**
     * Finds the closest nodes to the requested node information.
     *
     * @param requestedNodeInfo The requested node information.
     * @return List of closest nodes.
     */
    private List<NodeInfo> findClosestNodes(NodeInfo requestedNodeInfo) {
        List<NodeInfo> myRoutingTable = myNode.getRoutingTable();
        List<NodeInfo> nearNodes = new ArrayList<>();
        Map<NodeInfo, Integer> distanceMap = new HashMap<>();

        for (NodeInfo nodeInfo : myRoutingTable) {
            if(!nodeInfo.equals(requestedNodeInfo)) {
                int distance = calculateDistance(requestedNodeInfo.getNodeId(), nodeInfo.getNodeId());
                distanceMap.put(nodeInfo, distance);
            }
        }

        List<Map.Entry<NodeInfo, Integer>> sortedEntries = new ArrayList<>(distanceMap.entrySet());
        sortedEntries.sort(Map.Entry.comparingByValue());

        int k = Math.min(K, sortedEntries.size());
        for (int i = 0; i < k; i++) {
            nearNodes.add(sortedEntries.get(i).getKey());
        }

        return nearNodes;
    }

    /**
     * Calculates the distance between two node IDs.
     *
     * @param nodeId1 The first node ID.
     * @param nodeId2 The second node ID.
     * @return The distance between the node IDs.
     */
    private int calculateDistance(String nodeId1, String nodeId2) {
        BigInteger nodeId1BigInt = new BigInteger(nodeId1, 16);
        BigInteger nodeId2BigInt = new BigInteger(nodeId2, 16);
        BigInteger distance = nodeId1BigInt.xor(nodeId2BigInt);
        return distance.bitCount();
    }

    /**
     * Handles PING messages from the client.
     *
     * @param ctx          The channel handler context.
     * @param bytebuf      The received ByteBuf.
     * @param messageType  The type of the message.
     */
    private void pingHandler(ChannelHandlerContext ctx, ByteBuf bytebuf, MessageType messageType) {
        int pingLength = bytebuf.readInt();
        ByteBuf pingBytes = bytebuf.readBytes(pingLength);
        String pingStr = pingBytes.toString(StandardCharsets.UTF_8);
        logger.info("Received " + pingStr + " from client " + ctx.channel().remoteAddress());

        ByteBuf responseMsg = ctx.alloc().buffer();
        responseMsg.writeInt(messageType.ordinal());
        ByteBuf responseBuf = Unpooled.wrappedBuffer("PINGACK".getBytes());
        responseMsg.writeInt(responseBuf.readableBytes());
        responseMsg.writeBytes(responseBuf);
        ctx.writeAndFlush(responseMsg);
        logger.info("Responded to ping from client " + ctx.channel().remoteAddress());
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