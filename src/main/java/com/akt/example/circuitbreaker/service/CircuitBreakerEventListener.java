package com.akt.example.circuitbreaker.service;

import com.akt.example.circuitbreaker.constants.CircuitBreakerState;

public interface CircuitBreakerEventListener {
    void onStateChange(String breakerName, CircuitBreakerState newState);
}
