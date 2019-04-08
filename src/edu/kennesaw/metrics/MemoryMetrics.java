package edu.kennesaw.metrics;

import java.util.Map;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import edu.kennesaw.core.memory.CapacityMemory;

public final class MemoryMetrics {
    private final Map<CapacityMemory, MemoryInfo> _memories = new HashMap<>();
    private final Timer _timer = new Timer();

    public MemoryMetrics(){
        _timer.scheduleAtFixedRate(new MemoryInfoUpdateTask(), 0, 50);
    }

    public void register(CapacityMemory memory, String description){
        if(!isRegistered(memory))
            _memories.put(memory, new MemoryInfo(memory.getCapacity(), description));
    }

    public boolean isRegistered(CapacityMemory memory){
        return _memories.containsKey(memory);
    }

    public void printStats(){
        System.out.println("------------Memory metrics-------------");

        for(Map.Entry<CapacityMemory, MemoryInfo> memory : _memories.entrySet()){
            System.out.println("Memory name: " + memory.getValue().description);
            System.out.println(String.format("Capacity: %d bytes", memory.getValue().capacity));
            System.out.println(String.format("Maximum usage: %d bytes", memory.getValue().maxUsage));
            System.out.println(String.format("Average usage: %f bytes", memory.getValue().averageUsage));
            System.out.println(String.format("Average usage: %f%%", 100 * memory.getValue().averageUsage / memory.getValue().capacity));
        }
        System.out.println("------------------------------------");
    }

    public void shutdown(){
        _timer.cancel();
    }

    private MemoryInfo getInfo(CapacityMemory memory){
        return _memories.get(memory);
    }

    private final class MemoryInfoUpdateTask extends TimerTask{
        @Override
        public void run() {
            for(Map.Entry<CapacityMemory, MemoryInfo> memory : _memories.entrySet()){
                MemoryInfo mi = memory.getValue();
                int current = memory.getKey().getUsedCapacity();

                if(current > mi.maxUsage)
                    mi.maxUsage = current;

                mi.averageUsage = (mi.averageUsage + current) / 2;
            }
        }
    }
    private final class MemoryInfo{
        //Usage stats are in bytes
        public int maxUsage;
        public double averageUsage;
        public int capacity;
        public String description;

        public MemoryInfo(int capacity, String description){
            this.capacity = capacity;
            this.description = description;
        }
    }
}
