# Resource-AutoScaling

## Overview
Traditional database connection pooling strategies fail under high concurrency, leading to
blocked requests, starvation, and scaling pressure. This project introduces a
reservation-aware API that issues tokens to queued clients, prioritizes retries, and denies
abusive requests. Unlike static pooling, this approach adds fairness, resilience, visibility, and
safeguards.
Building on this foundation, the project integrates predictive auto-scaling using Prometheus
metrics, a neural network (NeuralScaler), and a ScalingFactor class with logarithmic scaling.
This ensures proactive resource allocation while preserving stability. Kubernetes integration
allows replicas to be adjusted dynamically based on prediction strength and resource
constraints.

## Dependencies
Before using this project, ensure the following are installed:
- Docker  
- Prometheus  
- MicroK8s  
- kubectl  
- PostgreSQL  
- Java 25 LTS
- Postman

## Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/KIKATA12/Server-connection-Reservation-and-Autoscaling.git
   cd Server-connection-Reservation-and-Autoscaling
2. Build the project:
   ```bash
   ./mvnw clean install
3. Run With Docker
   ```bash
   docker build -t reservation-autoscaling.
   docker run -p 8080:8080 reservation-autoscaling

## License
This project is licensed under the GNU General Public License v3.0 (GPLv3) 


