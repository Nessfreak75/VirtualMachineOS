package edu.kennesaw.core.execution;

import edu.kennesaw.core.execution.CentralProcessingUnit.Cpu;
import edu.kennesaw.core.execution.scheduling.LongTermScheduler;
import edu.kennesaw.core.execution.scheduling.ShortTermScheduler;
import edu.kennesaw.core.execution.scheduling.policies.FifoPolicy;
import edu.kennesaw.core.execution.scheduling.policies.PriorityPolicy;
import edu.kennesaw.core.memory.*;
import edu.kennesaw.core.processes.ProcessQueue;
import edu.kennesaw.core.utils.Stopwatch;
import edu.kennesaw.metrics.Metrics;

import java.util.ArrayList;
import java.util.List;
import java.io.FileNotFoundException;

public class OsDriver {

    public enum ExecutionResult{OK, FAIL}
    //Fields
    private final Loader _loader;
    private final ProcessQueue _jobQueue;
    private final ProcessQueue _readyQueue;
    private final Memory _disk;
    private final PagedMemory _ram;
    private final MemoryManagementUnit _ramMmu;
    private final LongTermScheduler _jobScheduler;
    private final ShortTermScheduler _cpuScheduler;
    private final Cpu[] _cpus;
    private final List<Thread> _cpuThreads;

    public OsDriver(int cpus, int ramSize, int diskSize) throws FileNotFoundException, MemoryInitializationException {
        //Start the system stopwatch
        Stopwatch.start();
        //Setup memories
        _disk = new SimpleMemory(diskSize);
        _ram = new PagedMemory(ramSize, 64);
        _ramMmu = new MemoryManagementUnit(_ram);
        //Start memory monitor
        Metrics.memory().register(_ram, "RAM");
        //Setup queues
        _jobQueue = new ProcessQueue(new FifoPolicy());
        _readyQueue = new ProcessQueue(new PriorityPolicy());

        //Change path to reflect your environment.
        String programFileSinglePath = System.getProperty("user.dir") + "/src/edu/kennesaw/program-file-sum.txt";
        String programFileMultiPath = System.getProperty("user.dir") + "/src/edu/kennesaw/program-file-multi.txt";

        //programFileSinglePath = System.getProperty("user.dir") + "/src/edu/kennesaw/program-file-four-jobs.txt";

        //_loader = new Loader(programFileSinglePath, _jobQueue, _disk);
        _loader = new Loader(programFileMultiPath, _jobQueue, _disk);
        //Setup CPUs
        _cpus = new Cpu[cpus];
        _cpuThreads = new ArrayList<>();
        spawnCpus();

        _jobScheduler = new LongTermScheduler(_ram, _disk, _jobQueue, _readyQueue, cpus * 2);
        _cpuScheduler = new ShortTermScheduler(_readyQueue, _cpus, _ram);
    }

    public ExecutionResult powerOn(){
        System.out.println("Going for system startup.");
        //Load jobs to disk or fail
        try{
            _loader.loadJobs();
        }catch(Exception e){
            System.out.println("System failed to start. Terminating.");
            System.out.println(e.getMessage());

            return ExecutionResult.FAIL;
        }
        //Power on the CPUs and optionally set the delay (recommended).
        setCpuExecutionDelay(20);
        startCpus();
        try{
            //This is the main scheduling loop of the driver.
            do {
                _jobScheduler.schedule();
                _cpuScheduler.schedule();

                //The loop will terminate if all CPUs finish their jobs
                //and there are no jobs waiting to be executed.
            } while (!cpusDone() || !_jobQueue.isEmpty() || !_readyQueue.isEmpty());

        }catch(Exception e){
            System.out.println(e.getMessage());
            return ExecutionResult.FAIL;
        }finally {
            System.out.println("All jobs done. Cleaning up.");
            _cpuScheduler.cleanUp();
        }

        //Try to gracefully terminate CPUs
        try {
            stopCpus();
        }catch(Exception e){
            System.out.println("CPU termination failed.");
            return ExecutionResult.FAIL;
        }

        System.out.println("Going for system shutdown.");
        long runtime = Stopwatch.tick();
        System.out.println(String.format("Total system runtime: %fs (%dms)", runtime / 1000.0, runtime));

        //Print metrics
        Metrics.job().printStats();
        Metrics.memory().printStats();
        Metrics.memory().shutdown();
        Metrics.cpu().printStats();

        return ExecutionResult.OK;
    }

    private void spawnCpus() throws MemoryInitializationException{
        for(int i = 0; i < _cpus.length; i++){
            Cpu cpu = new Cpu(i, _ramMmu);
            Metrics.cpu().register(i);
            _cpus[i] = cpu;
        }

    }
    private void stopCpus() throws Exception{
        System.out.println("Going for CPU shutdown.");
        for(int i = 0; i < _cpus.length; i++){
            _cpus[i].exit();
            //Wait for all the CPU threads to terminate.
            _cpuThreads.get(i).join();
        }
        //Release all references to the CPU threads.
        _cpuThreads.clear();
        System.out.println("All CPUs terminated successfully.");
    }
    private void startCpus(){
        System.out.println("Going for CPU power on.");

        for(int i = 0; i < _cpus.length; i++){
            Thread t = new Thread(_cpus[i]);
            t.start();
            _cpuThreads.add(t);
            System.out.println(String.format("CPU#%d powered on.", i));
        }
        System.out.println(String.format("All CPUs powered on successfully. Total CPUs: %d", _cpus.length));
    }

    private void setCpuExecutionDelay(int delay){
        System.out.println(String.format("Setting execution delay on all CPUs (%dms).", delay));
        for(Cpu cpu : _cpus)
            cpu.setExecutionDelay(delay);
    }

    private boolean cpusDone(){
        for(Cpu cpu : _cpus){
            if(!cpu.isIdle())
                return false;
        }
        return true;
    }

}
