package edu.kennesaw.core.execution.scheduling.policies;

import edu.kennesaw.core.processes.ProcessControlBlock;

public interface SchedulingPolicy {
    ProcessControlBlock select(Iterable<ProcessControlBlock> processes);
}
