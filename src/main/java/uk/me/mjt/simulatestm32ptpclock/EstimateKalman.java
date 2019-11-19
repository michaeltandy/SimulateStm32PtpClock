
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigDecimal;
import java.math.BigInteger;
import static uk.me.mjt.simulatestm32ptpclock.Matrix.*;

public class EstimateKalman implements FeedbackController {
    String lastNote;
    int sampleCount = 0;
    
    double[][] x = {{0},{0}}; // Initial state (time error and rate of error increase)
    double[][] P = {{1000,0},{0,1000}}; // Initial uncertainty
    double[][] u = {{0},{0}}; // Control input
    double[][] F = {{1,1},{0,1}}; // Next state function (assumes 1 second time step)
    double[][] H = {{1,0}}; // Measurement function
    double[][] R = {{1}}; // Measurement uncertainty
    double[][] I = {{1,0},{0,1}}; // Identity matrix
    
    public EstimateKalman() {
    }
    
    @Override
    public BigInteger updateDataGetNewControlOutput(BigDecimal trueTimeSeconds, BigInteger measuredTimeNanoseconds) {
        long trueTimeMicros = trueTimeSeconds.multiply(new BigDecimal("1000000")).longValueExact();
        long errorMicros = measuredTimeNanoseconds.divide(new BigInteger("1000")).longValueExact()-trueTimeMicros;
        
        if (sampleCount > 0) {
            // Update prediction:
            x = add(multiply(F,x),u);
            P = multiply(F,multiply(P,transpose(F)));
        }
        
        // Measurement update:
        double[][] z = {{(double)errorMicros}};
        double[][] y = subtract(z, multiply(H, x));
        double[][] S = add(multiply(H,multiply(P,transpose(H))),R);
        double[][] K = multiply(P,multiply(transpose(H),invert(S)));
        
        x = add(x,multiply(K,y));
        P = multiply(subtract(I,multiply(K,H)),P);
        
        lastNote = String.format("%.4f\t%.4f", x[0][0], x[1][0]);
        
        sampleCount++;
        if (sampleCount < 10) {
            return BigInteger.ZERO;
        }
        
        double clockErrorRatePpm = -x[1][0];
        double clockErrorMicroseconds = -x[0][0];
        
        double clockCorrectionPpm;
        if (-1 < clockErrorMicroseconds && clockErrorMicroseconds < 1) {
            clockCorrectionPpm = clockErrorRatePpm;
        } else if (clockErrorMicroseconds > 50) {
            clockCorrectionPpm = clockErrorRatePpm + 10;
        } else if (clockErrorMicroseconds < -50) {
            clockCorrectionPpm = clockErrorRatePpm - 10;
        } else {
            clockCorrectionPpm = clockErrorRatePpm + (clockErrorMicroseconds/5);
        }
        
        int adjustmentPpb = (int)(clockCorrectionPpm * 1000);
        int roundedCorrection = (180*adjustmentPpb)/1000;
        double recordedCorrection = roundedCorrection / 180.0;
        u[0][0] = recordedCorrection;
        
        return BigInteger.valueOf(-adjustmentPpb);
    }
    
    public String getNote() {
        return lastNote;
    }
}
