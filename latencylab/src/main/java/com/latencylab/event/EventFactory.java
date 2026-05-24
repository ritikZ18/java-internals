// we can prealocate pool of event objects at start of service   
// hot path borrrows -> uses -> returns : zero allocation 


package com.latencylab.event ; 

import java.util.concurrent.ArrayBlockingQueue;

public class EventFactory { 


    private final ArrayBlockingQueue<Event> pool ; 
    private final int poolSize ; 



    public EventFactory (){ 
        this.poolSize = poolSize ; 
        this.pool = new ArrayBlockingQueue<>(poolSize) ;
        
        // allocate all event upfront before any hot path runs
        for(int i = 0; i < poolSize; i++){ 
            pool.offer(new Event()); 

        }
        System.out.printf("[EventFactory] pre-allocated  %d events%n", poolSize);
    }

    // after pool event, borrpow from pool, return all null if pool is exhausted, handle  by caller  
    public Event borrow(long sequence, int type, int value ) { 
        Event event = pool.poll(); 

        if(event == null){ 
            return null ; 
        }
        event.reset(sequence, type, value);

        return event ; 
    }

    // return event back to pool after processing, always CALLED or the pool can drained permanently

    public void release(Event event) { 

        if(event != null){ 
            pool.offer(event); 
        }
    }

    public int available() { return pool.size() ;}
    public int  poolSize() { return poolSize()  ;}





}