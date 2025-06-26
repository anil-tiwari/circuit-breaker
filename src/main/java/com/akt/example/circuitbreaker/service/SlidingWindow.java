package com.akt.example.circuitbreaker.service;

import com.akt.example.circuitbreaker.model.CallResult;

public interface SlidingWindow {

    /**
     * add a new result (success/failure with timestamp)
     */
    void recordCall(CallResult result);

    /**
     * @return true if failure threshold is breached
     * e.g. failure rate has crossed the configured threshold 50%
     */
    boolean isThresholdBreached();
}
