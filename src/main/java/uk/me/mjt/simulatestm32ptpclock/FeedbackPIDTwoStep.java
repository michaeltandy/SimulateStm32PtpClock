
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigDecimal;
import java.math.BigInteger;


public class FeedbackPIDTwoStep implements FeedbackController {
    private final double proportionalGain1;
    private final double integralGain1;
    private final double derivativeGain1;
    private final double proportionalGain2;
    private final double integralGain2;
    private final double derivativeGain2;
    
    private long lastErrorMicros = Long.MAX_VALUE;
    
    private long errorIntegral = 0;
    boolean passedStep = false;
    
    String lastNote;
    
    public FeedbackPIDTwoStep(double p1, double i1, double d1, double p2, double i2, double d2) {
        this.proportionalGain1 = p1;
        this.integralGain1 = i1;
        this.derivativeGain1 = d1;
        this.proportionalGain2 = p2;
        this.integralGain2 = i2;
        this.derivativeGain2 = d2;
    }
    
    @Override
    public BigInteger updateDataGetNewControlOutput(BigDecimal trueTimeSeconds, BigInteger measuredTimeNanoseconds) {
        long trueTimeMicros = trueTimeSeconds.multiply(new BigDecimal("1000000")).longValueExact();
        long errorMicros = measuredTimeNanoseconds.divide(new BigInteger("1000")).longValueExact()-trueTimeMicros;
        
        double p = 0;
        double i = 0;
        double d = 0;
        
        if (lastErrorMicros != Long.MAX_VALUE) {
            long derivativeTerm = errorMicros-lastErrorMicros;
            errorIntegral += errorMicros;

            if (trueTimeMicros < 10*60*1000000) {
                p = proportionalGain1*errorMicros;
                i = integralGain1*errorIntegral;
                d = derivativeGain1*derivativeTerm;
            } else {
                if (passedStep == false) {
                    passedStep = true;
                    errorIntegral = Math.round((errorIntegral*integralGain1)/integralGain2);
                }
                p = proportionalGain2*errorMicros;
                i = integralGain2*errorIntegral;
                d = derivativeGain2*derivativeTerm;
            }
        }

        lastErrorMicros = errorMicros;
        lastNote = String.format("%.4f\t%.4f\t%.4f",p,i,d);
        return new BigInteger(""+Math.round(p+i+d));
    }
    
    public String getNote() {
        return lastNote;
    }
}
