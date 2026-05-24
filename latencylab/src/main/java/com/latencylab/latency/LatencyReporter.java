// just compare the latencies results sidebysidefrom recorder 

package com.latencylab.latency ;

import java.util.LinkedHashMap;
import java.util.Map;

public class LatencyReporter { 

    // insertion order, check
    private final Map<String, LatencyRecorder> recorders = new LinkedHashMap<>(); 

    public void add(String label, LatencyRecorder recorder){ 
        recorders.put(label, recorder);
    }

    public void printComparisonTable(){ 

        if (recorders.isEmpty()) {
            System.out.println("[LatencyReporter] No recorders added.");
            return;
        }

        System.out.println("\n");
        System.out.println("═".repeat(90));
        System.out.println("  LATENCY COMPARISON TABLE (all values in microseconds µs)");
        System.out.println("═".repeat(90));

        System.out.printf(
                "  %-28s │ %8s │ %8s │ %9s │ %10s │ %10s%n",
                "Scenario",
                "p50",
                "p99",
                "p99.9",
                "p99.99",
                "MAX"
        );

        System.out.println("  " + "─".repeat(86));

        for(var entry : recorders.entrySet()){ 
            String label = entry.getKey(); 
            LatencyRecorder r = entry.getValue() ; 
              System.out.printf(
                    "  %-28s │ %7.2fµ │ %7.2fµ │ %8.2fµ │ %9.2fµ │ %9.2fµ%n",
                    label,
                    r.getPercentile(50) / 1_000.0,
                    r.getPercentile(99) / 1_000.0,
                    r.getPercentile(99.9) / 1_000.0,
                    r.getPercentile(99.99) / 1_000.0,
                    r.getMax() / 1_000.0
            );
        }

        System.out.println(" " + "--".repeat(86));
        System.out.println();

        printWinner(); 
    }
      private void printWinner() {
        String bestP99Label = null;
        double bestP99 = Double.MAX_VALUE;

        for (var entry : recorders.entrySet()) {
            double p99 = entry.getValue().getPercentile(99);

            if (p99 < bestP99) {
                bestP99 = p99;
                bestP99Label = entry.getKey();
            }
        }

        if (bestP99Label != null) {
            System.out.printf(
                    "  ✓ Best p99: %-28s (%.2fµs)%n%n",
                    bestP99Label,
                    bestP99 / 1_000.0
            );
        }
    }
    
}