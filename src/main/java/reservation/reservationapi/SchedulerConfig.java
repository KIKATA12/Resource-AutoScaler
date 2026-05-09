package reservation.reservationapi;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SchedulerConfig {
    private final ConnectionService connectionService;
    private final ServerScaler serverScaler;

    public SchedulerConfig(ConnectionService connectionService, ServerScaler serverScaler) {
        this.connectionService = connectionService;
        this.serverScaler = serverScaler;
    }

    @Scheduled(fixedRate = 10000)
    public void checkTimeoutsAndScale() {
        connectionService.cleanExpiredReservations();
        serverScaler.scale();
    }
}