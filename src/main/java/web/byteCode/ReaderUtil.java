package web.byteCode;

import java.io.IOException;
import java.io.InputStream;

/**
 * The utils for {@link web.byteCode.ClassReader}.
 *
 * Created by xiang.xu on 2015/1/4.
 */
public class ReaderUtil {

    /**
     * Reads the bytecode of a class.
     *
     * @param is an input stream from which to read the class.
     * @param close true to close the input stream after reading.
     * @return the bytecode read from the given input stream.
     * @throws java.io.IOException if a problem occurs during reading.
     */
    public static byte[] readClass(final InputStream is, final boolean close) throws IOException {
        if (is == null) {
            throw new IOException("Class not found");
        }
        try {
            byte[] src = new byte[is.available()];
            int len = 0;
            while (true) {
                final int n = is.read(src, len, src.length - len);
                if (n == -1) {
                    if (len < src.length) {
                        final byte[] c = new byte[len];
                        System.arraycopy(src, 0, c, 0, len);
                        src = c;
                    }
                    return src;
                }
                len += n;
                if (len == src.length) {
                    final int last = is.read();
                    if (last < 0) {
                        return src;
                    }
                    final byte[] c = new byte[src.length + 1000];
                    System.arraycopy(src, 0, c, 0, len);
                    c[len++] = (byte)last;
                    src = c;
                }
            }
        } finally {
            if (close) {
                is.close();
            }
        }
    }

    /**
     * Reads an unsigned short value in src. <i>This method is intended
     * for Attribute sub classes, and is normally not needed by class
     * generators or adapters.</i>
     *
     * @param index the start index of the value to be read in src.
     * @param src the byte array read from the class.
     * @return the read value.
     */
    public static int readUnsignedShort(final int index, final byte src[]) {
        return ((src[index] & 0xFF) << 8) | (src[index + 1] & 0xFF);
    }

    /**
     * Reads a signed short value in src. <i>This method is intended
     * for Attribute sub classes, and is normally not needed by class
     * generators or adapters.</i>
     *
     * @param index the start index of the value to be read in src.
     * @param src the byte array read from the class.
     * @return the read value.
     */
    public static short readShort(final int index, final byte src[]) {
        return (short)(((src[index] & 0xFF) << 8) | (src[index + 1] & 0xFF));
    }

    /**
     * Reads a signed int value in src. <i>This method is intended for
     * Attribute sub classes, and is normally not needed by class
     * generators or adapters.</i>
     *
     * @param index the start index of the value to be read in src.
     * @param src the byte array read from the class.
     * @return the read value.
     */
    public static int readInt(final int index, final byte src[]) {
        return ((src[index] & 0xFF) << 24) | ((src[index + 1] & 0xFF) << 16)
                | ((src[index + 2] & 0xFF) << 8) | (src[index + 3] & 0xFF);
    }

    /**
     * Reads UTF8 string in src.
     *
     * @param index start offset of the UTF8 string to be read.
     * @param utfLen length of the UTF8 string to be read.
     * @param buf buffer to be used to read the string. This buffer must be
     *        sufficiently large. It is not automatically resized.
     * @param src the byte array read from the class.
     * @return the String corresponding to the specified UTF8 string.
     */
    public static String readUTF(int index, final int utfLen, final char[] buf, final byte src[]) {
        final int endIndex = index + utfLen;
        int strLen = 0;
        int c;
        int st = 0;
        char cc = 0;
        while (index < endIndex) {
            c = src[index++];
            switch (st) {
                case 0:
                    c = c & 0xFF;
                    if (c < 0x80) { // 0xxxxxxx
                        buf[strLen++] = (char)c;
                    } else if (c < 0xE0 && c > 0xBF) { // 110x xxxx 10xx xxxx
                        cc = (char)(c & 0x1F);
                        st = 1;
                    } else { // 1110 xxxx 10xx xxxx 10xx xxxx
                        cc = (char)(c & 0x0F);
                        st = 2;
                    }
                    break;

                case 1: // byte 2 of 2-byte char or byte 3 of 3-byte char
                    buf[strLen++] = (char)((cc << 6) | (c & 0x3F));
                    st = 0;
                    break;

                case 2: // byte 2 of 3-byte char
                    cc = (char)((cc << 6) | (c & 0x3F));
                    st = 1;
                    break;
            }
        }
        return new String(buf, 0, strLen);
    }
}
