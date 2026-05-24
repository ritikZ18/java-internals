
package com.latencylab.gc ; 

import com.sun.management.GarbageCollectionNotificationInfo;
import javax.management.* ;
import javax.management.openmbean.CompositeData;
import javax.lang.management.*;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.* ; 

// created for hooking into JVM GC notification
// printing every GC event with pause duration 
// GC pauses due to latecny spikes, this will help to check when the GC again fires
public class GCObserver{ 


    private final AtomicLong totalGCCount = new AtomicLong();
    private final AtomicLong totalGCPauseMs = new AtomicLong();
    private final AtomicLong maxGCPauseMs = new AtomicLong();




    public void start(){ 
        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBean()){ 

            // edge case 
            if(!(gcBean instanceof NotificationEmmitter emitter)) continue ;

            emitter.addNotificationListener((notification, handback) -> { 
                if(!notification.getType().equals(
                    GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION))
                    return ; 
                
                    var info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());

                    long pauseMs = info.getGcInfo().getDuration();
                    String gcName = info.getGcName();
                    String gcCause = info.getGcCause();

                    totalGCCount.incrementAndGet();
                    totalGCPauseMs.addAndGet(pauseMs);
                    maxGCPauseMs.updateAndGet(prev -> Math.max(prev, pauseMs));

                // Print every GC to see latency spikes
                System.out.printf(
                    "[GC] %-30s | cause=%-20s | pause=%dms%n",
                    gcName, gcCause, pauseMs
                );

                    
                
            }, null, null ); 

        }
        System.out.println("[GCObserver], Listening for GC events.....");



        
    }

    public void printSummary(){ 
        System.out.println("\n=== GC Summary ===");
        System.out.printf("  Total GC events : %d%n", totalGCCount.get());
        System.out.printf("  Total pause     : %dms%n", totalGCPauseMs.get());
        System.out.printf("  Max single pause: %dms%n", maxGCPauseMs.get());
        System.out.printf("  Avg pause       : %.2fms%n",
            totalGCCount.get() == 0 ? 0 :
            (double) totalGCPauseMs.get() / totalGCCount.get());
    }

}