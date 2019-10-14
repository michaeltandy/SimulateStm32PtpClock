
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigDecimal;
import java.math.BigInteger;


public class FeedbackPID implements FeedbackController {
    private final double proportionalGain;
    private final double integralGain;
    private final double derivativeGain;
    
    private long lastErrorMicros = Long.MAX_VALUE;
    
    private int errorIntegral = 0;
    
    String lastNote;
    
    public FeedbackPID(double p, double i, double d) {
        this.proportionalGain = p;
        this.integralGain = i;
        this.derivativeGain = d;
    }
    
    @Override
    public BigInteger updateDataGetNewControlOutput(BigDecimal trueTimeSeconds, BigInteger measuredTimeNanoseconds) {
        long trueTimeMicros = trueTimeSeconds.multiply(new BigDecimal("1000000")).longValueExact();
        long errorMicros = measuredTimeNanoseconds.divide(new BigInteger("1000")).longValueExact()-trueTimeMicros;
        
        if (lastErrorMicros != Long.MAX_VALUE) {
            long derivativeTerm = errorMicros-lastErrorMicros;
            errorIntegral += errorMicros;
            
            double pid = proportionalGain*errorMicros
                    +integralGain*errorIntegral
                    +derivativeGain*derivativeTerm;
            
            lastNote = String.format("%.4f\t%.4f\t%.4f",
                    (proportionalGain*errorMicros),
                    (integralGain*errorIntegral),
                    (derivativeGain*derivativeTerm));
            
            
            lastErrorMicros = errorMicros;
            return new BigInteger(""+Math.round(pid));
        } else {
            lastErrorMicros = errorMicros;
            return BigInteger.ZERO;
        }
    }
    
    public String getNote() {
        return lastNote;
    }
}
