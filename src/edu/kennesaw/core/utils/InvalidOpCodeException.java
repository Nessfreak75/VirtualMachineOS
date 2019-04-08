package edu.kennesaw.core.utils;

public class InvalidOpCodeException extends Exception {
    public InvalidOpCodeException(int opCode){
        super(String.format("Unsupported opcode '%d'", opCode));
    }
}
