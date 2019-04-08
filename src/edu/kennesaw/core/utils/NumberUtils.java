package edu.kennesaw.core.utils;

public abstract class NumberUtils {

    public static boolean isPowerOfTwo(int number){
        if(number <= 0) return false;

        return (number & (number - 1)) == 0;
    }

}
