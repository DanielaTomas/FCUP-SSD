package org.example;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Class Utils */
public class Utils {

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
     * @param requestedNodeInfo The requested node information.
     * @return List of closest nodes.
     */
    public static List<NodeInfo> findClosestNodes(List<NodeInfo> myRoutingTable, NodeInfo requestedNodeInfo, final int K) {
        List<NodeInfo> nearNodes = new ArrayList<>();
        Map<NodeInfo, Integer> distanceMap = new HashMap<>();

        for (NodeInfo nodeInfo : myRoutingTable) {
            if(!nodeInfo.equals(requestedNodeInfo)) {
                int distance = Utils.calculateDistance(requestedNodeInfo.getNodeId(), nodeInfo.getNodeId());
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
}
