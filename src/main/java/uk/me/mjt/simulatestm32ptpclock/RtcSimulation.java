
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;


public class RtcSimulation {
    
    public int CALM = 0;
    public boolean CALP = false;
    
    public BigInteger cal_cnt = BigInteger.ZERO;
    private BigInteger timeClocks = BigInteger.ZERO;
    private BigInteger PREDIV_A = new BigInteger("4");
    
    private static final NavigableSet<Integer> addedPulsesForCalp = getAddedPulsesForCalpBit();
    
    public BigInteger clockBy(final BigInteger rtcClockCycles) {
        final NavigableSet<Integer> maskedPulseSet = maskedPulseSetForCalm();
        final NavigableSet<Integer> addedPulseSet = CALP ? addedPulsesForCalp : Collections.<Integer>emptyNavigableSet();
        long countOfPulsesMasked = 0;
        BigInteger overflowThreshold = Constants.TWO_POW_20
                .add(toBI(maskedPulseSet.size()))
                .subtract(toBI(addedPulseSet.size()));
        
        long completeRollovers = rtcClockCycles.divide(overflowThreshold).intValueExact();
        countOfPulsesMasked += completeRollovers * maskedPulseSet.size();
        countOfPulsesMasked -= addedPulseSet.size();
        
        BigInteger calCntAfter = cal_cnt.add(rtcClockCycles).mod(overflowThreshold);
        if (calCntAfter.compareTo(cal_cnt) > 0) {
            countOfPulsesMasked += maskedPulseSet
                    .subSet(cal_cnt.intValueExact(), false, calCntAfter.intValueExact(), true)
                    .size();
            countOfPulsesMasked -= addedPulseSet
                    .subSet(cal_cnt.intValueExact(), false, calCntAfter.intValueExact(), true)
                    .size();
        } else {
            countOfPulsesMasked += maskedPulseSet
                    .subSet(cal_cnt.intValueExact(), false, overflowThreshold.intValueExact(), true)
                    .size();
            countOfPulsesMasked -= addedPulseSet
                    .subSet(cal_cnt.intValueExact(), false, overflowThreshold.intValueExact(), true)
                    .size();
            countOfPulsesMasked += maskedPulseSet
                    .subSet(0, false, cal_cnt.intValueExact(), true)
                    .size();
            countOfPulsesMasked -= addedPulseSet
                    .subSet(0, false, cal_cnt.intValueExact(), true)
                    .size();
        }
        
        BigInteger rtcClockCyclesAdjusted = rtcClockCycles.subtract(BigInteger.valueOf(countOfPulsesMasked));
        timeClocks = timeClocks.add(rtcClockCyclesAdjusted);
        
        //System.out.println("Input clock " + rtcClockCycles + " adjusted to " + rtcClockCyclesAdjusted);
        cal_cnt = calCntAfter;
        return rtcClockCyclesAdjusted;
    }
    
    private NavigableSet<Integer> maskedPulseSetForCalm() {
        TreeSet<Integer> result = new TreeSet();
        for (int i=0 ; i<=8 ; i++) {
            if (isBitSet(CALM, i)) {
                result.addAll(maskedPulsesForCalmBit(i));
            }
        }
        return Collections.unmodifiableNavigableSet(result);
    }
    
    private static boolean isBitSet(int value, int bit) {
        return (value & 1<<bit) != 0;
    }
    
    private static BigInteger toBI(long l) {
        return BigInteger.valueOf(l);
    }
    
    private static boolean greaterThan(BigInteger A, BigInteger B) {
        return A.compareTo(B) > 0;
    }
    
    public BigInteger getTimeNanoseconds() {
        return timeClocks.divide(PREDIV_A).multiply(Constants.ONE_THOUSAND).multiply(new BigInteger("2"));
    }
    
    private static NavigableSet<Integer> getAddedPulsesForCalpBit() {
        TreeSet<Integer> result = new TreeSet();
        result.addAll(maskedPulsesForCalmBit(9));
        return Collections.unmodifiableNavigableSet(result);
    }
    
    private static List<Integer> maskedPulsesForCalmBit(int calmBit) {
        ArrayList<Integer> result = new ArrayList();
        
        int suffix = 1 << (19-calmBit);

        for (int j=0 ; j<Math.pow(2, calmBit) ; j++) {
            int prefix = (j << (20-calmBit));
            result.add(suffix+prefix);
        }
        
        return result;
    }
    
    public static void main(String[] args) {
        RtcSimulation ri = new RtcSimulation();
        //ri.clockBy(new BigInteger("1000000000"));
        
        //ri.CALM = 511;
        //ri.clockBy(new BigInteger("1000000000"));
        
        /*for (int calm = 0 ; calm <= 8 ; calm++) {
            System.out.print("CALM["+calm+"]: ");
            for (Integer i : ri.maskedPulsesForCalmBit(calm)) {
                System.out.printf("%x ", i);
            }
            System.out.println();
        }*/
        
    }

}
