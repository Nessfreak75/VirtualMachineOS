package edu.kennesaw.core.execution.scheduling;

import edu.kennesaw.core.execution.CentralProcessingUnit.Cpu;
import edu.kennesaw.core.execution.CentralProcessingUnit.InvalidRegisterAccessException;
import edu.kennesaw.core.execution.Dispatcher;
import edu.kennesaw.core.execution.scheduling.policies.Scheduler;
import edu.kennesaw.core.memory.AllocatableMemory;
import edu.kennesaw.core.processes.ProcessControlBlock;
import edu.kennesaw.core.processes.ProcessQueue;
import edu.kennesaw.core.processes.ProcessState;
import edu.kennesaw.metrics.Metrics;

public class ShortTermScheduler implements Scheduler {

    //Fields
    private final ProcessQueue _readyQueue;
    private final Cpu[] _cpus;
    private final Dispatcher _dispatcher;
    private final AllocatableMemory _ram;

    public ShortTermScheduler(ProcessQueue readyQueue, Cpu[] cpus, AllocatableMemory ram){
        _dispatcher = new Dispatcher();
        _readyQueue = readyQueue;
        _cpus = cpus;
        _ram = ram;
    }

    @Override
    public void schedule() throws FailedToScheduleException {
        if(_readyQueue.isEmpty()) return;

        Cpu cpu = getAvailableCpu();

        if(cpu != null){
            ProcessControlBlock oldPcb;
            ProcessControlBlock newPcb = _readyQueue.getSchedulingPolicy().select(_readyQueue.getProcesses());
            _readyQueue.remove(newPcb);

            try{
                oldPcb = _dispatcher.switchContext(cpu, newPcb);
            }catch(InvalidRegisterAccessException ira){
                throw new FailedToScheduleException(ira);
            }

            if(oldPcb != null){
                if(oldPcb.state == ProcessState.TERMINATED){
                    destroyPcb(oldPcb);
                }else{
                    oldPcb.state = ProcessState.READY;
                    _readyQueue.add(oldPcb);
                }
            }

            //Metrics log
            Metrics.job().startedExecution(newPcb.jobId);
            newPcb.state = ProcessState.RUNNING;
        }
    }

    public void cleanUp(){
        for(int i = 0; i < _cpus.length; i++)
            destroyPcb(_cpus[i].getProcess());
    }

    private void destroyPcb(ProcessControlBlock pcb){
        Metrics.job().endedExecution(pcb.jobId);
        for(int page : pcb.pageTable)
            _ram.deallocate(page);
        Metrics.job().destroyed(pcb.jobId);
    }

    private Cpu getAvailableCpu(){
        //First try to find cpu that is not busy
        for(Cpu cpu : _cpus){
            if(cpu.isIdle())
                return cpu;
        }

        return null;
    }

}
