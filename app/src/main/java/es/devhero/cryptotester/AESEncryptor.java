package es.devhero.cryptotester;

/**
 * Created by krystian on 17/03/17.
 */

import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.io.CipherInputStream;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.Security;
import java.util.Arrays;

/**
 * Handles the security aspects of the app such as encryption and offline storage
 * Reference takens from http://android-developers.blogspot.co.uk/2013/02/using-cryptography-to-store-credentials.html
 *
 * @author Luke
 */
public class AESEncryptor {
    private static final String TAG = "SecurityModule";

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    /**
     * Encrypt the given plaintext bytes using the given key
     *
     * @param data The plaintext to encrypt
     * @return The encrypted bytes
     */
    public static byte[] encrypt(byte[] data, byte[] key, byte[] iv) {

        if (key == null || iv == null) {
            throw new AssertionError("ENCRYPT: Key or iv were not specified.");
        }

        // make sure key is AES256
        byte[] bookKeyData = new byte[32];
        System.arraycopy(key, 0, bookKeyData, 0, key.length);

        try {
            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
            cipher.init(true, new ParametersWithIV(new KeyParameter(bookKeyData), iv));

            byte[] outBuf = new byte[cipher.getOutputSize(data.length)];

            int processed = cipher.processBytes(data, 0, data.length, outBuf, 0);
            processed += cipher.doFinal(outBuf, processed);

            return Arrays.copyOfRange(outBuf, 0, processed);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decrypt the given data with the given key
     *
     * @param inputStream input stream to decrypt
     * @param inputSize size of stream
     * @return The decrypted bytes
     */
    public static byte[] decrypt(InputStream inputStream, int inputSize, byte[] key, byte[] iv) {
        if (key == null || iv == null) {
            throw new AssertionError("DECRYPT: Key or iv were not specified.");
        }

        CipherInputStream cipherStream;
        FileOutputStream dstStream;
        ByteArrayOutputStream resultStream;

        // make sure key is AES256
        byte[] bookKeyData = new byte[32];
        byte[] outBuf;
        System.arraycopy(key, 0, bookKeyData, 0, key.length);

        try {
            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
            cipher.init(false, new ParametersWithIV(new KeyParameter(bookKeyData), iv));

            int outputSize = cipher.getOutputSize(inputSize);
            resultStream = new ByteArrayOutputStream(outputSize);
            cipherStream = new CipherInputStream(inputStream, cipher);

            int len;
            byte[] buffer = new byte[128];
            while ((len = cipherStream.read(buffer)) != -1) {
                resultStream.write(buffer, 0, len);
            }

            outBuf = resultStream.toByteArray();
            resultStream.close();

            return outBuf;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decrypt the given data with the given key
     *
     * @param data The data to decrypt
     * @return The decrypted bytes
     */
    public static byte[] decrypt(byte[] data, byte[] key, byte[] iv) {
        if (key == null || iv == null) {
            throw new AssertionError("DECRYPT: Key or iv were not specified.");
        }

        // make sure key is AES256
        byte[] bookKeyData = new byte[32];
        byte[] outBuf;
        System.arraycopy(key, 0, bookKeyData, 0, key.length);

        try {
            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
            cipher.init(false, new ParametersWithIV(new KeyParameter(bookKeyData), iv));
            int outputSize = cipher.getOutputSize(data.length);
            outBuf = new byte[cipher.getOutputSize(outputSize)];
            int processed = cipher.processBytes(data, 0, data.length, outBuf, 0);
            if (processed < outputSize) {
                processed += cipher.doFinal(outBuf, processed);
            }
            return Arrays.copyOfRange(outBuf, 0, processed);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
