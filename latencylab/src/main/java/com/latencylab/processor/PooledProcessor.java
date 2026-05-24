// one of the good methhod/pattern to preallocate everything, and resuing objects 
// --> LOGIC
//     --> less allocation during hot path 
//     --> young gen stays clean ( JVM HEAP LOGIC in ALLOCATIONPROCESSOR)
//     --> so the gc fires less 
//     --> so spikes and metrics stay close toflat LinkedHashMap
//     --> game engine often optimise like this 


package com.latencylab.processor;

import java.util.concurrent.ArrayBlockingQueue;

import com.latencylab.latency.LatencyRecorder;



public class PooledProcessor { 

    private final LatencyRecorder recorder = new LatencyRecorder("PooledProcessor"); 

    // pre allocate buffer pool, borrow/return pattern 
    private final ArrayBlockingQueue<byte[]> bufferPool ; 
    
    private  final StringBuilder reusableBuilder ; 


    public PooledProcessor(int poolSize){ 
        this.bufferPool = new ArrayBlockingQueue<>(poolSize);
        this.reusableBuilder = new  StringBuilder(256);
        
        // populate hte pool before hot path, allocation happen at startup, not at runtime 
        for(int i = 0; i < poolSize ; i++){ 
                   bufferPool.offer(new byte[256]);
        }
        System.out.printf("[PooledProcessor] Pre Allocated %d buffer %n", poolSize);
    }
    public void process(long sequence, int type, int value, long publishTimestamp){ 

        // borrow from pool, no allocation if pool has capactiy
        byte[] workBuffer = bufferPool.poll(); 
        if(workBuffer == null){ 
            // pool exhausted, signal --> so we can block, drop or expand pool 
            workBuffer = new byte[256];
        }

        try {
            reusableBuilder.setLength(0);
            reusableBuilder.append("event-").append(sequence)
                            .append("-type").append(type)
                            .append("-value").append(value);
            // fun
            
            doWork(workBuffer, reusableBuilder); 

        }
         finally  {
            // or return to pool 
            bufferPool.offer(workBuffer); 
        }
    }

    public void doWork(byte[] buffer, StringBuilder sb){ 
        // buffer <-> jvm
        int hash = sb.hashCode(); 
        for(int i = 0 ; i < buffer.length ; i++){ 
            buffer[i] = (byte)(hash+1) ; 
        }
    }

    public LatencyRecorder getLatencyRecorder(){ 
        return recorder ; 
    }

}