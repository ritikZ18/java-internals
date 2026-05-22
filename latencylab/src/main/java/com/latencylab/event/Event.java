package com.latencylab.event ; 


// we need to fit the event flows that fits in cache line 64 bytes 
public class Event { 



   public long sequence;        // 8 bytes
    public long publishTimestamp; // 8 bytes — when published
    public long processTimestamp; // 8 bytes — when processed
    public int  type;             // 4 bytes
    public int  value;            // 4 bytes
    // total above = 32 bytes and 2 events per cahche line


    // reset the object pool, no new allocation 
    public void reset(long seq, int type, int value ){ 
            this.sequence = seq ; 
            this.publishTimestamp = System.nanoTime() ; 
            this.processTimestamp = 0 ; 
            this.type = type ;
            this.value= value ;

    }
    

    public long latencyNanos(){ 
        return processTimestamp - publishTimestamp ; 
    }



}