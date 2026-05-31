
// prodyce the events here 
//  --> publish event at controled rate 
//  --> omission is cordianted, hides true latency by scheduled rate

package com.latencylab.producer ; 

import java.util.concurrent.atomic.AtomicLong;

import com.latencylab.processor.AllocatingProcessor;
import com.latencylab.processor.OffHeapProcessor;
import com.latencylab.processor.PooledProcessor;
import com.lmax.disruptor.EventHandler;
import com.sun.jdi.InternalException;




public class EventProducer  {

    private final int eventsPerSecond; 
    private final int durationSeconds; 
    private final AtomicLong sequenceGen = new AtomicLong() ; 

    public EventProducer(int eventsPerSecond, int durationSeconds){ 
        this.eventsPerSecond = eventsPerSecond ; 
        this.durationSeconds = durationSeconds ; 
    }
    

    // scnenario 1 : allocating processor, gc fire
    public void runAllocating(AllocatingProcessor processor) throws InterruptedException{ 
                System.out.printf(
                "%n[Scenario] ALLOCATING — %,d events/sec for %ds%n",
                eventsPerSecond,
                durationSeconds
        );
        // func
        runScenario((seq,ts) -> { 
            processor.process(seq, (int)(seq%5), (int)(seq%100),ts);
        });
        
    }

    // scenario 2 : pooled processorts, GC quiet 
    public void  runPooled(PooledProcessor processor) throws InterruptedException { 

    System.out.printf(
                "%n[Scenario] POOLED — %,d events/sec for %ds%n",
                eventsPerSecond,
                durationSeconds
        );

        runScenario((seq, ts) ->
                processor.process(
                        seq,
                        (int) (seq % 5),
                        (int) (seq % 100),
                        ts
                )
        );

    }



    // scenario 3 : off heap proessor - zero heap allocation in hot path 
     public void runOffHeap(OffHeapProcessor processor)
            throws InterruptedException {
        System.out.printf(
                "%n[Scenario] OFF-HEAP — %,d events/sec for %ds%n",
                eventsPerSecond,
                durationSeconds
        );

        runScenario((seq, ts) ->
                processor.process(
                        seq,
                        (int) (seq % 5),
                        (int) (seq % 100),
                        ts
                )
        );
    }




    // fun runscenario
    private void runScenario(EventHandler handler) throws InterruptedException { 

    }





    


}