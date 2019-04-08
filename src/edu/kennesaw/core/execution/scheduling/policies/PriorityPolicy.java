package edu.kennesaw.core.execution.scheduling.policies;

import edu.kennesaw.core.processes.ProcessControlBlock;

import java.util.Iterator;

public class PriorityPolicy implements SchedulingPolicy {
    @Override
    public ProcessControlBlock select(Iterable<ProcessControlBlock> processes) {
        Iterator<ProcessControlBlock> it = processes.iterator();

        ProcessControlBlock output = it.next();
        ProcessControlBlock nextPcb;

        while(it.hasNext()){
            nextPcb = it.next();
            if(nextPcb.priority < output.priority)
                output = nextPcb;
        }
        return output;
    }
}