# In-Memory Circuit Breaker (Java)

This project is a **production-quality, in-memory circuit breaker implementation** written in Java using Maven.  
It supports **sliding window-based error detection**, customizable thresholds, fallback handling, state transitions, and event hooks.

---

## ✅ Features

- **Three Circuit States**: `CLOSED`, `OPEN`, `HALF_OPEN`
- **Sliding Window Variants**:
    - **Count-based**: Track X% failures in last N calls
    - **Time-based**: Track X% failures in last N seconds
- **State Transitions**:
    - Automatically transitions from `CLOSED → OPEN → HALF_OPEN → CLOSED`
- **Fallback Support**: Return a fallback response when circuit is open
- **Slow Call Detection**: Configurable timeout threshold
- **State Change Event Listener**: Hook to log/track state transitions
- **Extensible** and easy to embed in any app
- **Minimal dependencies** (no Spring)

---

## 🏗️ Project Structure

```bash
.
├── pom.xml
├── README.md
└── src
    └── main
        └── java
            └── com
                └── akt
                    └── example
                        ├── circuitbreaker
                        │	├── CircuitBreaker.java
                        │	├── constants
                        │	│	└── CircuitBreakerState.java
                        │	├── model
                        │	│	└── CallResult.java
                        │	└── service
                        │		 ├── CircuitBreakerEventListener.java
                        │		 ├── impl
                        │		 │	 ├── ConsoleLoggerListener.java
                        │		 │	 ├── CountBasedSlidingWindow.java
                        │		 │	 └── TimeBasedSlidingWindow.java
                        │		 └── SlidingWindow.java
                        └── Main.java
```
---

## 🔧 Prerequisites

- Java 17+ (Java 11 also supported)
- Maven 3.x

---

## 🚀 How to Run

1. Clone or unzip the project
2. Navigate to the project folder
3. Compile and run:

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.akt.example.circuitbreaker.Main"
```

## 🧪 Test Scenario
The Main.java simulates:
1. 100 random API calls
2. ~40% failure rate + random latency
3. Circuit transitions between states
4. Logs fallback usage and state changes

## 📊 Sample Output
```bash
[19:09:32.910529] Circuit Breaker 'CircuitBreaker - CountBased' changed state to OPEN
Request  1: Response = Fallback Response  | State = OPEN
Request  2: Response = Fallback Response  | State = OPEN
......
[19:09:37.963593] Circuit Breaker 'CircuitBreaker - CountBased' changed state to HALF_OPEN
Request 11: Response = Success            | State = HALF_OPEN
[19:09:38.550459] Circuit Breaker 'CircuitBreaker - CountBased' changed state to OPEN
.....
```

## 🛠️ Configuration Options
You can configure:
1. Failure threshold (X%)
2. Time window (for time-based)
3. Call count window (for count-based)
4. Slow call threshold (in ms)
5. Max trial calls in HALF_OPEN 
6. Fallback behavior

## 🤝 Author
Developed by: Anil Kumar Tiwari
