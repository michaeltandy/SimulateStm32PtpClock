package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigInteger;
import junit.framework.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

public class RtcSimulationTest {

    public RtcSimulationTest() {
    }

    @Test
    public void testOneMillionCycles() {
        BigInteger oneMillionClockCycles = new BigInteger("1000000");
        
        for (boolean CALP : new boolean[] { true, false} )
        {
            for (int CALM = 0 ; CALM<512 ; CALM++) {
                RtcSimulation instance = new RtcSimulation();
                instance.CALM = CALM;
                instance.CALP = CALP;
                instance.clockBy(oneMillionClockCycles);
                //System.out.println(CALP + "\t"+CALM+"\t"+instance.getTimeClocks());
                
                double expectedPpm = -0.95316*(CALM-(CALP?512:0));
                long seenPpm = instance.getTimeClocks().subtract(oneMillionClockCycles).longValueExact();
                Assert.assertEquals(null, expectedPpm, seenPpm, 1.4);
            }
        }
    }
    
    @Test
    public void testHundredMillionCycles() {
        BigInteger hundredMillionCycles = new BigInteger("100000000");
        
        for (boolean CALP : new boolean[] { true, false} )
        {
            for (int CALM = 0 ; CALM<512 ; CALM++) {
                RtcSimulation instance = new RtcSimulation();
                instance.CALM = CALM;
                instance.CALP = CALP;
                instance.clockBy(hundredMillionCycles);
                System.out.println(CALP + "\t"+CALM+"\t"+instance.getTimeClocks());
                
                double expectedPpm = -0.95316*(CALM-(CALP?512:0));
                long seenPpm = instance.getTimeClocks().subtract(hundredMillionCycles).longValueExact() / 100;
                Assert.assertEquals(null, expectedPpm, seenPpm, 1.4);
            }
        }
    }

    

}