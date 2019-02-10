
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigDecimal;
import java.math.BigInteger;


public class Constants {
    public static final BigInteger SUBSECOND_OVERFLOW = new BigInteger("1000000000");
    public static final BigInteger SECONDS_IN_100_YEARS = new BigInteger("3155760000");
    public static final BigInteger TWO_POW_32 = new BigInteger("2").pow(32);
    public static final BigDecimal CLOCK_RATE_HZ = new BigDecimal("180000000");
    public static final BigDecimal ONE_BILLION = new BigDecimal("1000000000");
    
    private Constants() {}
}
