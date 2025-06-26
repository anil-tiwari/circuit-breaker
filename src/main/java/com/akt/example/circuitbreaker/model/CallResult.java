package com.akt.example.circuitbreaker.model;

import java.time.Instant;

/**
    We need to track the outcome of each request:
        1. Was it successful?
        2. How long did it take?
        3. When did it happen?
    These results will be stored in the sliding window buffer to evaluate failure rate.
 */

public class CallResult {
    private final boolean success; // success: whether the call passed or failed
    private final long responseTimeMillis; //responseTimeMillis: how long the call took
    private final Instant timestamp; //timestamp: used to expire old entries in time-based sliding window

    public CallResult(boolean success, long responseTimeMillis) {
        this.success = success;
        this.responseTimeMillis = responseTimeMillis;
        this.timestamp = Instant.now();
    }

    public boolean isSuccess() {
        return success;
    }

    public long getResponseTimeMillis() {
        return responseTimeMillis;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
