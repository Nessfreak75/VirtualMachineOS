package edu.kennesaw.core.memory;

import java.util.Arrays;
import edu.kennesaw.core.utils.NumberUtils;

public class SimpleMemory implements Memory {
    //Fields
    private int _capacity;
    private final byte[] _data;

    //Getters & Setters
    public int getCapacity(){ return _capacity; }


    //Capacity is in bytes
    public SimpleMemory(int capacity) throws MemoryInitializationException{
        if(!NumberUtils.isPowerOfTwo(capacity))
            throw new MemoryInitializationException(String.format("Capacity must be a power of 2 (provided capacity: %d)", capacity));
        _data = new byte[capacity];
        _capacity = capacity;
    }

    @Override
    public void write(int address, byte[] data) throws MemoryOverflowException{
        if(data.length + address > _capacity)
            throw new MemoryOverflowException();

        System.arraycopy(data, 0, _data, address, data.length);
    }

    @Override
    public byte[] read(int address, int length) throws IllegalMemoryAccessException {
        if(length + address > _capacity)
            throw new IllegalMemoryAccessException(address);

        return Arrays.copyOfRange(_data, address, address + length);
    }
}
