
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class DualTimerControlTest {
    public static void main(String[] args) {
        double[] chosenPD = selectGoodPD();
        //double[] chosenPD = {0.098,6.397}; // Good with +-1000us rcom time jitter
        //double[] chosenPD = {0.148,8.261}; // Good with +-60us rcom time jitter
        //double[] chosenPD = {0.15,0,1};
        
        System.out.println("Simulation with selected P/D values:\n");
        
        System.out.println("0\t0\t0\t0\t0");
        FeedbackController sim = new FeedbackPID(chosenPD[0], chosenPD[1], chosenPD[2]);
        for (TimeStepResult tsr : simulateFeedback(sim)) {
            System.out.println(tsr.trueTimeNanoseconds + "\t"
                    + tsr.biasedCrystalTimeNanoseconds + "\t"
                    + tsr.ptpClockTimeNanoseconds + "\t"
                    + tsr.getPtpClockErrorNanos() + "\t"
                    + tsr.controlOutput);
        }
    }
    
    private static List<TimeStepResult> simulateFeedback(FeedbackController sim) {
        BiasedCrystalSimulation crystal = new BiasedCrystalSimulation(Constants.ONE_HUNDRED_EIGHTY_MHZ);
        crystal.setBiasPartsPerBillion(new BigInteger("-500"));

        DualTimerSimulation dualTimer = new DualTimerSimulation();
        dualTimer.adjustmentClockCyclesPerSecond = 0;
        
        ArrayList<TimeStepResult> result = new ArrayList();
        
        for (int second = 0; second < 60*20; second++) {
            BigInteger clocks = crystal.clockBySeconds(BigDecimal.ONE);
            dualTimer.clockBy(clocks);

            TimeStepResult tsr = new TimeStepResult();
            tsr.trueTimeNanoseconds = crystal.getTrueTimeNanoseconds();
            tsr.biasedCrystalTimeNanoseconds = crystal.getBiasedTimeNanoseconds();
            tsr.ptpClockTimeNanoseconds = dualTimer.getTimeNanoseconds();
            
            //BigInteger randomNoiseNs = BigInteger.ZERO;//= new BigInteger(Main.randbetween(-1000, 1000) + "000");
            BigInteger randomNoiseNs = new BigInteger(Main.randbetween(-60000, 60000) + "");
            BigInteger controlOutput = sim.updateDataGetNewControlOutput(crystal.trueTimeSeconds,
                    dualTimer.getTimeNanoseconds().add(randomNoiseNs));
            
            tsr.controlOutput = controlOutput;
            result.add(tsr);
            
            dualTimer.adjustmentClockCyclesPerSecond = controlOutput.intValueExact();
        }
        
        return result;
    }
    
    private static double[] selectGoodPD() {
        double bestValuesSoFar[] = {0.1, 0.1, 0.1};
        long bestScore = Long.MAX_VALUE;
        
        double[] scales = {10.0, 3.0, 1.0, 0.3, 0.1, 0.03, 0.01};
        
        for (double s : scales) {
            System.out.printf("Checking with scale %f\n", s);
            for (double[] candidate : generateCandidates(bestValuesSoFar, s)) {
                long score = evaluateParameters(candidate);
                if (score < bestScore) {
                    bestScore = score;
                    bestValuesSoFar = candidate;
                    System.out.printf("Good performance for %s, score %d\n", Arrays.toString(candidate), score);
                }
            }
        }
        return bestValuesSoFar;
    }
    
    private static Iterable<double[]> generateCandidates(double[] baseline, double scale) {
        double[][] betweenArrays = new double[baseline.length][10];
        for (int i=0 ; i<baseline.length ; i++) {
            List<Double> ld = Main.between(Math.max(0,baseline[i]-scale), Math.max(0,baseline[i]+scale), 10);
            for (int j=0 ; j<10 ; j++) {
                betweenArrays[i][j] = ld.get(j);
            }
        }
        
        ArrayList<double []> result = new ArrayList<>();
        
        for (int i=0 ; i<(int)Math.pow(10, baseline.length); i++) {
            int remainder=i;
            double[] thisEntry = new double[baseline.length];
            for (int j=0 ; j<baseline.length ; j++) {
                thisEntry[j] = betweenArrays[j][remainder%10];
                remainder = remainder/10;
            }
            result.add(thisEntry);
        }
        
        return result;
    }
    
    // With zero noise, good performance for P=0.023211 D=0.140000, score 220000
    // With noise and output/100, good performance for P=0.038318 D=2.291667, score 4218000
    public static long evaluateParameters(double[] parameters) {
        long overallScore = 0;
        
        long lowestUndershoot = 0;
        long maxDeviationAfterSomeTime = 0;
        long rmsDeviationAfterSomeTime = 0;
        
        for (int repeat=0 ; repeat<10 ; repeat++) {
            FeedbackController sim = new FeedbackPID(parameters[0], parameters[1], parameters[2]);
            
            for (TimeStepResult tsr : simulateFeedback(sim)) {
                lowestUndershoot = Math.min(lowestUndershoot, tsr.getPtpClockErrorNanos());
                if (tsr.getTrueTimeSeconds() > 10*60) {
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
