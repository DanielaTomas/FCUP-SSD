package org.example;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

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
    @NotNull
    static String getHexString(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
