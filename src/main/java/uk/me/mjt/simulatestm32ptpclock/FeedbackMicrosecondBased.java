
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigDecimal;
import java.math.BigInteger;


public class FeedbackMicrosecondBased {
    private final BigDecimal proportionalGain;// = new BigDecimal("-0.002");
    private final BigDecimal derivativeGain;// = new BigDecimal("-0.02");
    
    private long lastErrorMicros = Long.MAX_VALUE;
    
    private int controlOutput = 0;
    
    public FeedbackMicrosecondBased(double p, double d) {
        this.proportionalGain = new BigDecimal(p);
        this.derivativeGain = new BigDecimal(d);
    }
    
    public BigInteger updateDataGetNewControlOutput(BigDecimal trueTimeSeconds, BigInteger measuredTimeNanoseconds) {
        long trueTimeMicros = trueTimeSeconds.multiply(new BigDecimal("1000000")).longValueExact();
        long errorMicros = measuredTimeNanoseconds.divide(new BigInteger("1000")).longValueExact()-trueTimeMicros;
        
        if (errorMicros > 0) {
            System.out.println("Slow, error " + errorMicros+"us");
            controlOutput++;
        } else {
            System.out.println("Fast, error " + errorMicros+"us");
            controlOutput--;
        }
        
        /*if (lastErrorMicros != Long.MAX_VALUE) {
            BigInteger derivativeTerm = errorNanos.subtract(lastErrorNanos);
            
            BigDecimal pid = proportionalGain.multiply(toBD(errorNanos))
                    .add(derivativeGain.multiply(toBD(derivativeTerm)));
            
            controlOutput = controlOutput.add(pid.toBigInteger());
        }*/
        
        lastErrorMicros = errorMicros;
        
        return new BigInteger(""+controlOutput);
    }
}
