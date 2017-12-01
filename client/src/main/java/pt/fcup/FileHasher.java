package pt.fcup;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility for hashing files, used for chunk and file hash verification
 * TODO: Make local jar, duplicating work in the SeederServer
 */
public class FileHasher {
    private final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private final String HASHING_ALGORITHM = "SHA-256";

    /**
     *   Makes sure a file or chunk hash is correct
     */
    public boolean checkHash(String file, String hash) throws IOException {
        String realHash = hashFile(file);
        return realHash.equals(hash);

    }

    /**
     * Read file to byte array with buffer, hash, then convert to hex string
     * http://www.codejava.net/coding/how-to-calculate-md5-and-sha-hash-values-in-java
     * @param file
     * @return hex string hash
     */
    public String hashFile(String file) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance(HASHING_ALGORITHM);

            byte[] bytesBuffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(bytesBuffer)) > 0) {
                digest.update(bytesBuffer, 0, bytesRead);
            }

            byte[] hashedBytes = digest.digest();

            return bytesToHex(hashedBytes);

        } catch (NoSuchAlgorithmException e) {
            // Unreachable unless SHA-256 is removed
            return null;

        } catch (IOException e) {
            throw e;

        }
    }

    /**
     * Converts byte array to hex string
     * https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
     */
    private String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
