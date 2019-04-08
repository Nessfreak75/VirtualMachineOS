package edu.kennesaw.core.memory;

public class MemoryOverflowException extends Exception {
    public MemoryOverflowException(){
        super("Memory access was out of bounds.");
    }
}
