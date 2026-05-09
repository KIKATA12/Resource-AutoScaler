package reservation.reservationapi;

import java.util.Random;

public class NeuralClassifier {
    private static final int INPUTS = 5;
    private static final int HIDDEN = 4;
    @SuppressWarnings("unused")
    private static final int OUTPUTS = 1;
    private double[][] weightsInputHidden = new double[INPUTS][HIDDEN];
    private double[] weightsHiddenOutput = new double[HIDDEN];
    private double[] hiddenBias = new double[HIDDEN];
    private double outputBias;

    public NeuralClassifier() {
        Random rand = new Random();
        for (int i = 0; i < INPUTS; i++) {
            for (int j = 0; j < HIDDEN; j++) {
                weightsInputHidden[i][j] = rand.nextDouble() - 0.5;
            }
        }
        for (int j = 0; j < HIDDEN; j++) {
            weightsHiddenOutput[j] = rand.nextDouble() - 0.5;
            hiddenBias[j] = rand.nextDouble() - 0.5;
        }
        outputBias = rand.nextDouble() - 0.5;
    }

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    public double predict(double latency, double timeout, double packetLoss,
            double cpuSpike, double dbLoad) {
        double[] inputs = { latency, timeout, packetLoss, cpuSpike, dbLoad };
        double[] hiddenOutputs = new double[HIDDEN];
        for (int j = 0; j < HIDDEN; j++) {
            double sum = hiddenBias[j];
            for (int i = 0; i < INPUTS; i++) {
                sum += inputs[i] * weightsInputHidden[i][j];
            }
            hiddenOutputs[j] = sigmoid(sum);
        }
        double sumOutput = outputBias;
        for (int j = 0; j < HIDDEN; j++) {
            sumOutput += hiddenOutputs[j] * weightsHiddenOutput[j];
        }
        return sigmoid(sumOutput);
    }
}
