
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigInteger;


public class DualTimerSimulation {
    
    private final BigInteger CLK1PERIOD = new BigInteger("180");
    private final BigInteger CLK8PERIOD = new BigInteger("10000");
    
    private BigInteger comboClockValue = new BigInteger("0");
    private long overflowCounter = 0;
    
    public int cyclesAdjustmentPerClk8Overflow = 0;
    
    public BigInteger clockBy(final BigInteger rtcClockCycles) {
        BigInteger comboClockPeriod = CLK1PERIOD
                .multiply(CLK8PERIOD)
                .add(toBI(cyclesAdjustmentPerClk8Overflow));
        
        BigInteger comboClockValueAdded = comboClockValue.add(rtcClockCycles);
        
        
        int completeRollovers = comboClockValueAdded.divide(comboClockPeriod).intValueExact();
        overflowCounter += completeRollovers;
        
        comboClockValue = comboClockValueAdded.mod(comboClockPeriod);
        
        BigInteger timeMicroseconds = toBI(overflowCounter)
                .multiply(CLK8PERIOD)
                .add(comboClockValue.divide(CLK1PERIOD));
        // TODO could simulate this more precisely...
        
        return timeMicroseconds;
    }
    
    public BigInteger getTimeMicroseconds() {
        return toBI(overflowCounter)
                .multiply(CLK8PERIOD)
                .add(comboClockValue.divide(CLK1PERIOD));
    }
    
    public BigInteger getTimeNanoseconds() {
        return getTimeMicroseconds().multiply(Constants.ONE_THOUSAND);
    }
    
    private static BigInteger toBI(long l) {
        return BigInteger.valueOf(l);
    }
}
