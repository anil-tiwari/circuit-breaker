package com.akt.example.circuitbreaker;

import com.akt.example.circuitbreaker.constants.CircuitBreakerState;
import com.akt.example.circuitbreaker.model.CallResult;
import com.akt.example.circuitbreaker.service.CircuitBreakerEventListener;
import com.akt.example.circuitbreaker.service.SlidingWindow;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * CircuitBreaker Class responsibility
 * 1. Hold current state (CLOSED, OPEN, HALF_OPEN)
 * 2. Use a SlidingWindow strategy
 * 3. Allow/deny requests based on state
 * 4. Transition between states with timing logic
 * 5. Support fallback
 * 6. Raise events on state change
 * 7. Track basic metrics
 */
public class CircuitBreaker<T> {

    private final String name;
    private final SlidingWindow window;
    private final Duration openStateTimeout;
    private final int halfOpenMaxCalls;

    private CircuitBreakerState state;
    private Instant lastStateChangeTime;
    private final AtomicInteger halfOpenTrialCalls;

    private final CircuitBreakerEventListener eventListener;

    public CircuitBreaker(String name,
                          SlidingWindow window,
                          Duration openStateTimeout,
                          int halfOpenMaxCalls,
                          CircuitBreakerEventListener eventListener) {
        this.name = name;
        this.window = window;
        this.openStateTimeout = openStateTimeout;
        this.halfOpenMaxCalls = halfOpenMaxCalls;
        this.eventListener = eventListener;

        this.state = CircuitBreakerState.CLOSED;
        this.lastStateChangeTime = Instant.now();
        this.halfOpenTrialCalls = new AtomicInteger(0);
    }

    //Encapsulates logic of allowing/rejecting execution
    public T execute(Supplier<T> primary, Supplier<T> fallback) {
        if (state == CircuitBreakerState.OPEN) {
            if (Duration.between(lastStateChangeTime, Instant.now()).compareTo(openStateTimeout) > 0) {
                // move to HALF_OPEN
                changeState(CircuitBreakerState.HALF_OPEN);
            } else {
                return fallback.get();
            }
        }

        if (state == CircuitBreakerState.HALF_OPEN) {
            if (halfOpenTrialCalls.incrementAndGet() > halfOpenMaxCalls) {
                return fallback.get();
            }
        }

        Instant start = Instant.now();
        try {
            T result = primary.get();
            long duration = Duration.between(start, Instant.now()).toMillis();
            recordResult(new CallResult(true, duration));
            return result;
        } catch (Exception e) {
            long duration = Duration.between(start, Instant.now()).toMillis();
            recordResult(new CallResult(false, duration));
            return fallback.get();
        }
    }

    //Routes behavior depending on state
    private void recordResult(CallResult result) {
        window.recordCall(result);

        if (state == CircuitBreakerState.HALF_OPEN) {
            if (!result.isSuccess()) {
                changeState(CircuitBreakerState.OPEN);
                return;
            }

            if (halfOpenTrialCalls.get() >= halfOpenMaxCalls) {
                changeState(CircuitBreakerState.CLOSED);
            }
        }

        if (state == CircuitBreakerState.CLOSED && window.isThresholdBreached()) {
            changeState(CircuitBreakerState.OPEN);
        }
    }

    //Transitions state + resets counters
    private void changeState(CircuitBreakerState newState) {
        if (this.state != newState) {
            this.state = newState;
            this.lastStateChangeTime = Instant.now();
            this.halfOpenTrialCalls.set(0);
            if (eventListener != null) {
                //To expose observability of state change
                eventListener.onStateChange(name, newState);
            }
        }
    }

    public CircuitBreakerState getState() {
        return state;
    }

    public String getName() {
        return name;
    }
}
