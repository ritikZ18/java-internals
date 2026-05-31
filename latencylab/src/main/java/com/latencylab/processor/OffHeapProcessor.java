// ZGC ( zero GC) allocate memory outside the java heap 
// --> off heap mem : malloc() equivalent in java
// --> gc never sees it, so gc never collects it 
// --> you as SWE as responsible for freeing it like c++ 
// --> KAFKA

// LAYOUT FOR MEMORY SLOT

// sequence  : 8 bytes '
// type : 4 bytes 
// value : 4 bytes 
// paddinff : rest 64 byte cache line 


package com.latencylab.processor ; 

import java.lang.reflect.Field;

import com.latencylab.latency.LatencyRecorder;

import sun.misc.Unsafe;



public class OffHeapProcessor { 


private static final Unsafe UNSAFE = getUnsafe(); 

// each  slort ==> 64 byte = 1 cache line 
private static final int SLOT_SIZE = 64 ; 
private static final int OFFSET_SEQUENCE = 0 ; 
private static final int OFFSET_TYPE = 8 ;
private  static final int OFFSET_VALUE = 12 ; 

private final long baseAddress ; 
private final int slotCount ; 

private final LatencyRecorder recorder = new LatencyRecorder() ; 

public OffHeapProcessor(int slotCount){ 
    this.slotCount = slotCount ; 

    long bytes = (long) slotCount * SLOT_SIZE ; 

    // allocate raw-mem outside the jvm heap 
    this.baseAddress = UNSAFE.allocateMemory(bytes); 


    // zero out memory 
    UNSAFE.setMemory(baseAddress, bytes, (byte) 0);

    System.out.printf("[OffHeapProcessor] Allocated %dkb off-heap (%d slots x 64B)%n", bytes/1024, slotCount);

}

public void process( long sequence, int  type, int value, long publishTimestamp){ 
    // write event data to off heap mem
    // no object created , no gc involved 

    // 1. which slot ot be used, ring buffer style wraparound 
    int slot = (int) (sequence% slotCount) ; 
    long slotAddress = baseAddress + ((long) slot* SLOT_SIZE); 

    // 2. write directly to raw memroy no objects 
    UNSAFE.putLong(slotAddress + OFFSET_SEQUENCE, sequence);
    UNSAFE.putInt(slotAddress +  OFFSET_TYPE, type);
    UNSAFE.putInt(slotAddress + OFFSET_VALUE, value); 

    // 3. read it back to simualte real prcossing 
    long readSequence = UNSAFE.getLong(slotAddress+ OFFSET_SEQUENCE);
    long readType = UNSAFE.getInt(slotAddress+ OFFSET_TYPE);
    long readValue = UNSAFE.getInt(slotAddress+ OFFSET_VALUE);


    // now we avoid dead-code elimination 
    if(readSequence != sequence || readType != type || readValue != value){ 
        throw new RuntimeException("Memory Corruption detected !!!!");
    }
    long latency = System.nanoTime() - publishTimestamp ; 
    recorder.record(latency);

}

// imp, this func called after donee 
// off heap memory is not freed by gcc, 
// can cause memory leak

public void free(){ 
    UNSAFE.freeMemory(baseAddress);
    System.out.println("[offHeapMemory] off-heap memory freed")  ;
}

public LatencyRecorder getRecorder(){ 
    return recorder ; 
}

public void dumpSlot(int slot) { 
    long addr = baseAddress + ( (long) slot * SLOT_SIZE);

        System.out.printf(
                "[OffHeap] slot=%d seq=%d type=%d val=%d%n",
                slot,
                UNSAFE.getLong(addr + OFFSET_SEQUENCE),
                UNSAFE.getInt(addr + OFFSET_TYPE),
                UNSAFE.getInt(addr + OFFSET_VALUE)
        );

}




private static Unsafe getUnsafe(){ 
    try {
        Field f = Unsafe.class.getDeclaredField("theUnsafe"); 
        f.setAccessible(true);
        return  (Unsafe) f.get(null);

    } catch (Exception e) {
        throw new RuntimeException("Cannot get Unsafe Instance.", e);
    }
}

}