package edu.kennesaw.core.execution.scheduling;

import edu.kennesaw.core.execution.scheduling.policies.Scheduler;
import edu.kennesaw.core.memory.Memory;
import edu.kennesaw.core.memory.MemoryOverflowException;
import edu.kennesaw.core.memory.PagedMemory;
import edu.kennesaw.core.processes.ProcessControlBlock;
import edu.kennesaw.core.processes.ProcessQueue;
import edu.kennesaw.core.processes.ProcessState;
import edu.kennesaw.core.utils.Config;
import edu.kennesaw.metrics.Metrics;

public class LongTermScheduler implements Scheduler {

    //Fields
    private final PagedMemory _primaryMemory;
    private final Memory _secondaryMemory;
    private final ProcessQueue _jobQueue;
    private final ProcessQueue _readyQueue;
    private final int _maxParallelism;

    public LongTermScheduler(PagedMemory primaryMemory, Memory secondaryMemory, ProcessQueue jobQueue, ProcessQueue readyQueue, int maxParallelism) {
        _primaryMemory = primaryMemory;
        _secondaryMemory = secondaryMemory;
        _jobQueue = jobQueue;
        _readyQueue = readyQueue;
        _maxParallelism = maxParallelism;
    }

    @Override
    public void schedule() throws FailedToScheduleException{
        while(!_jobQueue.isEmpty() && _readyQueue.count() <= _maxParallelism){

            ProcessControlBlock pcb = _jobQueue.getSchedulingPolicy().select(_jobQueue.getProcesses());

            _jobQueue.remove(pcb);

            int processSize = (pcb.textSize + pcb.inputBufferSize + pcb.outputBufferSize + pcb.temporaryBufferSize) * Config.WORD_SIZE;

            try{
                int[] pageTable = _primaryMemory.allocate(processSize);

                //Terminate this scheduling session if no space has been allocated
                if(pageTable == null) {
                    _jobQueue.add(pcb);
                    return;
                }

                byte[] job = _secondaryMemory.read(pcb.diskAddress, processSize);

                loadToPrimaryMemory(pageTable, job);

                pcb.pageTable = pageTable;

                pcb.state = ProcessState.READY;

                _readyQueue.add(pcb);
                //Metrics log
                Metrics.job().created(pcb.jobId);
            }catch(Exception e){
                _jobQueue.add(pcb);
                throw new FailedToScheduleException(e);
            }
        }
    }

    private void loadToPrimaryMemory(int[] pageTable, byte[] job) throws MemoryOverflowException {
        int size = job.length;
        int pageSize = _primaryMemory.getPageSize();
        int bytesNeeded;
        byte[] chunk;

        for(int i = 0; i < pageTable.length; i++){
            chunk = new byte[pageSize];
            bytesNeeded = size - (pageSize * i) >= pageSize ? pageSize : size - (pageSize * i);

            System.arraycopy(job, pageSize * i, chunk, 0, bytesNeeded);

            _primaryMemory.write(pageTable[i] * pageSize, chunk);
        }

    }

}
