
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigDecimal;
import java.math.BigInteger;


public class FeedbackPIDWithIIR implements FeedbackController {
    private final double proportionalGain;
    private final double integralGain;
    private final double derivativeGain;
    private final double iir;
    
    private double lastErrorMicros = Double.NaN;
    
    private int errorIntegral = 0;
    private double errorSmoothed = 0;
    
    String lastNote;
    
    public FeedbackPIDWithIIR(double p, double i, double d, double iir) {
        this.proportionalGain = p;
        this.integralGain = i;
        this.derivativeGain = d;
        this.iir = (iir > 1 ? 1 : iir);
    }
    
    @Override
    public BigInteger updateDataGetNewControlOutput(BigDecimal trueTimeSeconds, BigInteger measuredTimeNanoseconds) {
        long trueTimeMicros = trueTimeSeconds.multiply(new BigDecimal("1000000")).longValueExact();
        long errorMicros = measuredTimeNanoseconds.divide(new BigInteger("1000")).longValueExact()-trueTimeMicros;
        
        if (Double.isNaN(lastErrorMicros)) {
            errorSmoothed = errorMicros;
            lastErrorMicros = errorMicros;
        } else {
            double thisError;
            errorSmoothed = ((1-iir)*errorSmoothed) + (iir*errorMicros);
            if (trueTimeMicros < 5*60*1000000) {
                thisError = errorMicros;
            } else {
                thisError = errorSmoothed;
            }
            
            double derivativeTerm = thisError-lastErrorMicros;
            errorIntegral += thisError;
            
            double pid = proportionalGain*thisError
                    +integralGain*errorIntegral
                    +derivativeGain*derivativeTerm;
            
            lastNote = String.format("%.4f\t%.4f\t%.4f",
                    (proportionalGain*thisError),
                    (integralGain*errorIntegral),
                    (derivativeGain*derivativeTerm));
            
            lastErrorMicros = thisError;
            return new BigInteger(""+Math.round(pid));
        }
        
        return BigInteger.ZERO;
    }
    
    public String getNote() {
        return lastNote;
    }
}
