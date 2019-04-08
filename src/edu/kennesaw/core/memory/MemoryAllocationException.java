package edu.kennesaw.core.memory;

public class MemoryAllocationException extends Exception {
    public MemoryAllocationException(int size){
        super(String.format("Could not allocate %d bytes in memory because it is full.", size));
    }
}
