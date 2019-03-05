
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


public class RtcControlTest {
    public static void main(String[] args) {
        
        System.out.println("0\t0\t0\t0");
        FeedbackLogicSimulator sim = new FeedbackLogicSimulator(-0.0000001,-0.00001, BigInteger.ZERO);
        for (TimeStepResult tsr : simulateFeedback(sim)) {
            System.out.println(tsr.trueTimeNanoseconds + "\t"
                    + tsr.biasedCrystalTimeNanoseconds + "\t"
                    + tsr.ptpClockTimeNanoseconds + "\t"
                    + tsr.getPtpClockErrorNanos() + "\t"
                    + tsr.controlOutput);
        }
    }
    
    private static List<TimeStepResult> simulateFeedback(FeedbackLogicSimulator sim) {
        BiasedCrystalSimulation crystal = new BiasedCrystalSimulation(Constants.TWO_MHZ);
        crystal.setBiasPartsPerBillion(new BigInteger("30000")); // 30ppm

        RtcSimulation rtc = new RtcSimulation();
        rtc.CALM = 0;
        rtc.CALP = false;
        
        ArrayList<TimeStepResult> result = new ArrayList();
        
        for (int second = 0; second < 60*20; second++) {
            BigInteger clocks = crystal.clockBySeconds(BigDecimal.ONE);
            rtc.clockBy(clocks);

            TimeStepResult tsr = new TimeStepResult();
            tsr.trueTimeNanoseconds = crystal.getTrueTimeNanoseconds();
            tsr.biasedCrystalTimeNanoseconds = crystal.getBiasedTimeNanoseconds();
            tsr.ptpClockTimeNanoseconds = rtc.getTimeNanoseconds();
            
            
            BigInteger randomNoiseNs = new BigInteger(Main.randbetween(-1000, 1000) + "000");
            BigInteger controlOutput = sim.updateDataGetNewControlOutput(crystal.trueTimeSeconds,
                    rtc.getTimeNanoseconds().add(randomNoiseNs));
            
            tsr.controlOutput = controlOutput;
            result.add(tsr);
            
            if (controlOutput.compareTo(BigInteger.ZERO) > 0) {
                rtc.CALP = true;
                rtc.CALM = controlOutput.intValueExact();
            } else {
                rtc.CALP = false;
                rtc.CALM = -controlOutput.intValueExact();
            }
        }
        
        return result;
    }
}
