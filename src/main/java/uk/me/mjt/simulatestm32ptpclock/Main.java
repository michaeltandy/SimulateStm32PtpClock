
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;


public class Main {
    
    private static final Random rand = new Random(0);

    public static void main(String[] args) {
        int measurementNoiseMagnitudeNanos = 1000000; // 1ms noise
        
        BiasedCrystalSimulation crystal = new BiasedCrystalSimulation();
        crystal.setBiasPartsPerBillion(new BigInteger("30000"));
        
        PtpClockSimulation ptpClock = new PtpClockSimulation();
        
        FeedbackLogicSimulator sim = new FeedbackLogicSimulator();
        
        ptpClock.subSecondAddend = new BigInteger("20");
        ptpClock.addend = sim.updateDataGetNewAddend(BigDecimal.ZERO, BigInteger.ZERO);
        
        System.out.println("True time (ns)\tw/ crystal bias (ns)\tPTP clock time (ns)\tError (ns)");
        System.out.println(crystal.getTrueTimeNanoseconds() + "\t" 
                + crystal.getBiasedTimeNanoseconds() + "\t"
                + ptpClock.getTimeNanoseconds() + "\t"
                + ptpClock.getTimeNanoseconds().subtract(crystal.getTrueTimeNanoseconds()));
        
        for (int i=0 ; i<300 ; i++) {
            BigInteger clocks = crystal.clockBySeconds(BigDecimal.ONE);
            ptpClock.clockBy(clocks);
            
            System.out.println(crystal.getTrueTimeNanoseconds() + "\t" 
                + crystal.getBiasedTimeNanoseconds() + "\t"
                + ptpClock.getTimeNanoseconds() + "\t"
                + ptpClock.getTimeNanoseconds().subtract(crystal.getTrueTimeNanoseconds()));
            
            BigInteger randomNoise = randomBiasOfMagnitude(measurementNoiseMagnitudeNanos);
            BigInteger newAddend = sim.updateDataGetNewAddend(crystal.trueTimeSeconds,
                    ptpClock.getTimeNanoseconds().add(randomNoise));
            ptpClock.addend = newAddend;
            
            //System.out.println(crystal + " " + ptpClock + " addend " + newAddend);
        }
        
    }
    
    private static BigInteger randomBiasOfMagnitude(int magNanos) {
        return new BigInteger((rand.nextInt(2*magNanos)-magNanos)+"");
    }
    
}
