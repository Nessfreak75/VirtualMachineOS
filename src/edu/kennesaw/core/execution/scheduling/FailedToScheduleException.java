package edu.kennesaw.core.execution.scheduling;

public class FailedToScheduleException extends Exception {
    public FailedToScheduleException(Exception innerException){
        super("Job failed to schedule.", innerException);
    }
}
