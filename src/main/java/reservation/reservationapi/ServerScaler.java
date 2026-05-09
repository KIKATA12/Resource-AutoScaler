package reservation.reservationapi;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ServerScaler {
    private final JdbcTemplate jdbcTemplate;
    private final KubernetesClient kubernetesClient;
    private final RestTemplate restTemplate;

    public ServerScaler(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.kubernetesClient = new KubernetesClientBuilder().build();
        this.restTemplate = new RestTemplate();
    }

    public void scale() {
        // Query real Prometheus metrics
        double latency = queryPrometheus("avg(api_latency_ms)");
        double timeout = queryPrometheus("avg(api_timeout_count)");
        double packetLoss = queryPrometheus("avg(network_packet_loss)");
        double cpuLoad = queryPrometheus("avg(node_cpu_seconds_total)");

        // Query DB for dbLoad
        double dbLoad = getDbLoad();

        NeuralClassifier nn = new NeuralClassifier();
        double prediction = nn.predict(latency, timeout, packetLoss, cpuLoad, dbLoad);

        ScalingFactor sf = new ScalingFactor(2, 10);
        int replicas = sf.computeReplicas(prediction, cpuLoad, dbLoad, packetLoss);
        
        // Scale Kubernetes deployment
        scaleDeployment("default", "reservation-api", replicas);
    }

    private double queryPrometheus(String query) {
        try {
            String url = "http://prometheus-server:9090/api/v1/query?query=" + query;
            // Adjust URL based on your MicroK8s Prometheus service
            String response = restTemplate.getForObject(url, String.class);
            return parsePrometheusValue(response);
        } catch (Exception e) {
            System.err.println("Failed to query Prometheus: " + e.getMessage());
            return 0.0; // fallback
        }
    }

    private double parsePrometheusValue(String response) {
        // Simple JSON parsing for Prometheus instant query response
        if (response != null && response.contains("\"value\"")) {
            try {
                int valueIndex = response.indexOf("\"value\"");
                int bracketStart = response.indexOf("[", valueIndex) + 1;
                int commaIndex = response.indexOf(",", bracketStart);
                String valueStr = response.substring(bracketStart, commaIndex).trim();
                return Double.parseDouble(valueStr);
            } catch (Exception e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private void scaleDeployment(String namespace, String deploymentName, int replicas) {
        try {
            Deployment deployment = kubernetesClient.apps().deployments().inNamespace(namespace).withName(deploymentName).get();
            if (deployment != null) {
                deployment.getSpec().setReplicas(replicas);
                kubernetesClient.apps().deployments().inNamespace(namespace).resource(deployment).update();
                System.out.println("Scaled " + deploymentName + " to " + replicas + " replicas");
            } else {
                System.err.println("Deployment " + deploymentName + " not found");
            }
        } catch (Exception e) {
            System.err.println("Failed to scale deployment: " + e.getMessage());
        }
    }
    private double getDbLoad() {
        try {
            Integer activeConn = jdbcTemplate.queryForObject("SELECT count(*) FROM pg_stat_activity WHERE state = 'active';", Integer.class);
            return activeConn != null ? (double) activeConn / 100.0 : 0.0; // normalize
        } catch (Exception e) {
            return 0.0;
        }
    }
}
