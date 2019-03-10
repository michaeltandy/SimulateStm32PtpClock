
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
        double[] chosenPD = selectGoodPD();
        
        System.out.println("Simulation with selected P/D values:\n");
        printCsv(chosenPD[0], chosenPD[1]);
    }
    
    private static void printCsv(double P, double D) {
        System.out.println("0\t0\t0\t0");
        FeedbackLogicSimulator sim = new FeedbackLogicSimulator(P,D, new BigInteger("1193046471"));
        for (TimeStepResult tsr : simulateFeedback(sim)) {
            System.out.println(tsr.trueTimeNanoseconds + "\t"
                    + tsr.biasedCrystalTimeNanoseconds + "\t"
                    + tsr.ptpClockTimeNanoseconds + "\t"
                    + tsr.getPtpClockErrorNanos());
        }
    }
    
    private static List<TimeStepResult> simulateFeedback(FeedbackLogicSimulator sim) {
        BiasedCrystalSimulation crystal = new BiasedCrystalSimulation(ONE_HUNDRED_EIGHTY_MHZ);
        crystal.setBiasPartsPerBillion(new BigInteger("30000")); // 30ppm

        PtpClockSimulation ptpClock = new PtpClockSimulation();

        ptpClock.subSecondAddend = new BigInteger("20");
        ptpClock.addend = sim.updateDataGetNewControlOutput(BigDecimal.ZERO, BigInteger.ZERO);
        
        ArrayList<TimeStepResult> result = new ArrayList();
        
        for (int second = 0; second < 60*20; second++) {
            BigInteger clocks = crystal.clockBySeconds(BigDecimal.ONE);
            ptpClock.clockBy(clocks);

            TimeStepResult tsr = new TimeStepResult();
            tsr.trueTimeNanoseconds = crystal.getTrueTimeNanoseconds();
            tsr.biasedCrystalTimeNanoseconds = crystal.getBiasedTimeNanoseconds();
            tsr.ptpClockTimeNanoseconds = ptpClock.getTimeNanoseconds();
            result.add(tsr);
            
            BigInteger randomNoiseNs = new BigInteger(randbetween(-1000, 1000) + "000");
            BigInteger newAddend = sim.updateDataGetNewControlOutput(crystal.trueTimeSeconds,
                    ptpClock.getTimeNanoseconds().add(randomNoiseNs));
            
            ptpClock.addend = newAddend;
        }
        
        return result;
    }
    
    private static double[] selectGoodPD() {
        double bestP = -0.000817;
        double bestD = -0.049;
        long bestScore = Long.MAX_VALUE;
        
        double[] scales = {1.0, 0.3, 0.1, 0.03, 0.01, 0.003, 0.001};
        
        for (double s : scales) {
            double pLow = bestP-0.05*s;
            double pHigh = Math.min(bestP+0.05*s, 0);
            double dLow = bestD-0.1*s;
            double dHigh = Math.min(bestD+0.1*s, 0);
            for (double P : between(pLow, pHigh, 20)) {
                for (double D : between(dLow, dHigh, 20)) {
                    if (P<-0.0001 && D<-0.0001) {
                        long score = evaluatePD(P, D);
                        if (score < bestScore) {
                            bestScore = score;
                            bestP = P;
                            bestD = D;
                            System.out.printf("Good performance for P=%.6f D=%.6f, score %d\n",P,D,score);
                        }
                    }
                }
            }
            double percentOfRangeP = 100*(bestP-pLow)/(pHigh-pLow);
            System.out.printf("Best P %.6f for range %.6f to %.6f, %.1f%%\n",bestP,pLow,pHigh,percentOfRangeP);
            double percentOfRangeD = 100*(bestD-dLow)/(dHigh-dLow);
            System.out.printf("Best D %.6f for range %.6f to %.6f, %.1f%%\n",bestD,dLow,dHigh, percentOfRangeD);
        }
        
        double[] result = {bestP, bestD};
        return result;
    }
    
    // Good performance for P=-0.000817 D=-0.049242
    // Good performance for P=-7.999999999999996E-4 D=-0.03300000000000002
    public static long evaluatePD(double P, double D) {
        long overallScore = 0;
        
        long lowestUndershoot = 0;
        long maxDeviationAfterSomeTime = 0;
        
        for (int repeat=0 ; repeat<10 ; repeat++) {
            FeedbackLogicSimulator sim = new FeedbackLogicSimulator(P, D, new BigInteger("1193046471"));
            
            for (TimeStepResult tsr : simulateFeedback(sim)) {
                lowestUndershoot = Math.min(lowestUndershoot, tsr.getPtpClockErrorNanos());
                if (tsr.getTrueTimeSeconds() > 10*60) {
                    maxDeviationAfterSomeTime = Math.max(maxDeviationAfterSomeTime,
                            Math.abs(tsr.getPtpClockErrorNanos()));
                }
            }
            
            //overallScore += Math.abs(lowestUndershoot) + Math.abs(maxDeviationAfterTwoMins);
            overallScore += Math.abs(maxDeviationAfterSomeTime);
        }
        
        return overallScore;
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

    public static int randbetween(int low, int high) {
        return rand.nextInt(high-low)+low;
    }
    
    public static Iterable<Double> between(final double low, final double high, final int count) {
        if (high == low) {
            ArrayList<Double> result = new ArrayList();
            result.add(low);
            return Collections.unmodifiableList(result);
        }
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
