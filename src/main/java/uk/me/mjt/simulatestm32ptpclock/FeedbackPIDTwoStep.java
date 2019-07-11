
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigDecimal;
import java.math.BigInteger;


public class FeedbackPIDTwoStep implements FeedbackController {
    private final double proportionalGain;
    private final double integralGain;
    private final double derivativeGain;
    
    private long lastErrorMicros = Long.MAX_VALUE;
    
    private int errorIntegral = 0;
    boolean passedStep = false;
    
    String lastNote;
    
    public FeedbackPIDTwoStep(double p, double i, double d) {
        this.proportionalGain = p;
        this.integralGain = i;
        this.derivativeGain = d;
    }
    
    @Override
    public BigInteger updateDataGetNewControlOutput(BigDecimal trueTimeSeconds, BigInteger measuredTimeNanoseconds) {
        long trueTimeMicros = trueTimeSeconds.multiply(new BigDecimal("1000000")).longValueExact();
        long errorMicros = measuredTimeNanoseconds.divide(new BigInteger("1000")).longValueExact()-trueTimeMicros;
        
        double pid;
        
        if (lastErrorMicros != Long.MAX_VALUE) {
            long derivativeTerm = errorMicros-lastErrorMicros;
            errorIntegral += errorMicros;
            
            if (trueTimeMicros > 3*60*1000000) {
                if (passedStep != true) {
                    passedStep = true;
                    errorIntegral = errorIntegral * 30;
                }
                
                pid = (integralGain/30)*errorIntegral;
                lastNote = String.format("0\t%.4f\t0", pid);
                
            } else {
            
                pid = proportionalGain*errorMicros
                        +integralGain*errorIntegral
                        +derivativeGain*derivativeTerm;
                
                lastNote = String.format("%.4f\t%.4f\t%.4f",
                    (proportionalGain*errorMicros),
                    (integralGain*errorIntegral),
                    (derivativeGain*derivativeTerm));
            }
            
            return new BigInteger(""+Math.round(pid));
        }
        
        lastErrorMicros = errorMicros;
        
        return BigInteger.ZERO;
    }
    
    public String getNote() {
        return lastNote;
    }
}
