/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package web.byteCode;

import com.google.common.base.Splitter;
import web.byteCode.meta.ConstantPoolTag;

import java.io.IOException;
import java.io.InputStream;

/**
 * A Tiny Java class parser to read an existing java class file. This class parses
 * a byte array conforming to the Java class file format and find the target annotation.
 * This parser just find the annotation and ignore another.
 *
 * Created by xiang.xu on 2015/1/4.
 */
public class ClassReader {

    /**
     * The class to be parsed. <i>The content of this array must not be
     * modified. This field is intended for Attribute sub classes, and
     * is normally not needed by class generators or adapters.</i>
     */
    private final byte[] src;

    /**
     * The start index of each constant pool item in {@link #src src}, plus one. The
     * one byte offset skips the constant pool item tag that indicates its type.
     */
    private final int[] items;

    /**
     * The String objects corresponding to the CONSTANT_Utf8 items. This cache
     * avoids multiple parsing of a given CONSTANT_Utf8 constant pool item,
     * which GREATLY improves performances (by a factor 2 to 3). This caching
     * strategy could be extended to all constant pool items, but its benefit
     * would not be so great for these items (because they are much less
     * expensive to parse than CONSTANT_Utf8 items).
     */
    private final String[] strings;

    /**
     * Maximum length of the strings contained in the constant pool of the
     * class.
     */
    private final int maxStringLength;

    /**
     * Start index of the class header information (access, name...) in
     * {@link #src src}.
     */
    public final int header;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a new {@link ClassReader} object.
     *
     * @param src the byte code of the class to be read.
     */
    public ClassReader(final byte[] src) {
        this(src, 0, src.length);
    }

    /**
     * Constructs a new {@link ClassReader} object.
     *
     * @param is an input stream from which to read the class.
     * @throws java.io.IOException if a problem occurs during reading.
     */
    public ClassReader(final InputStream is) throws IOException {
        this(ReaderUtil.readClass(is, false));
    }

    /**
     * Constructs a new {@link ClassReader} object.
     *
     * @param src the bytecode of the class to be read.
     * @param off the start offset of the class data.
     * @param len the length of the class data.
     */
    public ClassReader(final byte[] src, final int off, final int len) {
        this.src = src;
        // checks the class version
        /* SPRING PATCH: REMOVED FOR FORWARD COMPATIBILITY WITH JDK 9
        if (readShort(off + 6) > Opcodes.V1_8) {
            throw new IllegalArgumentException();
        }
         */
        // parses the constant pool
        this.items = new int[ReaderUtil.readUnsignedShort(off + 8, this.src)];
        final int n = this.items.length;
        this.strings = new String[n];
        int max = 0;
        int index = off + 10;
        for (int i = 1; i < n; ++i) {
            this.items[i] = index + 1;
            int size;
            switch (src[index]) {
                case ConstantPoolTag.FIELD:
                case ConstantPoolTag.METH:
                case ConstantPoolTag.IMETH:
                case ConstantPoolTag.INT:
                case ConstantPoolTag.FLOAT:
                case ConstantPoolTag.NAME_TYPE:
                case ConstantPoolTag.INDY:
                    size = 5;
                    break;
                case ConstantPoolTag.LONG:
                case ConstantPoolTag.DOUBLE:
                    size = 9;
                    ++i;
                    break;
                case ConstantPoolTag.UTF8:
                    size = 3 + ReaderUtil.readUnsignedShort(index + 1, this.src);
                    if (size > max) {
                        max = size;
                    }
                    break;
                case ConstantPoolTag.HANDLE:
                    size = 4;
                    break;
                // case ConstantPoolTag.CLASS:
                // case ConstantPoolTag.STR:
                // case ConstantPoolTag.MTYPE
                default:
                    size = 3;
                    break;
            }
            index += size;
        }
        this.maxStringLength = max;
        // the class header information starts just after the constant pool
        this.header = index;
    }

    // ------------------------------------------------------------------------
    // End of constructors
    // ------------------------------------------------------------------------

    /**
     * Find the annotation in the class.
     *
     * @param targetAnnotationClass the target annotation.
     * @return found the target annotation for <tt>true</tt>, or for <tt>false</tt>.
     */
    public boolean containsAnnotation(final Class<?> targetAnnotationClass) {
        int idx = this.getAttributes(); // current offset in the class file
        final char[] buf = new char[this.maxStringLength]; // buffer used to read strings

        for (int i = ReaderUtil.readUnsignedShort(idx, this.src); i > 0 ; i-- ) {
            final String attrName = this.readUTF8(idx + 2, buf);
            if ("RuntimeVisibleAnnotations".equals(attrName)) {
                return this.findTargetInAnnotation(targetAnnotationClass, idx+8, buf);
            }
            idx += 6 + ReaderUtil.readInt(idx + 4, this.src);
        }
        return  false;
    }

