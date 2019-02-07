
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigDecimal;
import java.math.BigInteger;

public class PtpClockSimulation {
    public static final BigInteger SECONDS_IN_100_YEARS = new BigInteger("3155760000");
    
    public static final BigInteger ACCUMULATOR_OVERFLOW =
            new BigInteger("2").pow(32);
    
    public static final BigInteger SUBSECOND_OVERFLOW =
            new BigInteger("1000000000");
    
    public BigInteger addend = null;
    public BigInteger accumulator = BigInteger.ZERO;
    public BigInteger subSecondAddend = null;
    public BigInteger subsecond = BigInteger.ZERO;
    public BigInteger second = BigInteger.ZERO;

    public void clockBy(BigInteger clocks) {
        if (clocks.compareTo(BigInteger.ZERO) < 0) {
            throw new RuntimeException("Simulation doesn't support negative clocking.");
        }
        
        BigInteger accumulatorNext = accumulator.add(clocks.multiply(addend));
        BigInteger[] accDiv = accumulatorNext.divideAndRemainder(ACCUMULATOR_OVERFLOW);
        this.accumulator = accDiv[1];
        BigInteger accOverflows = accDiv[0];
        
        BigInteger subsecondNext = subsecond.add(accOverflows.multiply(subSecondAddend));
        BigInteger[] ssDiv = subsecondNext.divideAndRemainder(SUBSECOND_OVERFLOW);
        this.subsecond = ssDiv[1];
        BigInteger ssOverflows = ssDiv[0];
        
        this.second = second.add(ssOverflows);
        if (this.second.compareTo(SECONDS_IN_100_YEARS) > 0) {
            throw new RuntimeException("Simulation can't count higher than 100 years.");
        }
        
    }
    
    public BigInteger getTimeNanoseconds() {
        return second.multiply(SUBSECOND_OVERFLOW).add(subsecond);
    }
    
    public String toString() {
        return second.toString() + "s " + subsecond.toString() + "ns";
        
    }
}
