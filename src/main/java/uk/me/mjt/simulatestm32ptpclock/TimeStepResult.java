
package uk.me.mjt.simulatestm32ptpclock;

import java.math.BigInteger;


class TimeStepResult {
    BigInteger trueTimeNanoseconds;
    BigInteger biasedCrystalTimeNanoseconds;
    BigInteger ptpClockTimeNanoseconds;
    BigInteger controlOutput;
    String controllerNote = null;

    long getPtpClockErrorNanos() {
        return ptpClockTimeNanoseconds.subtract(trueTimeNanoseconds).longValueExact();
    }

    long getTrueTimeSeconds() {
        return trueTimeNanoseconds.divide(Constants.ONE_BILLION.toBigInteger()).longValueExact();
    }

}
