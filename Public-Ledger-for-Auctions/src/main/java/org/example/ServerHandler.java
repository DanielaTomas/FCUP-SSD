package org.example;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
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
            NodeInfo nodeInfo = (NodeInfo) Utils.deserialize(bytebuf);
            logger.info("Received node info from client: " + nodeInfo);

            List<NodeInfo> nearNodes = dummyFindClosestNodes(nodeInfo);
            ByteBuf responseBuffer = Utils.serialize(nearNodes);
            ctx.writeAndFlush(responseBuffer);
            //TODO Process the received node info here
            /*
            MessageType messageType = MessageType.values()[bytebuf.readInt()];
            switch (messageType) {
                case FIND_NODE:
                    //responseBuffer.release();
                    break;
                case PING:
                    //TODO
                    break;
                default:
                    logger.warning("Received unknown message type: " + messageType);
                    break;
            }
            */
            ctx.close();
        } else {
            logger.warning("Received unknown message type from client: " + msg.getClass().getName());
        }
    }

    private List<NodeInfo> dummyFindClosestNodes(NodeInfo requestedNode) {
        List<NodeInfo> closestNodes = new ArrayList<>();
        closestNodes.add(new NodeInfo("192.168.0.1", 1234));
        closestNodes.add(new NodeInfo("192.168.0.2", 1235));

        return closestNodes;
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