package reservation.reservationapi;

import java.util.UUID;

public class Reservation {
    private String clientId;
    private String token;
    private int timeoutCount;
    private long timestamp;
    private long expirationTime; // in milliseconds

    public Reservation(String clientId, int timeoutCount, long timeoutDurationMs) {
        this.clientId = clientId;
        this.token = UUID.randomUUID().toString();
        this.timeoutCount = timeoutCount;
        this.timestamp = System.currentTimeMillis();
        this.expirationTime = timestamp + timeoutDurationMs;
    }

    // Getters and Setters
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getTimeoutCount() {
        return timeoutCount;
    }

    public void setTimeoutCount(int timeoutCount) {
        this.timeoutCount = timeoutCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }
}