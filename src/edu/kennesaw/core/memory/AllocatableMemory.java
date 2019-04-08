package edu.kennesaw.core.memory;

public interface AllocatableMemory {
    int[] allocate(int size) throws MemoryAllocationException;
    void deallocate(int frame);

    int getPageSize();
}
