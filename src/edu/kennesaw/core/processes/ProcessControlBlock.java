package edu.kennesaw.core.processes;

import edu.kennesaw.core.utils.Config;

public class ProcessControlBlock{
    //Job identifiers
    public int jobId;
    public int textSize;
    public int priority;
    public ProcessType type;
    public ProcessState state;
    //Memory control
    public int diskAddress;
    public int inputBufferSize;
    public int outputBufferSize;
    public int temporaryBufferSize;
    public int[] pageTable;
    //Job control
    public int programCounter;
    //Cpu properties
    public int[] registers = new int[Config.REGISTER_COUNT];
}