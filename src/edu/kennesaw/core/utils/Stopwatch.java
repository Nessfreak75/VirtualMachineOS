package edu.kennesaw.core.utils;

public final class Stopwatch {
    //Singleton fields
    private static volatile  Stopwatch _instance;
    private final static Object _syncLock = new Object();
    //Instance fields
    private final long _startTime;

    private Stopwatch(){
        _startTime = System.currentTimeMillis();
    }

    private static Stopwatch getInstance(){
        if(_instance == null){
            synchronized (_syncLock){
                if(_instance == null)
                    _instance = new Stopwatch();
            }
        }
        return _instance;
    }

    /**
     * Starts the stopwatch for the system.
     * @return True if stopwatch started otherwise false.
     */
    public static boolean start(){
        return getInstance() != null;
    }

    /**
     * Retrieves current system runtime.
     * @return Current system runtime.
     */
    public static long tick(){
        return System.currentTimeMillis() - getInstance()._startTime;
    }

}
