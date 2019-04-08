package edu.kennesaw.core.execution.scheduling.policies;

import edu.kennesaw.core.execution.scheduling.FailedToScheduleException;

public interface Scheduler {
    void schedule() throws FailedToScheduleException;
}
