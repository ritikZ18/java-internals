// important  topic pov interview, what happens in Java 
// bad method, allocate new byte[] per event 
// --> each event create package 
// --> young gen fills up fast --> minor GC fires frquently
// --> major gc fires -> stops the world
// --> user see p99/p999 spike to ms 

package com.latencylab.processor ; 

import com.latencylab.latency.LatencyRecorder;


public class AllocatingProcessor { 


    private final LatencyRecorder recorder = new LatencyRecorder(); 
    

    public void process(long sequence, int type, int value, long publishTimestamp){ 
        // the issue is of hte new allocation on every single event 
        // case : 100k event/s = 100k object/s punching into young gen []
        // JVM HEAP
        // ┌─────────────────────────────────────────────────────┐
        // │                    JVM HEAP                         │
        // │                                                     │
        // │   ┌─────────────────────┐   ┌──────────────────┐    │
        // │   │    YOUNG GEN        │   │    OLD GEN       │    │
        // │   │  (small, fast GC)   │   │  (large,slow GC) │    │
        // │   │                     │   │                  │    │
        // │   │  ┌──────┬─────────┐ │   │  Objects that    │    │
        // │   │  │ Eden │  S0  S1 │ │   │  survived 15+    │    │
        // │   │  │      │Survivor │ │   │  GC cycles       │    │
        // │   │  └──────┴─────────┘ │   │                  │    │
        // │   └─────────────────────┘   └─────────────────┘     │
        // └─────────────────────────────────────────────────────┘

        byte[] workBuffer = new byte[256]; // this is fault, use this and it becamses the G

        // this string new format create moere objects
        String result = String.format( 
            "event-%d-type-%d-val-%d",
            sequence, type,value
        );

        // func 
        doWork(workBuffer,result); 

        long latency = System.nanoTime() - publishTimestamp ; 
        recorder.record(latency);

    }

    // doing fake work, buffer forces the JVM to execcute the fake work 
    public void doWork(byte[] buffer, String result){ 
        int hash = result.hashCode(); 
        for(int i =0 ; i < buffer.length; i++){ 
            buffer[i] = (byte)(hash+1);
        }
    }

    public LatencyRecorder getLatencyRecorder(){ 
        return recorder ; 
    }


    
}
