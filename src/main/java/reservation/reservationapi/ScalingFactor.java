package reservation.reservationapi;

public class ScalingFactor {
    private int minReplicas;
    private int maxReplicas;

    public ScalingFactor(int minReplicas, int maxReplicas) {
        this.minReplicas = minReplicas;
        this.maxReplicas = maxReplicas;
    }

    /**
     * Compute dynamic alpha based on server metrics.
     */
    private double computeAlpha(double cpuLoad, double dbLoad, double packetLoss) {
        double baseAlpha = 5.0;
        double wCpu = 2.0, wDb = 3.0, wNet = 1.5;
        cpuLoad = Math.min(1.0, Math.max(0.0, cpuLoad));
        dbLoad = Math.min(1.0, Math.max(0.0, dbLoad));
        packetLoss = Math.min(1.0, Math.max(0.0, packetLoss));
        return baseAlpha * (1 + wCpu * cpuLoad + wDb * dbLoad + wNet * packetLoss);
    }

    /**
     * Logarithmic scaling function.
     * 
     * @param prediction Neural net output (0–1)
     * @param cpuLoad    CPU utilization (0–1)
     * @param dbLoad     DB utilization (0–1)
     * @param packetLoss network packet loss (0–1)
     * @return target replicas
     */
    public int computeReplicas(double prediction, double cpuLoad, double dbLoad, double packetLoss) {
        prediction = Math.max(0.0, Math.min(1.0, prediction));
        double alpha = computeAlpha(cpuLoad, dbLoad, packetLoss);
        double fraction = Math.log(1 + alpha * prediction) / Math.log(1 + alpha);
        return (int) Math.round(minReplicas + fraction * (maxReplicas - minReplicas));
    }
}
