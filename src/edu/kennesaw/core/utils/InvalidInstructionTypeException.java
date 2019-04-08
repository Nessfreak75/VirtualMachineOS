package edu.kennesaw.core.utils;

public class InvalidInstructionTypeException extends Exception{
    public InvalidInstructionTypeException(int type){
        super(String.format("Invalid instruction type '%d'.", type));
    }
}
