
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


public class DualTimerControlTest {
    public static void main(String[] args) {
        
        double[] chosenPD = selectGoodPD();
        //double[] chosenPD = {0.098,6.397}; // Good with +-1000us rcom time jitter
        //double[] chosenPD = {0.148,8.261}; // Good with +-60us rcom time jitter
        
        System.out.println("Simulation with selected P/D values:\n");
        
        System.out.println("0\t0\t0\t0\t0");
        //FeedbackMicrosecondBased sim = new FeedbackMicrosecondBased(0.001, 0.01);
        FeedbackMicrosecondBased sim = new FeedbackMicrosecondBased(chosenPD[0], chosenPD[1]);
        for (TimeStepResult tsr : simulateFeedback(sim)) {
            System.out.println(tsr.trueTimeNanoseconds + "\t"
                    + tsr.biasedCrystalTimeNanoseconds + "\t"
                    + tsr.ptpClockTimeNanoseconds + "\t"
                    + tsr.getPtpClockErrorNanos() + "\t"
                    + tsr.controlOutput);
        }
    }
    
    private static List<TimeStepResult> simulateFeedback(FeedbackMicrosecondBased sim) {
        BiasedCrystalSimulation crystal = new BiasedCrystalSimulation(Constants.ONE_HUNDRED_EIGHTY_MHZ);
        crystal.setBiasPartsPerBillion(new BigInteger("30000")); // 30ppm

        DualTimerSimulation dualTimer = new DualTimerSimulation();
        dualTimer.cyclesAdjustmentPerClk8Overflow = 0;
        
        ArrayList<TimeStepResult> result = new ArrayList();
        
        for (int second = 0; second < 60*20; second++) {
            BigInteger clocks = crystal.clockBySeconds(BigDecimal.ONE);
            dualTimer.clockBy(clocks);

            TimeStepResult tsr = new TimeStepResult();
            tsr.trueTimeNanoseconds = crystal.getTrueTimeNanoseconds();
            tsr.biasedCrystalTimeNanoseconds = crystal.getBiasedTimeNanoseconds();
            tsr.ptpClockTimeNanoseconds = dualTimer.getTimeNanoseconds();
            
            //BigInteger randomNoiseNs = BigInteger.ZERO;//= new BigInteger(Main.randbetween(-1000, 1000) + "000");
            BigInteger randomNoiseNs = new BigInteger(Main.randbetween(-60, 60) + "000");
            BigInteger controlOutput = sim.updateDataGetNewControlOutput(crystal.trueTimeSeconds,
                    dualTimer.getTimeNanoseconds().add(randomNoiseNs));
            
            tsr.controlOutput = controlOutput;
            result.add(tsr);
            
            dualTimer.cyclesAdjustmentPerClk8Overflow = controlOutput.intValueExact();
        }
        
        return result;
    }
    
    private static double[] selectGoodPD() {
        double bestP = 0.1;
        double bestD = 0.5;
        long bestScore = Long.MAX_VALUE;
        
        double[] scales = {30.0, 10.0, 3.0, 1.0, 0.3, 0.1, 0.03, 0.01, 0.003};
        
        for (double s : scales) {
            double pLow = Math.max(bestP-0.1*s, 0);
            double pHigh = Math.max(bestP+0.1*s, 0);
            double dLow = Math.max(bestD-0.5*s, 0);
            double dHigh = Math.max(bestD+0.5*s, 0);
            for (double P : Main.between(pLow, pHigh, 10)) {
                for (double D : Main.between(dLow, dHigh, 10)) {
                    if (P>0 && D>0) {
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
            System.out.printf("Best P %.10f for range %.10f to %.10f, %.1f%%\n",bestP,pLow,pHigh,percentOfRangeP);
            double percentOfRangeD = 100*(bestD-dLow)/(dHigh-dLow);
            System.out.printf("Best D %.10f for range %.10f to %.10f, %.1f%%\n",bestD,dLow,dHigh, percentOfRangeD);
        }
        
        double[] result = {bestP, bestD};
        return result;
    }
    
    // With zero noise, good performance for P=0.023211 D=0.140000, score 220000
    // With noise and output/100, good performance for P=0.038318 D=2.291667, score 4218000
    public static long evaluatePD(double P, double D) {
        long overallScore = 0;
        
        long lowestUndershoot = 0;
        long maxDeviationAfterSomeTime = 0;
        long rmsDeviationAfterSomeTime = 0;
        
        for (int repeat=0 ; repeat<10 ; repeat++) {
            FeedbackMicrosecondBased sim = new FeedbackMicrosecondBased(P, D);
            
            for (TimeStepResult tsr : simulateFeedback(sim)) {
                lowestUndershoot = Math.min(lowestUndershoot, tsr.getPtpClockErrorNanos());
                if (tsr.getTrueTimeSeconds() > 3*60) {
                    long error = Math.abs(tsr.getPtpClockErrorNanos()/1000);
                    maxDeviationAfterSomeTime = Math.max(maxDeviationAfterSomeTime,error);
                    rmsDeviationAfterSomeTime += error*error;
                }
            }
            
            //overallScore += Math.abs(lowestUndershoot) + Math.abs(maxDeviationAfterSomeTime);
            overallScore += rmsDeviationAfterSomeTime;
            //overallScore += Math.abs(maxDeviationAfterSomeTime);
        }
        
        return overallScore;
    }
}
