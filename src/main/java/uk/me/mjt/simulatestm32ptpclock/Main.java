
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import static uk.me.mjt.simulatestm32ptpclock.Constants.*;

public class Main {
    
    private static final Random rand = new Random();

    public static void main(String[] args) {
        long lowestScoreSoFar = Long.MAX_VALUE;
        double bestP = Double.NaN;
        double bestD = Double.NaN;
        for (double P : between(-0.000817-0.0001, -0.000817+0.0001, 100)) {
            for (double D : between(-0.049-0.01, -0.049+0.01, 100)) {
                long score = evaluatePD(P, D);
                if (score < lowestScoreSoFar) {
                    lowestScoreSoFar = score;
                    bestP = P;
                    bestD = D;
                    System.out.printf("Good performance for P=%.6f D=%.6f, score %d\n",P,D,score);
                }
            }
        }
        
        System.out.println("Simulation with selected P/D values:\n");
        printCsv(bestP, bestD);
        
    }
    
    private static void printCsv(double P, double D) {
        System.out.println("0\t0\t0\t0");
        FeedbackLogicSimulator sim = new FeedbackLogicSimulator(P,D);
        for (TimeStepResult tsr : simulateFeedback(sim)) {
            System.out.println(tsr.trueTimeNanoseconds + "\t"
                    + tsr.biasedCrystalTimeNanoseconds + "\t"
                    + tsr.ptpClockTimeNanoseconds + "\t"
                    + tsr.getPtpClockErrorNanos());
        }
    }
    
    private static List<TimeStepResult> simulateFeedback(FeedbackLogicSimulator sim) {
        BiasedCrystalSimulation crystal = new BiasedCrystalSimulation();
        crystal.setBiasPartsPerBillion(new BigInteger("30000")); // 30ppm

        PtpClockSimulation ptpClock = new PtpClockSimulation();

        ptpClock.subSecondAddend = new BigInteger("20");
        ptpClock.addend = sim.updateDataGetNewAddend(BigDecimal.ZERO, BigInteger.ZERO);
        
        ArrayList<TimeStepResult> result = new ArrayList();
        
        for (int ts = 0; ts < 600; ts++) {
            BigInteger clocks = crystal.clockBySeconds(BigDecimal.ONE);
            ptpClock.clockBy(clocks);

            TimeStepResult tsr = new TimeStepResult();
            tsr.trueTimeNanoseconds = crystal.getTrueTimeNanoseconds();
            tsr.biasedCrystalTimeNanoseconds = crystal.getBiasedTimeNanoseconds();
            tsr.ptpClockTimeNanoseconds = ptpClock.getTimeNanoseconds();
            result.add(tsr);
            
            BigInteger randomNoise = new BigInteger(randbetween(-1000, 1000) + "000");
            BigInteger newAddend = sim.updateDataGetNewAddend(crystal.trueTimeSeconds,
                    ptpClock.getTimeNanoseconds().add(randomNoise));
            ptpClock.addend = newAddend;
        }
        
        return result;
    }
    
    // Good performance for P=-0.000817 D=-0.049242
    // Good performance for P=-7.999999999999996E-4 D=-0.03300000000000002
    public static long evaluatePD(double P, double D) {
        long overallScore = 0;
        
        long lowestUndershoot = 0;
        long maxDeviationAfterTwoMins = 0;
        
        for (int repeat=0 ; repeat<10 ; repeat++) {
            FeedbackLogicSimulator sim = new FeedbackLogicSimulator(P, D);
            
            for (TimeStepResult tsr : simulateFeedback(sim)) {
                lowestUndershoot = Math.min(lowestUndershoot, tsr.getPtpClockErrorNanos());
                if (tsr.getTrueTimeSeconds() > 300) {
                    maxDeviationAfterTwoMins = Math.max(maxDeviationAfterTwoMins,
                            Math.abs(tsr.getPtpClockErrorNanos()));
                }
            }
            
            overallScore += Math.abs(lowestUndershoot)/10 + Math.abs(maxDeviationAfterTwoMins);
        }
        
        return overallScore;
    }
    
    private static class TimeStepResult {
        BigInteger trueTimeNanoseconds;
        BigInteger biasedCrystalTimeNanoseconds;
        BigInteger ptpClockTimeNanoseconds;
        long getPtpClockErrorNanos() {
            return ptpClockTimeNanoseconds.subtract(trueTimeNanoseconds).longValueExact();
        }
        long getTrueTimeSeconds() {
            return trueTimeNanoseconds.divide(ONE_BILLION.toBigInteger()).longValueExact();
        }
    }
    
    /*private static int noiseModelMicroseconds() {
        switch(rand.nextInt(10)) {
            case 0: return randbetween(-87191, -57233);
            case 1: return randbetween(-57233, -42825);
            case 2: return randbetween(-42825, -28556);
            case 3: return randbetween(-28556, -15462);
            case 4: return randbetween(-15462, 0);
            case 5: return randbetween(0, 18614);
            case 6: return randbetween(18614, 37641);
            case 7: return randbetween(37641, 53420);
            case 8: return randbetween(53420, 84295);
            case 9: return randbetween(84295, 293568);
        }
        throw new RuntimeException("Should never get here?");
    }*/

    private static int randbetween(int low, int high) {
        return rand.nextInt(high-low)+low;
    }
    
    private static Iterable<Double> between(final double low, final double high, final int count) {
        if (high == low) throw new RuntimeException("high!=low");
        if (count < 2) throw new RuntimeException("count must be >= 2");
        if (low<high) return between(high, low, count);
        
        ArrayList<Double> result = new ArrayList(count);
        double step = (high-low)/(count-1);
        double next = low;
        for (int i=0 ; i<count ; i++) {
            result.add(next);
            next += step;
        }
        return Collections.unmodifiableList(result);
    }
    
}
