package com.latencylab.latency ;

import org.HdrHistogram.Histogram;

// we use the HDR Histogram, recording the latency with safepoint baises
public class LatencyRecorder {

    // track valye from 1ns to 30s
    private final Histogram histogram = new Histogram(
            1, // min value, nano sec (ns)
            30_000_000_000L, // max 30 sec
            3 // significant digits
    );

    private final String name;

    public LatencyRecorder(String name) {
        this.name = name;
    }

    public void record(long latencyNanos) {
        if (latencyNanos > 0) {
            histogram.recordValue(latencyNanos);
        }
    }

    public void printReport() {
        System.out.println("\n=== Latency Report: " + name + " ===");
        System.out.printf("  Count   : %,d events%n",
                histogram.getTotalCount());
        System.out.printf("  p50     : %,.2f µs%n",
                histogram.getValueAtPercentile(50) / 1_000.0);
        System.out.printf("  p90     : %,.2f µs%n",
                histogram.getValueAtPercentile(90) / 1_000.0);
        System.out.printf("  p99     : %,.2f µs%n",
                histogram.getValueAtPercentile(99) / 1_000.0);
        System.out.printf("  p99.9   : %,.2f µs%n",
                histogram.getValueAtPercentile(99.9) / 1_000.0);
        System.out.printf("  p99.99  : %,.2f µs%n",
                histogram.getValueAtPercentile(99.99) / 1_000.0);
        System.out.printf("  MAX     : %,.2f µs%n",
                histogram.getMaxValue() / 1_000.0);
        System.out.println();
    }

    public void reset() {
        histogram.reset();
    }
        public synchronized double getPercentile(double percentile) {
        return histogram.getValueAtPercentile(percentile);
    }

    public synchronized long getMax() {
        return histogram.getMaxValue();
    }

    public synchronized long getCount() {
        return histogram.getTotalCount();
    }

    public String getName() {
        return name;
    }

}