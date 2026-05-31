
package com.latencylab;
// main function to spin all objects and pools
/**
 * This is LAtency lab - JVM mem, GC, Thread, allocation time, etc 
 * 
 * Prefered to run with different GC flags to see the actual working differences
 * 
 * 1. G1GC :  java -XX:+UseG1GC -Xms512m -Xmx512m -Xlog:gc*:gc.log
 * 
 * 
 * 
 */

import java.lang.management.ManagementFactory;

import com.latencylab.cpu.CPUMonitor;
import com.latencylab.gc.GCObserver;
import com.latencylab.latency.LatencyRecorder;
import com.latencylab.latency.LatencyReporter;
import com.latencylab.processor.AllocatingProcessor;
import com.latencylab.processor.OffHeapProcessor;
import com.latencylab.processor.PooledProcessor;
import com.latencylab.producer.EventProducer;

public class Main {
    

    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║        LATENCY LAB v1.0          ║");
        System.out.println("║  JVM GC + Allocation + Threads   ║");
        System.out.println("╚══════════════════════════════════╝");

        printJVMInfo();

        // Start observers
        var gcObserver = new GCObserver();
        var cpuMonitor = new CPUMonitor();
        var reporter = new LatencyReporter();

        gcObserver.start();
        cpuMonitor.start();

        // PART 1: Allocation Patterns
        System.out.println("\n══ PART 1: ALLOCATION PATTERNS ══");
        System.out.println("Same event rate. Same duration. Different allocation behavior.");

        var producer = new EventProducer(
                50_000,
                10
        );

        // 1A. Allocating — bad pattern
        var allocating = new AllocatingProcessor();
        producer.runAllocating(allocating);
        allocating.getLatencyRecorder().printReport();
        reporter.add("Allocating", allocating.getLatencyRecorder());
        cpuMonitor.printSummary();

        Thread.sleep(2_000);

        // 1B. Pooled — good pattern
        var pooled = new PooledProcessor(200);
        producer.runPooled(pooled);
        pooled.getLatencyRecorder().printReport();
        reporter.add("Pooled", pooled.getLatencyRecorder());
        cpuMonitor.printSummary();

        Thread.sleep(2_000);

        // 1C. Off-heap — zero GC pattern
        var offHeap = new OffHeapProcessor(1_000);
        producer.runOffHeap(offHeap);
        offHeap.getRecorder().printReport();
        reporter.add("OffHeap", offHeap.getRecorder());
        cpuMonitor.printSummary();
        offHeap.free();

        gcObserver.printSummary();

        // PART 2: Thread Scheduling
        System.out.println("\n══ PART 2: THREAD SCHEDULING ══");
        System.out.println("Same task count. Different scheduling models.");

        LatencyRecorder sharedPool = ThreadScenarios.runSharedPool();
        reporter.add("SharedPool", sharedPool);

        Thread.sleep(1_000);

        LatencyRecorder virtualThreads = ThreadScenarios.runVirtualThreads();
        reporter.add("VirtualThreads", virtualThreads);

        Thread.sleep(1_000);

        LatencyRecorder dedicated = ThreadScenarios.runDedicatedThreads();
        reporter.add("DedicatedThreads", dedicated);

        // FINAL comparison
        reporter.printComparisonTable();

        cpuMonitor.stop();

        System.out.println("Done. Check gc-*.log for GC details.");
    }

    static void printJVMInfo() {
        System.out.printf(
                "%nJVM  : %s %s%n",
                System.getProperty("java.vendor"),
                System.getProperty("java.version")
        );

        System.out.printf(
                "GC   : %s%n",
                ManagementFactory.getGarbageCollectorMXBeans()
                        .stream()
                        .map(b -> b.getName())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("unknown")
        );

        System.out.printf(
                "Heap : %dMB max%n",
                ManagementFactory.getMemoryMXBean()
                        .getHeapMemoryUsage()
                        .getMax() / 1024 / 1024
        );

        System.out.printf(
                "CPUs : %d%n%n",
                Runtime.getRuntime().availableProcessors()
        );
    }
}
