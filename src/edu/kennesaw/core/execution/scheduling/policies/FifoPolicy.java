package edu.kennesaw.core.execution.scheduling.policies;

import edu.kennesaw.core.processes.ProcessControlBlock;

public class FifoPolicy implements SchedulingPolicy{

    @Override
    public ProcessControlBlock select(Iterable<ProcessControlBlock> processes) {
        return processes.iterator().next();
    }
}
