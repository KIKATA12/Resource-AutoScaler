package reservation.reservationapi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ConnectionService {
    private final JdbcTemplate jdbcTemplate;
    private int maxConnections;
    private int activeConnections = 0;
    private final List<Reservation> reservationQueue = new ArrayList<>();

    public ConnectionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.maxConnections = getMaxConnectionsFromDB();
    }

    private int getMaxConnectionsFromDB() {
        try {
            Integer maxConn = jdbcTemplate.queryForObject("SHOW max_connections;", Integer.class);
            return maxConn != null ? maxConn : 10; // fallback
        } catch (Exception e) {
            System.err.println("Failed to query max_connections: " + e.getMessage());
            return 10; // fallback
        }
    }

    public synchronized boolean requestConnection(String clientId) {
        if (activeConnections < maxConnections && reservationQueue.isEmpty()) {
            activeConnections++;
            return true;
        } else {
            Reservation reservation = new Reservation(clientId, 0, 30000L); // 30 seconds timeout
            reservationQueue.add(reservation);
            return false;
        }
    }
    
    public synchronized void releaseConnection() {
        if (activeConnections > 0)
            activeConnections--;
        cleanExpiredReservations();
        if (!reservationQueue.isEmpty()) {
            reservationQueue.sort(Comparator.comparingInt(Reservation::getTimeoutCount).reversed());
            Reservation next = reservationQueue.remove(0);
            if (next.getTimeoutCount() >= 5) {
                System.out.println("Denied permanently: " + next.getClientId());
            } else {
                activeConnections++;
                System.out.println("Granted silently to: " + next.getClientId());
            }
        }
    }

    public void cleanExpiredReservations() {
        List<Reservation> expired = reservationQueue.stream().filter(Reservation::isExpired).toList();
        reservationQueue.removeAll(expired);
        for (Reservation res : expired) {
            Reservation newRes = new Reservation(res.getClientId(), res.getTimeoutCount() + 1, 30000L);
            reservationQueue.add(newRes);
        }
    }

    public int getActiveConnections() {
        return activeConnections;
    }

    public List<Reservation> getReservationQueue() {
        return reservationQueue; 
    }
}
