package edu.kennesaw.core.processes;

import edu.kennesaw.core.execution.scheduling.policies.SchedulingPolicy;

import java.util.ArrayList;
import java.util.List;

public class ProcessQueue {
    private final SchedulingPolicy _schedulingPolicy;
    private final List<ProcessControlBlock> _pcbList;

    //Getters & Setters
    public SchedulingPolicy getSchedulingPolicy(){ return _schedulingPolicy; }
    public List<ProcessControlBlock> getProcesses(){ return _pcbList; }

    public ProcessQueue(SchedulingPolicy schedulingPolicy){
        _schedulingPolicy = schedulingPolicy;
        _pcbList = new ArrayList<>();
    }

    public void add(ProcessControlBlock pcb){
        _pcbList.add(pcb);
    }
    public int count() { return _pcbList.size(); }
    public void remove(ProcessControlBlock pcb){
        _pcbList.remove(pcb);
    }
    public boolean isEmpty(){ return _pcbList.isEmpty(); }
}
