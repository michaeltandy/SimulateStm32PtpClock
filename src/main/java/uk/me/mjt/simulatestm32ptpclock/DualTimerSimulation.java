
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigInteger;


public class DualTimerSimulation {
    
    private final long CLK1PERIOD = 180;
    private final long CLK8PERIOD = 10000;
    
    private long comboClockValue = 0;
    private long overflowCounter = 0;
    
    public int adjustmentClockCyclesPerSecond = 0;
    
    public BigInteger clockBy(final BigInteger rtcClockCycles) {
        adjustmentClockCyclesPerSecond = Math.max(-50000, Math.min(50000, adjustmentClockCyclesPerSecond));
        int[] adjustmentSequence = generateAdjustmentSequence(adjustmentClockCyclesPerSecond);
        
        long rtcClockCyclesRemaining = rtcClockCycles.longValueExact();
        
        while (rtcClockCyclesRemaining > 0) {
            int thisCycleIdx = (int)(overflowCounter % 100);
            int adjustmentThisCycle = adjustmentSequence[thisCycleIdx];
            
            long comboClockPeriod = CLK1PERIOD*CLK8PERIOD+adjustmentThisCycle;
            
            if (comboClockValue+rtcClockCyclesRemaining < comboClockPeriod) {
                comboClockValue += rtcClockCyclesRemaining;
                rtcClockCyclesRemaining = 0;
            } else {
                long clocksToNextOverflow = comboClockPeriod - comboClockValue;
                overflowCounter++;
                comboClockValue = 0;
                rtcClockCyclesRemaining -= clocksToNextOverflow;
            }
        }
        
        return toBI(overflowCounter * CLK8PERIOD + comboClockValue / CLK1PERIOD);
    }
    
    public static int[] generateAdjustmentSequence(int adjustmentClockCyclesPerSecond) {
        int baseAdjustment = Math.floorDiv(adjustmentClockCyclesPerSecond, 100);
        int remainder1 = adjustmentClockCyclesPerSecond - 100 * baseAdjustment;
        int tenthsToAdd = Math.floorDiv(remainder1, 10);
        int hundredthsToAdd = remainder1 - 10 * tenthsToAdd;
        
        int[] result = new int[100];
        for (int i=0 ; i<100 ; i++) {
            result[i] = baseAdjustment +
                    (i % 10 < tenthsToAdd ? 1 : 0) +
                    (i % 10 == 9 && Math.floorDiv(i, 10) < hundredthsToAdd ? 1 : 0);
        }
        
        return result;
    }
    
    public BigInteger getTimeMicroseconds() {
        return toBI(overflowCounter * CLK8PERIOD + comboClockValue / CLK1PERIOD);
    }
    
    public BigInteger getTimeNanoseconds() {
        return getTimeMicroseconds().multiply(Constants.ONE_THOUSAND);
    }
    
    private static BigInteger toBI(long l) {
        return BigInteger.valueOf(l);
    }
}
