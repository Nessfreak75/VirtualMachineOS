package edu.kennesaw.core.utils;

import java.nio.ByteBuffer;

public abstract class BitUtils {
    public static  int getBits(int value, int numberOfBits){
        return getBits(value, numberOfBits, 0);
    }
    public static int getBits(int value, int numberOfBits, int  start){
        //Move the value into position requested
        int properValue = value >> start;
        //Make AND operand
        int andValue = (1 << numberOfBits) - 1;

        return properValue & andValue;
    }

    public static byte[] getBytes(int value){
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public static int getInt(byte b1, byte b2, byte b3, byte b4){
        return getInt(new byte[] { b1, b2, b3, b4 });
    }
    public static int getInt(byte[] bytes){
        return ByteBuffer.wrap(bytes).getInt();
    }
}
