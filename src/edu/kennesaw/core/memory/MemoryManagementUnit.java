package edu.kennesaw.core.memory;

import edu.kennesaw.core.utils.BitUtils;
import edu.kennesaw.core.utils.Config;

import javax.swing.plaf.basic.BasicMenuItemUI;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MemoryManagementUnit{

    //Fields
    private final PagedMemory _memory;
    private final int _offsetBits;
    private volatile Object _syncLock = new Object();

    //locking variables
    private volatile Object _lock = null;
    //end locking variables

    public MemoryManagementUnit(PagedMemory memory){
        _memory = memory;
        _offsetBits = calculateOffsetBits();
    }
    /**
     * Writes a word at specified address.
     * @param address Virtual write address.
     * @param data Word to write.
     * @param pageTable Page table for the paged memory.
     * @throws MemoryOverflowException If memory is full.
     */
    public void write(int address, int data, int[] pageTable) throws MemoryOverflowException {
        int physicalAddress = getPhysicalAddress(address, pageTable);
        synchronized (_syncLock) {
            _memory.write(physicalAddress, BitUtils.getBytes(data));
        }
    }
    /**
     * Reads a word from specified address in a paged memory.
     * @param address Virtual address to read from.
     * @param pageTable Page table for the paged memory.
     * @return Word read.
     * @throws IllegalMemoryAccessException If address is not legal.
     */
    public int read(int address, int[] pageTable) throws IllegalMemoryAccessException {
        int physicalAddress = getPhysicalAddress(address, pageTable);
        byte[] bytes;
        //Lock the instance, read from memory, and unlock the instance for future use.
        synchronized (_syncLock) {
            bytes = _memory.read(physicalAddress, Config.WORD_SIZE);
        }
        return BitUtils.getInt(bytes);
    }

    public int getPageSize(){
        return _memory.getPageSize();
    }

    private int calculateOffsetBits(){
        int pageSizeTemp = _memory.getPageSize();
        int offsetSize = 0;

        while((pageSizeTemp >>= 1) != 0) offsetSize++;

        return offsetSize;
    }
    private int getOffset(int virtualAddress){
        return virtualAddress & ((1 << _offsetBits) - 1);
    }
    private int getPageIndex(int virtualAddress){
        return virtualAddress >> _offsetBits;
    }
    private int getPhysicalAddress(int address, int[] pageTable){
        int page = pageTable[getPageIndex(address)];
        int offset = getOffset(address);

        return (page << _offsetBits) | offset;

    }
}
