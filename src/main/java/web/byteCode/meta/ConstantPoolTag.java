package web.byteCode.meta;

/**
 * Created by xiang.xu on 2015/1/4.
 */
public class ConstantPoolTag {
    /**
     * The type of CONSTANT_Class constant pool items.
     */
    public static final int CLASS = 7;

    /**
     * The type of CONSTANT_Fieldref constant pool items.
     */
    public static final int FIELD = 9;

    /**
     * The type of CONSTANT_Methodref constant pool items.
     */
    public static final int METH = 10;

    /**
     * The type of CONSTANT_InterfaceMethodref constant pool items.
     */
    public static final int IMETH = 11;

    /**
     * The type of CONSTANT_String constant pool items.
     */
    public static final int STR = 8;

    /**
     * The type of CONSTANT_Integer constant pool items.
     */
    public static final int INT = 3;

    /**
     * The type of CONSTANT_Float constant pool items.
     */
    public static final int FLOAT = 4;

    /**
     * The type of CONSTANT_Long constant pool items.
     */
    public static final int LONG = 5;

    /**
     * The type of CONSTANT_Double constant pool items.
     */
    public static final int DOUBLE = 6;

    /**
     * The type of CONSTANT_NameAndType constant pool items.
     */
    public static final int NAME_TYPE = 12;

    /**
     * The type of CONSTANT_Utf8 constant pool items.
     */
    public static final int UTF8 = 1;

    /**
     * The type of CONSTANT_MethodType constant pool items.
     */
    public static final int MTYPE = 16;

    /**
     * The type of CONSTANT_MethodHandle constant pool items.
     */
    public static final int HANDLE = 15;

    /**
     * The type of CONSTANT_InvokeDynamic constant pool items.
     */
    public static final int INDY = 18;
}
