
// this will be used to track cpu usage + latency 
// metric : 
//     1. Process CPU : used by JVM 
//     2. System CPU : used bu machine

// high  process CPU + low latency --> goal 

package com.latencylab.cpu;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.management.OperatingSystemMXBean;

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

      public void printSummary() {
        double sumProcess = 0;
        double sumSystem = 0;
        double maxProcess = 0;
        double maxSystem = 0;

        int samples = Math.min(index, 60);

        for (int i = 0; i < samples; i++) {
            sumProcess += processCpuHistory[i];
            sumSystem += systemCpuHistory[i];
            maxProcess = Math.max(maxProcess, processCpuHistory[i]);
            maxSystem = Math.max(maxSystem, systemCpuHistory[i]);
        }

        System.out.println("\n=== CPU Summary ===");
        System.out.printf(
                "  Avg process CPU : %.1f%%%n",
                samples == 0 ? 0 : sumProcess / samples
        );
        System.out.printf("  Max process CPU : %.1f%%%n", maxProcess);
        System.out.printf(
                "  Avg system CPU  : %.1f%%%n",
                samples == 0 ? 0 : sumSystem / samples
        );
        System.out.printf("  Max system CPU  : %.1f%%%n", maxSystem);
    }

    public void stop() {
        scheduler.shutdown();
    }
}