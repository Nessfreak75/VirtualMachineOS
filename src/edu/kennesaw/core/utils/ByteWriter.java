package edu.kennesaw.core.utils;

import edu.kennesaw.core.memory.MemoryOverflowException;

public abstract class ByteWriter{
    public void write(int address, byte[] data, byte[] memory) throws MemoryOverflowException{
        if(data.length + address > memory.length)
            throw new MemoryOverflowException();

        System.arraycopy(data, 0, memory, address, data.length);
    }
}
