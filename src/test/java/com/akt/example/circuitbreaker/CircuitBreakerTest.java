package com.akt.example.circuitbreaker;

import com.akt.example.circuitbreaker.constants.CircuitBreakerState;
import com.akt.example.circuitbreaker.service.SlidingWindow;
import com.akt.example.circuitbreaker.service.impl.CountBasedSlidingWindow;
import com.akt.example.circuitbreaker.service.impl.TimeBasedSlidingWindow;
import org.junit.Test;


import java.time.Duration;
import java.util.function.Supplier;

import static junit.framework.Assert.assertEquals;


public class CircuitBreakerTest {

    @Test
    public void testCountBasedCircuitBreakerTripsOnFailures() {
        SlidingWindow window = new CountBasedSlidingWindow(
                5,     // window size
                50,    // 50% threshold
                200    // 200ms slow call threshold
        );

        CircuitBreaker<String> cb = new CircuitBreaker<>(
                "CountCB", window,
                Duration.ofSeconds(2), // open state duration
                2,                     // half-open trial calls
                (name, state) -> {}    // no-op listener
        );

        Supplier<String> fastSuccess = () -> "OK";
        Supplier<String> alwaysFail = () -> {
            throw new RuntimeException("fail");
        };

        // 3 of 5 calls fail → trips breaker
        cb.execute(alwaysFail, () -> "fallback");
        cb.execute(alwaysFail, () -> "fallback");
        cb.execute(fastSuccess, () -> "fallback");
        cb.execute(alwaysFail, () -> "fallback");
        cb.execute(fastSuccess, () -> "fallback");

        assertEquals(CircuitBreakerState.OPEN, cb.getState());
    }

    @Test
    public void testTimeBasedCircuitBreakerTripsOnSlowCalls() throws InterruptedException {
        SlidingWindow window = new TimeBasedSlidingWindow(
                Duration.ofSeconds(5),
                50,   // 50% threshold
                100   // 100ms slow threshold
        );

        CircuitBreaker<String> cb = new CircuitBreaker<>(
                "TimeCB", window,
                Duration.ofSeconds(2),
                2,
                (name, state) -> {}
        );

        Supplier<String> slowSuccess = () -> {
            try {
                Thread.sleep(150); // slow call
            } catch (InterruptedException ignored) {}
            return "OK";
        };

        cb.execute(slowSuccess, () -> "fallback");
        cb.execute(slowSuccess, () -> "fallback");
        cb.execute(slowSuccess, () -> "fallback");

        assertEquals(CircuitBreakerState.OPEN, cb.getState());
    }
}
