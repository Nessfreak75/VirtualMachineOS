package edu.kennesaw.metrics;

public final class Metrics {

    private static volatile JobMetrics _jobMetricsInstance;
    private static volatile CpuMetrics _cpuMetricsInstance;
    private static volatile MemoryMetrics _memoryMetricsInstance;

    private final static Object _syncLock = new Object();

    private Metrics(){}

    public static JobMetrics job(){
        if(_jobMetricsInstance == null){
            synchronized (_syncLock){
                if(_jobMetricsInstance == null)
                    _jobMetricsInstance = new JobMetrics();
            }
        }

        return _jobMetricsInstance;
    }

    public static CpuMetrics cpu(){
        if(_cpuMetricsInstance == null){
            synchronized (_syncLock){
                if(_cpuMetricsInstance == null)
                    _cpuMetricsInstance = new CpuMetrics();
            }
        }
        return _cpuMetricsInstance;
    }

    public static MemoryMetrics memory(){
        if(_memoryMetricsInstance == null){
            synchronized (_syncLock){
                if(_memoryMetricsInstance == null)
                    _memoryMetricsInstance = new MemoryMetrics();
            }
        }
        return _memoryMetricsInstance;
    }

}
