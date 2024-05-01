package BlockChain;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Class BlockChainUtils */
public class BlockChainUtils {

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
     * Converts a Unix timestamp to a formatted date and time string.
     *
     * @param time The Unix timestamp (in milliseconds).
     * @return The formatted date and time string ("yyyy/MM/dd HH:mm:ss").
     */
    static public String convertTime(long time){
        Date date = new Date(time);
        Format format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return format.format(date);
    }
}
