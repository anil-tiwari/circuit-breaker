package com.akt.example;

import com.akt.example.circuitbreaker.CircuitBreaker;
import com.akt.example.circuitbreaker.service.impl.ConsoleLoggerListener;
import com.akt.example.circuitbreaker.service.impl.CountBasedSlidingWindow;
import com.akt.example.circuitbreaker.service.SlidingWindow;
import com.akt.example.circuitbreaker.service.impl.TimeBasedSlidingWindow;

import java.time.Duration;
import java.util.Random;
import java.util.function.Supplier;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        Random random = new Random();

        SlidingWindow slidingWindowCountBase = new CountBasedSlidingWindow(
                10, // window size: last 10 calls
                50, // 50% failure threshold
                200 // 200 ms slow call threshold
        );

        SlidingWindow slidingWindowTimeBase = new TimeBasedSlidingWindow(
                Duration.ofSeconds(30), // sliding window of last 30 seconds
                50,                    // 50% failure threshold
                200                    // consider >200ms as slow
        );

        CircuitBreaker<String> countBaseCB = new CircuitBreaker<>(
                "CircuitBreaker - CountBased",
                slidingWindowCountBase,
                Duration.ofSeconds(5), // open state timeout
                3,                     // 3 trial calls in HALF_OPEN
                new ConsoleLoggerListener()
        );

        CircuitBreaker<String> timeBasedCB = new CircuitBreaker<>(
                "CircuitBreaker - TimeBased",
                slidingWindowTimeBase,
                Duration.ofSeconds(5), // Stay OPEN for 5 seconds
                3,                     // Allow 3 calls in HALF_OPEN
                new ConsoleLoggerListener()
        );

        Supplier<String> remoteCallCountBase = () -> {
            int delay = random.nextInt(300); // 0–299 ms
            if (random.nextInt(100) < 40) {  // 40% chance of failure
                throw new RuntimeException("Simulated failure");
            }
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ignored) {}
            return "Success";
        };

        Supplier<String> remoteCallTimeBase = () -> {
            int delay = random.nextInt(300); // 0–299 ms
            if (random.nextInt(100) < 40) {  // 40% chance of failure
                throw new RuntimeException("Simulated failure");
            }
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ignored) {}
            return "Success";
        };

        Supplier<String> fallback = () -> "Fallback Response";

        for (int i = 1; i <= 50; i++) {
            String response = countBaseCB.execute(remoteCallCountBase, fallback);
            System.out.printf("Request %2d: Response = %-18s | State = %s%n",
                    i, response, countBaseCB.getState());
            Thread.sleep(500); // simulate request rate
        }

        for (int i = 1; i <= 50; i++) {
            String response = timeBasedCB.execute(remoteCallTimeBase, fallback);
            System.out.printf("Request %2d: Response = %-18s | State = %s%n",
                    i, response, timeBasedCB.getState());
            Thread.sleep(500); // simulate request rate
        }
    }
}
