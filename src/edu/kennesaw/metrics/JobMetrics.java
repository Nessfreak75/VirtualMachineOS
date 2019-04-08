package edu.kennesaw.metrics;

import edu.kennesaw.core.utils.Stopwatch;

import java.util.HashMap;
import java.util.Map;

public final class JobMetrics {
    //Fields
    private volatile Map<Integer, JobInfo> _monitoredJobs;

    public JobMetrics(){
        _monitoredJobs = new HashMap<>();
    }

    public void register(int pid, int priority){
        if(!isRegistered(pid)){
            JobInfo jobInfo = new JobInfo();

            jobInfo.priority = priority;

            _monitoredJobs.put(pid, jobInfo);
        }

    }

    public void startedExecution(int pid){
        if(isRegistered(pid))
            getInfo(pid).beganExecution = Stopwatch.tick();
    }

    public void endedExecution(int pid){
        if(isRegistered(pid))
            getInfo(pid).endedExecution = Stopwatch.tick();
    }

    public void created(int pid){
        if(isRegistered(pid))
            getInfo(pid).created = Stopwatch.tick();
    }

    public void destroyed(int pid){
        if(isRegistered(pid))
            getInfo(pid).destroyed = Stopwatch.tick();
    }

    public void ioMade(int pid){
        if(isRegistered(pid))
            getInfo(pid).ioOperations++;
    }

    public boolean isRegistered(int pid){
        return _monitoredJobs.containsKey(pid);
    }

    //Just a helper method to make _monitoredJobs.get(pid) => getInfo(pid)
    private JobInfo getInfo(int pid){
        return _monitoredJobs.get(pid);
    }

    private double averageWaitTime(){
        long sum = 0;

        for (Map.Entry<Integer, JobInfo> job : _monitoredJobs.entrySet()){
            sum += job.getValue().getWaitTime();
        }

        return sum / (double)_monitoredJobs.size();
    }

    private double averageRunTime(){
        long sum = 0;

        for (Map.Entry<Integer, JobInfo> job : _monitoredJobs.entrySet()){
            sum += job.getValue().getRunTime();
        }

        return sum / (double)_monitoredJobs.size();
    }

    public void printStats(){
        System.out.println("------------Job metrics-------------");

        for(Map.Entry<Integer, JobInfo> job : _monitoredJobs.entrySet()){
            System.out.println(String.format(">>>Job - %d (priority: %d)", job.getKey(), job.getValue().priority));
            System.out.println(String.format("Creation time: %dms", job.getValue().created));
            System.out.println(String.format("Destruction time: %dms", job.getValue().destroyed));
            System.out.println(String.format("Begin execution time: %dms", job.getValue().beganExecution));
            System.out.println(String.format("End execution time: %dms", job.getValue().endedExecution));
            System.out.println(String.format("Run time: %dms", job.getValue().getRunTime()));
            System.out.println(String.format("Wait time: %dms", job.getValue().getWaitTime()));
            System.out.println(String.format("IO operations made: %d", job.getValue().ioOperations));
        }

        System.out.println("All jobs");
        System.out.println(String.format("Average run time: %fms", averageRunTime()));
        System.out.println(String.format("Average wait time: %fms", averageWaitTime()));

        System.out.println("------------------------------------");
    }

    //A structure to store info
    private final class JobInfo{
        public int priority;
        public long created;
        public long destroyed;
        public long beganExecution;
        public long endedExecution;
        public int ioOperations;

        public long getWaitTime(){
            return beganExecution - created;
        }
        public long getRunTime(){
            return endedExecution - beganExecution;
        }
    }
}
