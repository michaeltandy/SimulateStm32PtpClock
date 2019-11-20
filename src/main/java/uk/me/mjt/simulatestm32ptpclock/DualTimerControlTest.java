
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class DualTimerControlTest {
    public static void main(String[] args) {
        //double[] chosenPD = selectGoodPD();
        //double[] chosenPD = {0.098,6.397}; // Good with +-1000us rcom time jitter
        //double[] chosenPD = {0.148,8.261}; // Good with +-60us rcom time jitter
        //double[] chosenPD = {10.671, 0.1, 6.834};
        //double[] chosenPD = {5,0.10,0.1};
        //double[] chosenPD = {37.9, 0.4, 11.68};
        // [25.433333333333337, 0.33290656912056116, 12.522222222222236]
        
        System.out.println("Simulation with selected P/D values:\n");
        
        //FeedbackController sim = makeController(chosenPD);
        FeedbackController sim = new EstimateKalman();
        
        List<TimeStepResult> example = simulateFeedback(sim, 30000);
        System.out.println("Score for this simulation: " +
                calculateScoreForSimulationOutput(example));
        System.out.println("Error range for this simulation: " +
                calculateRangeForSimulationOutput(example) + " us");
        
        XYChart.chartTimeStepResults(example);
        /*for (TimeStepResult tsr : example) {
            System.out.println(tsr.trueTimeNanoseconds + "\t"
                    + tsr.biasedCrystalTimeNanoseconds + "\t"
                    + tsr.ptpClockTimeNanoseconds + "\t"
                    + tsr.getPtpClockErrorNanos() + "\t"
                    + tsr.controlOutput + "\t"
                    + tsr.controllerNote);
        }*/
    }
    
    public static List<TimeStepResult> simulateFeedback(FeedbackController sim, int crystalBiasPpb) {
        BiasedCrystalSimulation crystal = new BiasedCrystalSimulation(Constants.ONE_HUNDRED_EIGHTY_MHZ);
        crystal.setBiasPartsPerBillion(new BigInteger(""+crystalBiasPpb));

        DualTimerSimulation dualTimer = new DualTimerSimulation();
        dualTimer.adjustmentClockCyclesPerSecond = 0;
        
        ArrayList<TimeStepResult> result = new ArrayList();
        
        for (int second = 0; second < 60*60*5; second++) {
            int timeDependentCrystalBiasPpb = (int)Math.round(17000.0/120.0*Math.sin(Math.PI * second / 9000));
            crystal.setBiasPartsPerBillion(new BigInteger(""+(crystalBiasPpb+timeDependentCrystalBiasPpb)));
            
            BigInteger clocks = crystal.clockBySeconds(BigDecimal.ONE);
            dualTimer.clockBy(clocks);

            TimeStepResult tsr = new TimeStepResult();
            tsr.trueTimeNanoseconds = crystal.getTrueTimeNanoseconds();
            tsr.biasedCrystalTimeNanoseconds = crystal.getBiasedTimeNanoseconds();
            tsr.ptpClockTimeNanoseconds = dualTimer.getTimeNanoseconds();
            
            //BigInteger randomNoiseNs = BigInteger.ZERO;//= new BigInteger(Main.randbetween(-1000, 1000) + "000");
            BigInteger randomNoiseNs = new BigInteger(Main.randbetween(-50000, 50000) + "");
            BigInteger controlOutput = sim.updateDataGetNewControlOutput(crystal.trueTimeSeconds,
                    dualTimer.getTimeNanoseconds().add(randomNoiseNs));
            
            tsr.controlOutput = controlOutput;
            tsr.controllerNote = sim.getNote();
            result.add(tsr);
            
            int adjustmentPartsPPB = controlOutput.intValueExact();
            dualTimer.adjustmentClockCyclesPerSecond = (180*adjustmentPartsPPB)/1000;
        }
        
        return result;
    }
    
    private static FeedbackController makeController(double[] parameters) {
        //return new FeedbackPIDTwoStep(35.80, 0.300, 0.5, parameters[0], 0.3, parameters[1]);
        return new FeedbackPIDWithIIR(parameters[0], parameters[1], parameters[2], 0.1);
    }
    
    private static int numberOfParameters() {
        for (int i=0 ; i<=5 ; i++) {
            double[] test = new double[i];
            try {
                makeController(test);
                return i;
            } catch (ArrayIndexOutOfBoundsException e) {}
        }
        throw new RuntimeException("Too many paramters?! :(");
    }
    
    private static double[] selectGoodPD() {
        double bestValuesSoFar[] = new double[numberOfParameters()];
        long bestScore = Long.MAX_VALUE;
        
        //double[] scales = {10.0, 3.0, 1.0, 0.3, 0.1, 0.03, 0.01};
        double[] scales = {30, 10.0, 3.0, 1.0, 0.3, 0.1, 0.03, 0.01};
        
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
        
        int[] crystalBiasPpb = {-150000, -30000, -5000, 0, 5000, 30000, 150000};
        
        for (int bias : crystalBiasPpb) {
            FeedbackController sim = makeController(parameters);
            overallScore += calculateScoreForSimulationOutput(simulateFeedback(sim, bias));
        }
        
        return overallScore;
    }
    
    static long calculateScoreForSimulationOutput(List<TimeStepResult> simulationOutput) {
        long rmsDeviationAfterSomeTime = 0;
        //long maxDeviationAfterSomeTime = 0;
        //long lowestUndershoot = 0;
        for (TimeStepResult tsr : simulationOutput) {
            //lowestUndershoot = Math.min(lowestUndershoot, tsr.getPtpClockErrorNanos());
            if (tsr.getTrueTimeSeconds() > 10 * 60) {
                long error = Math.abs(tsr.getPtpClockErrorNanos() / 1000);
                //maxDeviationAfterSomeTime = Math.max(maxDeviationAfterSomeTime, error);
                rmsDeviationAfterSomeTime += error * error;
            }
        }
        return rmsDeviationAfterSomeTime;
    }
    
    static long calculateRangeForSimulationOutput(List<TimeStepResult> simulationOutput) {
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (TimeStepResult tsr : simulationOutput) {
            if (tsr.getTrueTimeSeconds() > 15 * 60) {
                long errorMicros = (tsr.getPtpClockErrorNanos() / 1000);
                min = Math.min(min, errorMicros);
                max = Math.max(max, errorMicros);
            }
        }
        return max-min;
    }
}
