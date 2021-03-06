package uk.me.mjt.simulatestm32ptpclock;

import org.junit.Test;
import static org.junit.Assert.*;

public class MatrixTest {
    
    private static final double[][] SQUARE_1234 = {{1,2},{3,4}};
    private static final double[][] SQUARE_7654 = {{7,6},{5,4}};
    private static final double[][] ROW_123 = {{1,2,3}};
    private static final double[][] COL_123 = {{1},{2},{3}};
    
    @Test
    public void testAdd() {
        double[][] expResult = {{8,8},{8,8}};
        assertArrayEquals(expResult, Matrix.add(SQUARE_1234, SQUARE_7654));
    }
    
    @Test
    public void testAddB() {
        double[][] expResult = {{2,4},{6,8}};
        assertArrayEquals(expResult, Matrix.add(SQUARE_1234, SQUARE_1234));
    }

    @Test
    public void testSubtract() {
        double[][] expResult = {{6,4},{2,0}};
        assertArrayEquals(expResult, Matrix.subtract(SQUARE_7654, SQUARE_1234));
    }
    
    @Test
    public void testSubtractB() {
        double[][] expResult = {{0,0},{0,0}};
        assertArrayEquals(expResult, Matrix.subtract(SQUARE_1234, SQUARE_1234));
    }

    @Test
    public void testTranspose() {
        assertArrayEquals(ROW_123, Matrix.transpose(COL_123));
        assertArrayEquals(COL_123, Matrix.transpose(ROW_123));
    }

    @Test
    public void testMultiply() {
        double[][] expResult = {{17, 14}, {41, 34}};
        assertArrayEquals(expResult, Matrix.multiply(SQUARE_1234, SQUARE_7654));
    }
    
    @Test
    public void testMultiplyB() {
        double[][] expResult = {{14}};
        assertArrayEquals(expResult, Matrix.multiply(ROW_123, COL_123));
    }
    
    @Test
    public void testMultiplyC() {
        double[][] expResult = {{1, 2, 3}, {2, 4, 6}, {3, 6, 9}};
        assertArrayEquals(expResult, Matrix.multiply(COL_123, ROW_123));
    }

    @Test
    public void testInvertTwoByTwo() {
        double[][] expResult = {{-2, 1}, {3.0/2, -1.0/2}};
        assertArrayEquals(expResult, Matrix.invert(SQUARE_1234));
    }
}