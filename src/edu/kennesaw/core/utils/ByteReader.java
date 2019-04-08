package edu.kennesaw.core.utils;

import edu.kennesaw.core.memory.MemoryOverflowException;

import java.util.Arrays;

public abstract class ByteReader {
    public byte[] read(int address, int length, byte[] memory) throws MemoryOverflowException{
        if(length + address > memory.length)
            throw new MemoryOverflowException();

        return Arrays.copyOfRange(memory, address, address + length);
    }
}
