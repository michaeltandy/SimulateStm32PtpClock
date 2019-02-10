
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigDecimal;
import java.math.BigInteger;

public class PtpClockSimulation {
    
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
        BigInteger[] accDiv = accumulatorNext.divideAndRemainder(Constants.TWO_POW_32);
        this.accumulator = accDiv[1];
        BigInteger accOverflows = accDiv[0];
        
        BigInteger subsecondNext = subsecond.add(accOverflows.multiply(subSecondAddend));
        BigInteger[] ssDiv = subsecondNext.divideAndRemainder(Constants.SUBSECOND_OVERFLOW);
        this.subsecond = ssDiv[1];
        BigInteger ssOverflows = ssDiv[0];
        
        this.second = second.add(ssOverflows);
        if (this.second.compareTo(Constants.SECONDS_IN_100_YEARS) > 0) {
            throw new RuntimeException("Simulation can't count higher than 100 years.");
        }
        
    }
    
    public BigInteger getTimeNanoseconds() {
        return second.multiply(Constants.SUBSECOND_OVERFLOW).add(subsecond);
    }
    
    public String toString() {
        return second.toString() + "s " + subsecond.toString() + "ns";
        
    }
}
