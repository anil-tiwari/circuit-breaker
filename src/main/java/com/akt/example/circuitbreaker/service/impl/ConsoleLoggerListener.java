package com.akt.example.circuitbreaker.service.impl;

import com.akt.example.circuitbreaker.constants.CircuitBreakerState;
import com.akt.example.circuitbreaker.service.CircuitBreakerEventListener;

public class ConsoleLoggerListener implements CircuitBreakerEventListener {
    @Override
    public void onStateChange(String breakerName, CircuitBreakerState newState) {
        System.out.printf("[%s] Circuit Breaker '%s' changed state to %s%n",
                java.time.LocalTime.now(), breakerName, newState);
    }
}
