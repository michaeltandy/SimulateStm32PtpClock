
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigDecimal;
import java.math.BigInteger;


public class FeedbackPIDWindupLimit implements FeedbackController {
    private final double proportionalGain;
    private final double integralGain;
    private final double derivativeGain;
    private final int windupLimit;
    
    private long lastErrorMicros = Long.MAX_VALUE;
    
    private int errorIntegral = 0;
    
    
    String lastNote = "";
    
    public FeedbackPIDWindupLimit(double p, double i, double d) {
        this.proportionalGain = p;
        this.integralGain = i;
        this.derivativeGain = d;
        this.windupLimit = (int)Math.round((1000.0/180.0) * 500000.0 / integralGain);
    }
    
    @Override
    public BigInteger updateDataGetNewControlOutput(BigDecimal trueTimeSeconds, BigInteger measuredTimeNanoseconds) {
        long trueTimeMicros = trueTimeSeconds.multiply(new BigDecimal("1000000")).longValueExact();
        long errorMicros = measuredTimeNanoseconds.divide(new BigInteger("1000")).longValueExact()-trueTimeMicros;
        
        if (lastErrorMicros != Long.MAX_VALUE) {
            long derivativeTerm = errorMicros-lastErrorMicros;
            errorIntegral += errorMicros;
            if (errorIntegral > windupLimit)
                errorIntegral = windupLimit;
            if (errorIntegral < -windupLimit)
                errorIntegral = -windupLimit;
            
            double pid = proportionalGain*errorMicros
                    +integralGain*errorIntegral
                    +derivativeGain*derivativeTerm;
            
            lastNote = String.format("%.4f\t%.4f\t%.4f",
                    (proportionalGain*errorMicros),
                    (integralGain*errorIntegral),
                    (derivativeGain*derivativeTerm));
            
            return new BigInteger(""+Math.round(pid));
        }
        
        lastErrorMicros = errorMicros;
        
        return BigInteger.ZERO;
    }
    
    public String getNote() {
        return lastNote;
    }
}
