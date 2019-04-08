package edu.kennesaw.core.converters;

public abstract class Hexadecimal{
    public static int toInt(String hex) throws NumberFormatException{
        hex = stripFormatter(hex);
        return Long.valueOf(hex, 16).intValue();
    }

    private static String stripFormatter(String value){
        return value.replace("0x", "");
    }
}