    /**
     * Iterater the annotation and find the target annotation in the class.
     *
     * @param targetAnnotationClass the target annotation.
     * @param idx the offset of the byte array.
     * @param buf buf buffer to be used to call {@link #readUTF8 readUTF8}.
     * @return found the target annotation for <tt>true</tt>, or for <tt>false</tt>.
     */
    private boolean findTargetInAnnotation(final Class<?> targetAnnotationClass, final int idx, final char[] buf) {
        final String targetAnnotation = this.conventNameToByteCodeStyle(targetAnnotationClass.getName());
        for (int i = ReaderUtil.readUnsignedShort(idx, this.src), v = idx + 2; i > 0 ; i--) {
            final String annotation = this.readUTF8(v, buf);
            if (annotation.equals(targetAnnotation)) {
                return true;
            }
            else {
                v = skipElement(v + 2, buf);
            }
        }
        return false;
    }

    /**
     * Convent the class name to byte code style.
     *
     * @param name the class full name.
     * @return the byte code style.
     */
    private String conventNameToByteCodeStyle(final String name) {
        StringBuffer buffer = new StringBuffer("L");
        for (String s : Splitter.on('.').trimResults().omitEmptyStrings().split(name)) {
            buffer.append(s).append('/');
        }
        int len = buffer.length();
        return buffer.replace(len-1, len, ";").toString();
    }

    /**
     * To skip the element_value_pairs
     *
     * @param idx the offset of the byte array.
     * @param buf buf buffer to be used to call {@link #readUTF8 readUTF8}.
     * @return the end offset of the annotation values.
     */
    private int skipElement(int idx, final char[] buf) {
        int k = ReaderUtil.readUnsignedShort(idx, this.src);
        idx += 2;
        for (int i = 0 ; i < k ; i++ ) {
            idx = skipAnnotationValue(idx + 2, buf); // skip the [u2 element_name_index]
        }
        return idx;
    }

    /**
     * Skip a value of an annotation and ignore that byte.
     *
     * @param idx the start offset in {@link #src src} of the value to be read (<i>not
     *        including the value name constant pool index</i>).
     * @param buf buffer to be used to call {@link #readUTF8 readUTF8}.
     * @return the end offset of the annotation value.
     */
    private int skipAnnotationValue(int idx, final char[] buf) {
        switch (this.src[idx++] & 0xFF) {
            case 'I': // pointer to CONSTANT_Integer
            case 'J': // pointer to CONSTANT_Long
            case 'F': // pointer to CONSTANT_Float
            case 'D': // pointer to CONSTANT_Double
            case 'B': // pointer to CONSTANT_Byte
            case 'Z': // pointer to CONSTANT_Boolean
            case 'S': // pointer to CONSTANT_Short
            case 'C': // pointer to CONSTANT_Char
            case 's': // pointer to CONSTANT_Utf8
            case 'c': // class_info
                idx += 2;
                break;
            case 'e': // enum_const_value
                idx += 4;
                break;
            case '@': // annotation_value
                skipElement(idx + 2, buf);
                break;
            case '[': // array_value
                final int size = ReaderUtil.readUnsignedShort(idx, this.src);
                idx += 2;
                if (0 == size) {
                    return this.skipElement(idx - 2, buf);
                }
                switch (this.src[idx++] & 0xFF) {
                    case 'B':
                    case 'Z':
                    case 'S':
                    case 'C':
                    case 'I':
                    case 'J':
                    case 'F':
                    case 'D':
                        idx += 3*size -1;
                        break;
                    default:
                        idx = this.skipElement(idx - 3, buf);
                }
                break;
            default:
                break;
        }
        return idx;
    }

    /**
     * Returns the start index of the attribute_info structure of this class.
     *
     * @return the start index of the attribute_info structure of this class.
     */
    private int getAttributes() {
        // skips the header
        int idx = this.header + 8 + ReaderUtil.readUnsignedShort(this.header + 6, this.src) * 2;
        // skips fields and methods
        for (int i = ReaderUtil.readUnsignedShort(idx, this.src); i > 0; --i) {
            for (int j = ReaderUtil.readUnsignedShort(idx + 8, this.src); j > 0; --j) {
                idx += 6 + ReaderUtil.readInt(idx + 12, this.src);
            }
            idx += 8;
        }
        idx += 2;
        for (int i = ReaderUtil.readUnsignedShort(idx, this.src); i > 0; --i) {
            for (int j = ReaderUtil.readUnsignedShort(idx + 8, this.src); j > 0; --j) {
                idx += 6 + ReaderUtil.readInt(idx + 12, this.src);
            }
            idx += 8;
        }
        // the attribute_info structure starts just after the methods
        return idx + 2;
    }

    /**
     * Reads an UTF8 string constant pool item in {@link #src src}. <i>This method
     * is intended for Attribute sub classes, and is normally not needed
     * by class generators or adapters.</i>
     *
     * @param index the start index of an unsigned short value in {@link #src src},
     *        whose value is the index of an UTF8 constant pool item.
     * @param buf buffer to be used to read the item. This buffer must be
     *        sufficiently large. It is not automatically resized.
     * @return the String corresponding to the specified UTF8 item.
     */
    public String readUTF8(int index, final char[] buf) {
        final int item = ReaderUtil.readUnsignedShort(index, this.src);
        if (index == 0 || item == 0) {
            return null;
        }
        final String s = this.strings[item];
        if (s != null) {
            return s;
        }
        index = this.items[item];
        return this.strings[item] = ReaderUtil.readUTF(index + 2, ReaderUtil.readUnsignedShort(index, this.src), buf, this.src);
    }
}
