
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigDecimal;
import java.math.BigInteger;


public class FeedbackLogicSimulator {
    private BigInteger addend = new BigInteger("1193046471");
    
    private final BigDecimal proportionalGain;// = new BigDecimal("-0.002");
    private final BigDecimal derivativeGain;// = new BigDecimal("-0.02");
    
    private BigInteger lastErrorNanos = null;
    
    public FeedbackLogicSimulator(double p, double d) {
        this.proportionalGain = new BigDecimal(p);
        this.derivativeGain = new BigDecimal(d);
    }
    
    public BigInteger updateDataGetNewAddend(BigDecimal trueTimeSeconds, BigInteger measuredTimeNanoseconds) {
        BigInteger trueTimeNanos = trueTimeSeconds.multiply(new BigDecimal("1000000000")).toBigInteger();
        BigInteger errorNanos = measuredTimeNanoseconds.subtract(trueTimeNanos);
        
        /*if (isNegative(errorNanos)) {
            System.out.println("Slow, error " + errorNanos+"ns");
        } else {
            System.out.println("Fast, error " + errorNanos+"ns");
        }*/
        
        if (lastErrorNanos != null) {
            BigInteger derivativeTerm = errorNanos.subtract(lastErrorNanos);
            
            BigDecimal pid = proportionalGain.multiply(toBD(errorNanos))
                    .add(derivativeGain.multiply(toBD(derivativeTerm)));
            
            addend = addend.add(pid.toBigInteger());
        }
        
        lastErrorNanos = errorNanos;
        
        return addend;
    }
    
    private boolean isNegative(BigInteger bi) {
        return bi.compareTo(BigInteger.ZERO) < 0;
    }
    
    private BigDecimal toBD(BigInteger bi) {
        return new BigDecimal(bi);
    }
}
