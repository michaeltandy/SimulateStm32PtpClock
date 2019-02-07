
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;


public class BiasedCrystalSimulation {
    
    private final BigDecimal ONE_BILLION = new BigDecimal("1000000000");
    private final BigDecimal CLOCK_RATE_HZ = new BigDecimal("180000000");
    
    public BigDecimal trueTimeSeconds = BigDecimal.ZERO;
    private BigDecimal previousClocks = BigDecimal.ZERO;
    private BigDecimal biasScale = BigDecimal.ONE;
    
    public void setBiasPartsPerBillion(BigInteger biasPartsPerBillion) {
        BigDecimal bias = new BigDecimal(biasPartsPerBillion).divide(ONE_BILLION);
        biasScale = BigDecimal.ONE.add(bias);
    }
    
    public BigInteger clockBySeconds(BigDecimal deltaSecs) {
        trueTimeSeconds = trueTimeSeconds.add(deltaSecs);
        if (trueTimeSeconds.scale() > 20) {
            trueTimeSeconds = trueTimeSeconds.setScale(20, RoundingMode.HALF_EVEN);
        }
        
        BigDecimal deltaClocks = deltaSecs.multiply(biasScale).multiply(CLOCK_RATE_HZ);
        BigDecimal subsequentClocks = previousClocks.add(deltaClocks);
        if (subsequentClocks.scale() > 12) {
            subsequentClocks.setScale(12, RoundingMode.HALF_EVEN);
        }
        
        BigDecimal clocksRequired = floor(subsequentClocks)
                .subtract(floor(previousClocks));
        
        previousClocks = subsequentClocks;
        
        return clocksRequired.toBigIntegerExact();
    }
    
    public BigDecimal floor(BigDecimal in) {
        return in.setScale(0, RoundingMode.FLOOR);
    }
    
    public BigInteger getTrueTimeNanoseconds() {
        return trueTimeSeconds.multiply(ONE_BILLION).toBigInteger();
    }
    
    public BigInteger getBiasedTimeNanoseconds() {
        return previousClocks.divide(CLOCK_RATE_HZ).multiply(ONE_BILLION).toBigInteger();
    }
    
    public String toString() {
        return trueTimeSeconds + "s unbiased " + previousClocks.divide(CLOCK_RATE_HZ) + "s biased";
    }

}
