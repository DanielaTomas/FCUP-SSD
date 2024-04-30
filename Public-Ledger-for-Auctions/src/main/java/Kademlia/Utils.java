package Kademlia;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/** Class Utils */
public class Utils {

    private static final Logger logger = Logger.getLogger(Utils.class.getName());

    /**
     * Retrieves the public IP address of the local machine.
     *
     * @return The public IP address of the local machine.
     * @throws RuntimeException if an error occurs during the retrieval process.
     */
    public static String getAddress() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new URL("https://checkip.amazonaws.com/").openStream()));
            return br.readLine();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts a byte array to a hexadecimal string representation.
     *
     * @param hash The byte array to be converted.
     * @return The hexadecimal string representation of the byte array.
     */
    static String getHexString(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Serialize an object into a ByteBuf.
     *
     * @param obj The object to serialize.
     * @return ByteBuf containing serialized data.
     * @throws IOException If an I/O error occurs during serialization.
     */
    public static ByteBuf serialize(Object obj) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(obj);
        objectOutputStream.flush();
        objectOutputStream.close();
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Unpooled.wrappedBuffer(bytes);
    }

    /**
     * Deserialize data from a ByteBuf into an object.
     *
     * @param byteBuf The ByteBuf containing serialized data.
     * @return The deserialized object.
     * @throws IOException            If an I/O error occurs during deserialization.
     * @throws ClassNotFoundException If the class of the serialized object cannot be found.
     */
    public static Object deserialize(ByteBuf byteBuf) throws IOException, ClassNotFoundException {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return objectInputStream.readObject();
    }

    /**
     * Finds the closest nodes to the requested node information.
     *
     * @param requestedNodeId The requested node id.
     * @return List of closest nodes.
     */
    public static List<NodeInfo> findClosestNodes(List<NodeInfo> myRoutingTable, String requestedNodeId, final int K) {
        List<NodeInfo> nearNodes = new ArrayList<>();
        Map<NodeInfo, Integer> distanceMap = new HashMap<>();

        for (NodeInfo nodeInfo : myRoutingTable) {
            if(!nodeInfo.getNodeId().equals(requestedNodeId)) {
                int distance = Utils.calculateDistance(requestedNodeId, nodeInfo.getNodeId());
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
    public static int calculateDistance(String nodeId1, String nodeId2) {
        BigInteger nodeId1BigInt = new BigInteger(nodeId1, 16);
        BigInteger nodeId2BigInt = new BigInteger(nodeId2, 16);
        BigInteger distance = nodeId1BigInt.xor(nodeId2BigInt);
        return distance.bitCount();
    }

    /**
     * Sends a packet.
     *
     * @param ctx The ChannelHandlerContext for sending the packet.
     * @param msg The ByteBuf containing the packet data to send.
     * @param sender The InetSocketAddress of the packet sender.
     * @param messageType The type of the message being sent.
     * @param success The success message to log upon successful packet send.
     */
    public static void sendPacket(ChannelHandlerContext ctx, ByteBuf msg, InetSocketAddress sender, Kademlia.MessageType messageType, String success) {
        ctx.writeAndFlush(new DatagramPacket(msg, sender)).addListener(future -> {
            if (!future.isSuccess()) {
                ChannelFuture closeFuture = ctx.channel().close();
                if (closeFuture.isSuccess()) {
                    System.err.println(messageType + " failed, Channel close successfully");
                } else {
                    System.err.println(messageType + " failed, Channel close failed " + closeFuture.cause());
                }
            } else {
                logger.info(success);
            }
        });
    }
}
