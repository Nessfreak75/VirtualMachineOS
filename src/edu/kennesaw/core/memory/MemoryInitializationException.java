package edu.kennesaw.core.memory;

public class MemoryInitializationException extends Exception{
    private static String _defaultMessage = "Memory failed to initialize.";
    public MemoryInitializationException(){
        super(_defaultMessage);
    }
    public MemoryInitializationException(String additionalInfo){
        super(String.format("%s (%s)", _defaultMessage, additionalInfo));
    }
}
