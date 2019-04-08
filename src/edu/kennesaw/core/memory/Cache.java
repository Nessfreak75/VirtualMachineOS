package edu.kennesaw.core.memory;

import javax.swing.text.html.HTML;
import java.util.ArrayList;
import java.util.List;

public class Cache extends SimpleMemory implements CapacityMemory{
    private final int BLOCK_UNDEFINED = -1;
    //Stat fields
    private int hits;
    private int misses;
    //End stat fields

    private final CacheBlock[] _data;
    private int _used;

    public Cache(int capacity) throws MemoryInitializationException{
        super(capacity);
        _data = new CacheBlock[capacity];

        initBlocks();
    }

    @Override
    public int getUsedCapacity() {
        return _used;
    }

    public void cache(int address, int data){
        int blockIndex = _used == _data.length ? makeEmpty() : findEmpty();

        if(blockIndex >= 0){
            writeToBlock(blockIndex, address, data);
            _used++;
        }
    }

    public boolean write(int address, int data){
        int blockIndex = findIndex(address);

        if(blockIndex >= 0){
            _data[blockIndex].data = data;
            _data[blockIndex].usage++;
            _data[blockIndex].dirty = true;
            hits++;
        }
        misses++;
        return false;
    }

    public int read(int address){
        for(CacheBlock block : _data){
            if(block.tag == address){
                hits++;
                block.usage++;
                return block.data;
            }
        }
        //If nothing found mark as miss and return nothing.
        misses++;
        return -1;
    }
    public AddressDataPair[] getDirty(){
        List<AddressDataPair> dirtyBlocks = new ArrayList<>();

        for(CacheBlock block : _data){
            if(block.dirty){
                dirtyBlocks.add(new AddressDataPair(block.tag, block.data));
            }
        }

        return dirtyBlocks.toArray(new AddressDataPair[0]);
    }
    public void flush(){
        for(CacheBlock block : _data)
            block.clear();
        _used = 0;
    }
    public int flushAddress(int address){
        int index = findIndex(address);
        int data = -1;

        if(index >= 0){
            data = _data[index].data;
            _data[index].clear();
            _used--;
        }
        return data;
    }
    private int makeEmpty(){
        int leastUsed = _data[0].dirty ? -1 : 0;

        for(int i = 1; i < _data.length; i++){
            if(!_data[i].dirty && _data[i].usage < _data[i - 1].usage)
                leastUsed = i;
        }

        if(leastUsed >= 0) {
            _data[leastUsed].clear();
            _used--;
        }
        return leastUsed;
    }
    private int findEmpty(){
        return findIndex(BLOCK_UNDEFINED);
    }
    private int findIndex(int address){
        for(int i = 0; i < _data.length; i++){
            if(_data[i].tag == address)
                return i;
        }
        return -1;
    }
    private void writeToBlock(int blockIndex, int address, int data){
        if(blockIndex < _data.length) {
            _data[blockIndex].tag = address;
            _data[blockIndex].data = data;
        }
    }

    private void initBlocks(){
        for(int i = 0; i < _data.length; i++)
            _data[i] = new CacheBlock();
    }
    private final class CacheBlock {
        private int tag = BLOCK_UNDEFINED;
        private int data, usage;
        private boolean dirty;

        private void clear(){
            tag = BLOCK_UNDEFINED;
            data = 0;
            dirty = false;
            usage = 0;
        }
    }
    public final class AddressDataPair{
        private int address, data;

        public AddressDataPair(int address, int data){
            this.address = address;
            this.data = data;
        }

        public int getAddress(){return address;}
        public int getData(){return data;}
    }

}
