package com.akt.example.circuitbreaker.service.impl;

import com.akt.example.circuitbreaker.model.CallResult;
import com.akt.example.circuitbreaker.service.SlidingWindow;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;

public class TimeBasedSlidingWindow implements SlidingWindow {

    private final Duration windowSize; //e.g., 30 seconds — all calls older than this are removed.
    private final int failureThresholdPercentage; //e.g., 50% → if 50/100 recent calls failed or were slow → trip
    private final long slowCallThresholdMs; //if a call takes too long, it's considered a failure

    private final Queue<CallResult> buffer;

    public TimeBasedSlidingWindow(Duration windowSize, int failureThresholdPercentage, long slowCallThresholdMs) {
        this.windowSize = windowSize;
        this.failureThresholdPercentage = failureThresholdPercentage;
        this.slowCallThresholdMs = slowCallThresholdMs;
        this.buffer = new LinkedList<>();
    }

    //adds a new result after cleaning old ones.
    @Override
    public void recordCall(CallResult result) {
        cleanUpOldCalls();
        buffer.offer(result);
    }

    private void cleanUpOldCalls() {
        Instant cutoff = Instant.now().minus(windowSize);
        while (!buffer.isEmpty() && buffer.peek().getTimestamp().isBefore(cutoff)) {
            buffer.poll();
        }
    }

    //checks how many recent calls failed or were slow.
    @Override
    public boolean isThresholdBreached() {
        cleanUpOldCalls(); // Ensure the buffer only contains recent entries
        if (buffer.isEmpty()) return false;

        int failureCount = 0;
        for (CallResult result : buffer) {
            boolean slow = result.getResponseTimeMillis() > slowCallThresholdMs;
            if (!result.isSuccess() || slow) {
                failureCount++;
            }
        }

        int failureRate = (failureCount * 100) / buffer.size();
        return failureRate >= failureThresholdPercentage;
    }
}
