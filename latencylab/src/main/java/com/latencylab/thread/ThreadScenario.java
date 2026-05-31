

package com.latencylab.thread; 

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import com.latencylab.latency.LatencyRecorder;



// this object will show how thread scheduling scheduling affects tail latency 
/**
 * Scenario-1 : shared thread pool -> threads compete, context swtich ==> latency spike 
 * Scenario-2 : virtual threads -> Java 21 lighweight threads
 * Scenario-3 : dedicated threads -> OS threads / task, islolated (expensive)
 * 
 * --> scheduling delay will be showed on metrics p99/p999
 */
public class ThreadScenario { 


    private static final int TASKS = 100_000; 

    // SHARED THREAD POOL -> thread compute for CPU, tasks will fight for few threads
    public static LatencyRecorder runSharedPool() throws InterruptedException{ 
        System.out.println("\n[THREADS] SHARED POOL (4 shared pool, 100k tasks)");    
        var recorder = new LatencyRecorder();
        var pool = Executors.newFixedThreadPool(4); 
        var latch = new CountDownLatch(TASKS);

        long start = System.nanoTime(); 
        for(int i = 0 ; i < TASKS; i++){ 
            final long submitTime = System.nanoTime() ; 

            pool.submit(()-> { 
                long scheduleLatency = System.nanoTime() - submitTime ;
                recorder.record(scheduleLatency); 
                // doWork(); // func tbd
                latch.countDown();
            });
        }
        latch.await();
        pool.shutdown();
          System.out.printf(
                "  Total time: %.2fms%n",
                (System.nanoTime() - start) / 1_000_000.0
        );

        recorder.printReport();

            return recorder ;



    }

    // Virtual Threads -> java 21 feature, each task gets virtual thread
    public static LatencyRecorder runVirtualThreads() throws InterruptedException{ 
        System.out.println("\n[THREADS] VIRTUAL THREADS (100k virtual threads)"); 


        var recorder = new LatencyRecorder() ;
        var pool = Executors.newVirtualThreadPerTaskExecutor(); 
        var latch = new CountDownLatch(TASKS); 

        long start = System.nanoTime(); 
        for(int i = 0 ; i < TASKS; i++){ 
            final long submitTime = System.nanoTime() ; 

            pool.submit(()-> { 
                long scheduleLatency = System.nanoTime() - submitTime ;
                recorder.record(scheduleLatency); 
                doWork(); // func tbd
                latch.countDown();
            });
        }
        
        latch.await();
        pool.shutdown();
          System.out.printf(
                "  Total time: %.2fms%n",
                (System.nanoTime() - start) / 1_000_000.0
        );

        recorder.printReport();

            return recorder ;
    }


    // dedicated os Threads -> one task per dedicated thread
    public static LatencyRecorder runDedicatedThreads() throws InterruptedException{ 

        var recorder = new LatencyRecorder();
        int tasks = 1_000;
        var latch = new CountDownLatch(tasks);

        long start = System.nanoTime();

        for (int i = 0; i < tasks; i++) {
            final long submitTime = System.nanoTime();

            Thread thread = new Thread(() -> {
                long scheduleLatency = System.nanoTime() - submitTime;
                recorder.record(scheduleLatency);
                doWork();
                latch.countDown();
            });

            thread.start();
        }

        latch.await();

        System.out.printf(
                "  Total time: %.2fms%n",
                (System.nanoTime() - start) / 1_000_000.0
        );

        recorder.printReport();
        return recorder;
    }

    public doWork(){ 
        long end = System.nanoTime() + 1_000 ; // simualte 1 us of computation
        while(System.nanoTime() < end){ 
            Thread.onSpinWait(); 
        }
    }
    
}