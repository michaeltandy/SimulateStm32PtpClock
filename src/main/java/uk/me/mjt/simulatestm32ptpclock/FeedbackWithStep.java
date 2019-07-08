
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigDecimal;
import java.math.BigInteger;


public class FeedbackWithStep implements FeedbackController {
    private final double proportionalGainA;
    private final double derivativeGainA;
    private final double proportionalGainB;
    private final double derivativeGainB;
    
    private long lastErrorMicros = Long.MAX_VALUE;
    
    private int controlOutput = 0;
    
    public FeedbackWithStep(double pA, double dA, double pB, double dB) {
        this.proportionalGainA = pA;
        this.derivativeGainA = dA;
        this.proportionalGainB = pB;
        this.derivativeGainB = dB;
    }
    
    public BigInteger updateDataGetNewControlOutput(BigDecimal trueTimeSeconds, BigInteger measuredTimeNanoseconds) {
        long trueTimeMicros = trueTimeSeconds.multiply(new BigDecimal("1000000")).longValueExact();
        long errorMicros = measuredTimeNanoseconds.divide(new BigInteger("1000")).longValueExact()-trueTimeMicros;
        
        if (lastErrorMicros != Long.MAX_VALUE) {
            long derivativeTerm = errorMicros-lastErrorMicros;
            
            double pid;
            if (trueTimeMicros < 2*60*1000000) {
                pid = proportionalGainA*errorMicros
                    +derivativeGainA*derivativeTerm;
            } else {
                pid = proportionalGainB*errorMicros
                    +derivativeGainB*derivativeTerm;
            }
            
            controlOutput = controlOutput+(int)Math.round(pid);
        }
        
        lastErrorMicros = errorMicros;
        
        return new BigInteger(""+controlOutput/100);
    }
}
