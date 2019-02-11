# Java STM32 PTP Clock Simulation

This is a Java simulation of the PTP clock peripheral on the ethernet port of the STM32F427 (and probably other STM32 ICs).

Being in Java lets us use BigDecimal arbritrary-precision math, which is useful as precision timing can involve some big numbers.

There's also a simulation of a clock source with a bias, and a PD control loop; and a coarse-fine system to choose P and D constants.

### License

Entirely under the MIT license.
