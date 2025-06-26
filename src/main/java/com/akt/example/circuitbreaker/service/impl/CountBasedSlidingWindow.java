package com.akt.example.circuitbreaker.service.impl;

import com.akt.example.circuitbreaker.model.CallResult;
import com.akt.example.circuitbreaker.service.SlidingWindow;

import java.util.LinkedList;
import java.util.Queue;

public class CountBasedSlidingWindow implements SlidingWindow {

    private final int maxSize; // size of the sliding window (e.g., last 100 calls)
    private final int failureThresholdPercentage; //e.g., 50% → if 50/100 recent calls failed or were slow → trip
    private final long slowCallThresholdMs; //if a call takes too long, it's considered a failure

    private final Queue<CallResult> buffer;

    public CountBasedSlidingWindow(int maxSize, int failureThresholdPercentage, long slowCallThresholdMs) {
        this.maxSize = maxSize;
        this.failureThresholdPercentage = failureThresholdPercentage;
        this.slowCallThresholdMs = slowCallThresholdMs;
        this.buffer = new LinkedList<>();
    }

    @Override
    public void recordCall(CallResult result) {
        if (buffer.size() >= maxSize) {
            buffer.poll(); // remove oldest
        }
        buffer.offer(result);
    }

    @Override
    public boolean isThresholdBreached() {
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
