
// this will be used to track cpu usage + latency 
// metric : 
//     1. Process CPU : used by JVM 
//     2. System CPU : used bu machine

// high  process CPU + low latency --> goal 

package com.latencylab.cpu;

import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class CPUMonitor {

    private final OperatingSystemMXBean osBean;
    private final ScheduledExecutorService scheduler;

    private final AtomicReference<double[]> latest = new AtomicReference<>(new double[] { 0, 0 });

    // check the circular buffer
    private final double[] processCpuHistory = new double[60];
    private final double[] systemCpuHistory = new double[60];

    private int index = 0;

     public CPUMonitor() {
        this.osBean = (OperatingSystemMXBean)
                ManagementFactory.getOperatingSystemMXBean();

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cpu-monitor");
            t.setDaemon(true);
            return t;
        });
    }

    public void start(){
        scheduler.scheduleAtFixedRate(() -> {
            double processCpu = osBean.getProcessCpuLoad() * 100 ; 
            double systemCpu = osBean.getCpuLoad() * 100 ; 


            processCpuHistory[index % 60] = processCpu ; 
            systemCpuHistory[index % 60] = systemCpu ; 
            index ++ ; 
            

            latest.set(new double[]{processCpu, systemCpu});  
             System.out.printf(
                    "[CPU] process=%.1f%%  system=%.1f%%%n",
                    processCpu,
                    systemCpu
            );

        }, 0 , 1 , TimeUnit.SECONDS);
                System.out.println("[CPUMonitor] Sampling every 1s...");

     }

    public void stop() {
    }
}