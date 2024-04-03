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

import static org.example.Kademlia.MessageType;

/** Class ServerHandler: Handles the client-side channel events */
class ServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = Logger.getLogger(ServerHandler.class.getName());

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

    private void findNodeHandler(ChannelHandlerContext ctx, ByteBuf bytebuf, MessageType messageType) throws IOException, ClassNotFoundException {
        int nodeInfoLength = bytebuf.readInt();
        ByteBuf nodeInfoBytes = bytebuf.readBytes(nodeInfoLength);
        NodeInfo nodeInfo = (NodeInfo) Utils.deserialize(nodeInfoBytes);
        logger.info("Received node info from client: " + nodeInfo);

        List<NodeInfo> nearNodes = dummyFindClosestNodes(nodeInfo);

        ByteBuf responseMsg = ctx.alloc().buffer();
        responseMsg.writeInt(messageType.ordinal());
        ByteBuf responseBuf = Utils.serialize(nearNodes);
        responseMsg.writeInt(responseBuf.readableBytes());
        responseMsg.writeBytes(responseBuf);
        ctx.writeAndFlush(responseMsg);
        logger.info("Sent near nodes info to client " + nodeInfo.getIpAddr() + ":" + nodeInfo.getPort());

        nodeInfoBytes.release();
    }

    private List<NodeInfo> dummyFindClosestNodes(NodeInfo requestedNode) {
        List<NodeInfo> closestNodes = new ArrayList<>();
        closestNodes.add(new NodeInfo("192.168.0.1", 1234));
        closestNodes.add(new NodeInfo("192.168.0.2", 1235));

        return closestNodes;
    }

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