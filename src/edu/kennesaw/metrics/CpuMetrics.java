package edu.kennesaw.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CpuMetrics {
    private final Map<Integer, CpuInfo> _monitoredCpus;

    public CpuMetrics(){
        _monitoredCpus = new HashMap<>();
    }

    public void register(int cpuId){
        if(!isRegistered(cpuId))
            _monitoredCpus.put(cpuId, new CpuInfo());
    }

    public boolean isRegistered(int cpuId){
        return _monitoredCpus.containsKey(cpuId);
    }

    public void jobAssigned(int cpuId, int jobId){
        if(isRegistered(cpuId))
            getInfo(cpuId).addJob(jobId);
    }

    public void printStats(){
        System.out.println("------------Cpu metrics-------------");

        double totalJobs = getTotalJobs();

        for(Map.Entry<Integer, CpuInfo> cpu : _monitoredCpus.entrySet()){
            System.out.println(String.format("Cpu id: %d", cpu.getKey()));
            System.out.println("Jobs assigned: " + getJobAssignedList(cpu.getValue()));
            System.out.println(String.format("Number of jobs executed: %d", cpu.getValue().jobs.size()));
            System.out.println(String.format("Jobs assigned: %f%%", 100 * cpu.getValue().jobs.size() / totalJobs));
        }
        System.out.println("------------------------------------");
    }

    private String getJobAssignedList(CpuInfo info){
        StringBuilder output = new StringBuilder();

        for(int jobId : info.jobs)
            output.append(String.format("%d, ", jobId));

        if(output.length() > 0)
            output.delete(output.length() - 2, output.length() - 1);

        return output.toString();
    }

    private int getTotalJobs(){
        int jobsCount = 0;

        for(Map.Entry<Integer, CpuInfo> cpu : _monitoredCpus.entrySet())
            jobsCount += cpu.getValue().jobs.size();

        return jobsCount;
    }

    private CpuInfo getInfo(int cpuId){
        return _monitoredCpus.get(cpuId);
    }

    private final class CpuInfo{
        private final List<Integer> jobs;

        public CpuInfo(){
            jobs = new ArrayList<>();
        }

        private void addJob(int jobId){
            jobs.add(jobId);
        }

    }
}
