package be.yvanmazy.remotedminecraft.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashUtil {

    private HashUtil() throws IllegalAccessException {
        throw new IllegalAccessException("You cannot instantiate a utility class");
    }

    public static @NotNull String hash(final @NotNull InputStream stream) throws IOException {
        final MessageDigest hash;
        try {
            hash = MessageDigest.getInstance("SHA-1");
        } catch (final NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
        final byte[] buf = new byte[1024];
        int count;
        try (stream) {
            while ((count = stream.read(buf)) != -1) hash.update(buf, 0, count);
        }

        final byte[] bytes = hash.digest();
        final StringBuilder builder = new StringBuilder();
        for (final byte b : bytes) {
            builder.append(String.format("%02x", b));
        }

        return builder.toString();
    }

}