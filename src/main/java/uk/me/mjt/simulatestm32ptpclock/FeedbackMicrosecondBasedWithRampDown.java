
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigDecimal;
import java.math.BigInteger;


public class FeedbackMicrosecondBasedWithRampDown {
    private final double proportionalGain;// = new BigDecimal("-0.002");
    private final double derivativeGain;// = new BigDecimal("-0.02");
    
    private long lastErrorMicros = Long.MAX_VALUE;
    
    private int controlOutput = 0;
    
    public FeedbackMicrosecondBasedWithRampDown(double p, double d) {
        this.proportionalGain = p;
        this.derivativeGain = d;
    }
    
    public BigInteger updateDataGetNewControlOutput(BigDecimal trueTimeSeconds, BigInteger measuredTimeNanoseconds) {
        long trueTimeMicros = trueTimeSeconds.multiply(new BigDecimal("1000000")).longValueExact();
        long errorMicros = measuredTimeNanoseconds.divide(new BigInteger("1000")).longValueExact()-trueTimeMicros;
        
        int secondsFullGain = 120;
        int secondsRampDown = 120;
        
        double pidScaleFactor;
        if (trueTimeMicros < secondsFullGain*1000000) {
            pidScaleFactor = 1.0;
        } else if (trueTimeMicros < (secondsFullGain+secondsRampDown)*1000000) {
            double timeSecs = trueTimeMicros/1000000.0;
            double d = (timeSecs-secondsFullGain)/(secondsRampDown);
            pidScaleFactor = 0.9*(1.0-d) + 0.1;
        } else {
            pidScaleFactor = 0.1;
        }
        //System.out.println(pidScaleFactor);
        
        if (lastErrorMicros != Long.MAX_VALUE) {
            long derivativeTerm = errorMicros-lastErrorMicros;
            
            double pid = proportionalGain*errorMicros
                    +derivativeGain*derivativeTerm;
            
            controlOutput = controlOutput+(int)Math.round(pidScaleFactor * pid);
        }
        
        lastErrorMicros = errorMicros;
        
        return new BigInteger(""+controlOutput/100);
    }
}
