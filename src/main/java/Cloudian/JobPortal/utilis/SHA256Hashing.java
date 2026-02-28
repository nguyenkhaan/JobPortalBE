//Source Code: https://freedium-mirror.cfd/https://www.google.com/url?sa=t&source=web&rct=j&opi=89978449&url=https://medium.com/%40AlexanderObregon/what-is-sha-256-hashing-in-java-0d46dfb83888&ved=2ahUKEwjUwbDC3_2SAxX6rlYBHRLSI6sQFnoECDEQAQ&usg=AOvVaw2TwDYHZ6pVAm91UhM4miJC

package Cloudian.JobPortal.utilis;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256Hashing {
    public static String generateSHA256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }
    public static boolean verifyDataIntegrity(String originalData, String receivedData) {
        String originalHash = SHA256Hashing.generateSHA256Hash(originalData);
        String receivedHash = SHA256Hashing.generateSHA256Hash(receivedData);
        return originalHash.equals(receivedHash);
    }
}
