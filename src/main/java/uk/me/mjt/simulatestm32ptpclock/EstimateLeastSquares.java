
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;


public class EstimateLeastSquares implements FeedbackController {
    String lastNote;
    
    double[][] normalMatrix = {{0,0},{0,0}};
    double[] normalVector = {0,0};
    int sampleCount = 0;
    
    public EstimateLeastSquares() {
    }
    
    @Override
    public BigInteger updateDataGetNewControlOutput(BigDecimal trueTimeSeconds, BigInteger measuredTimeNanoseconds) {
        long trueTimeMicros = trueTimeSeconds.multiply(new BigDecimal("1000000")).longValueExact();
        long errorMicros = measuredTimeNanoseconds.divide(new BigInteger("1000")).longValueExact()-trueTimeMicros;
        
        double trueTimeSecs = trueTimeMicros / 1000000.0;
        
        normalMatrix[0][0] += 1;
        normalMatrix[1][0] += trueTimeSecs;
        normalMatrix[0][1] += trueTimeSecs;
        normalMatrix[1][1] += trueTimeSecs*trueTimeSecs;
        
        normalVector[0] += errorMicros;
        normalVector[1] += trueTimeSecs*errorMicros;
        
        sampleCount++;
        
        System.out.println(trueTimeSecs + "," + errorMicros + "," +
                Arrays.deepToString(normalMatrix) + "," +
                Arrays.toString(normalVector));
        
        if (sampleCount >= 2) {
            // Invert normal matrix:
            double s = 1/(normalMatrix[0][0]*normalMatrix[1][1]-normalMatrix[0][1]*normalMatrix[1][0]);
            double[][] invNorm = {{s*normalMatrix[1][1],-s*normalMatrix[0][1]},
                                 {-s*normalMatrix[1][0],s*normalMatrix[0][0]}};
            
            double[] result = {invNorm[0][0]*normalVector[0] + invNorm[0][1]*normalVector[1],
                               invNorm[1][0]*normalVector[0] + invNorm[1][1]*normalVector[1] };
            
            lastNote = String.format("%.4f\t%.4f", result[0], result[1]);
        }
        
        
        return BigInteger.ZERO;
    }
    
    public String getNote() {
        return lastNote;
    }
}
