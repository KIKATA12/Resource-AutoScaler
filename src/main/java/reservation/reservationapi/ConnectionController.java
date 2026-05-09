package reservation.reservationapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ConnectionController {
    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @PostMapping("/request")
    public Map<String, Object> request(@RequestParam String clientId) {
        boolean granted = connectionService.requestConnection(clientId);
        Map<String, Object> response = new HashMap<>();
        response.put("clientId", clientId);
        response.put("status", granted ? "granted" : "queued");
        response.put("activeConnections", connectionService.getActiveConnections());
        if (!granted) {
            // Find the reservation and include token
            Reservation res = connectionService.getReservationQueue().stream()
                    .filter(r -> r.getClientId().equals(clientId))
                    .findFirst().orElse(null);
            if (res != null) {
                response.put("token", res.getToken());
            }
        }
        return response;
    }

    @PostMapping("/release")
    public Map<String, Object> release() {
        connectionService.releaseConnection();
        return Map.of("status", "released", "activeConnections", connectionService.getActiveConnections());
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("activeConnections", connectionService.getActiveConnections(),
                "queueSize", connectionService.getReservationQueue().size());
    }

    @GetMapping("/queue")
    public List<Map<String, Object>> queue() {
        return connectionService.getReservationQueue().stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("clientId", r.getClientId());
                    map.put("token", r.getToken());
                    map.put("timeoutCount", r.getTimeoutCount());
                    map.put("timestamp", r.getTimestamp());
                    return map;
                })
                .toList();
    }
}
