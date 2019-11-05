
package uk.me.mjt.simulatestm32ptpclock;


public class TwoByTwoMatrix {
    
    public static double[][] newMatrix(double a, double b, double c, double d) {
        return new double[][] {{a,b},{c,d}};
    }
    
    public static double[][] add(double[][] a, double[][] b) {
        int h = getHeight(a);
        int w = getWidth(a);
        REQUIRE(h == getHeight(b));
        REQUIRE(w == getWidth(b));
        
        double[][] result = new double[h][w];
        
        for (int i=0 ; i<h ; i++) {
            for (int j=0 ; j<w ; j++) {
                result[i][j] = a[i][j]+b[i][j];
            }
        }
        return result;
    }
    
    public static double[][] subtract(double[][] a, double[][] b) {
        int h = getHeight(a);
        int w = getWidth(a);
        REQUIRE(h == getHeight(b));
        REQUIRE(w == getWidth(b));
        
        double[][] result = new double[h][w];
        
        for (int i=0 ; i<h ; i++) {
            for (int j=0 ; j<w ; j++) {
                result[i][j] = a[i][j]-b[i][j];
            }
        }
        return result;
    }
    
    public static double[][] transpose(double[][] a) {
        int h = getHeight(a);
        int w = getWidth(a);
        
        double[][] result = new double[w][h];
        
        for (int i=0 ; i<h ; i++) {
            for (int j=0 ; j<w ; j++) {
                result[j][i] = a[i][j];
            }
        }
        return result;
    }
    
    public static double[][] multiply(double[][] a, double[][] b) {
        int hA = getHeight(a);
        int wA = getWidth(a);
        int hB = getHeight(b);
        int wB = getWidth(b);
        REQUIRE(wA == hB);
        
        double[][] result = new double[hA][wB];
        
        for (int i=0 ; i<hA ; i++) {
            for (int j=0 ; j<wB ; j++) {
                result[i][j] = 0;
                for (int k=0 ; k<wA ; k++) {
                    result[i][j] += a[i][k]*b[k][j];
                }
            }
        }
        return result;
    }
    
    public static double[][] invertTwoByTwo(double[][] mat) {
        REQUIRE(getHeight(mat) == 2);
        REQUIRE(getWidth(mat) == 2);
        
        double a = mat[0][0];
        double b = mat[0][1];
        double c = mat[1][0];
        double d = mat[1][1];
        
        double det = 1/(a*d-b*c);
        double[][] result = {{det*d, det*-b},{det*-c, det*a}};
        return result;
    }
    
    public static int getHeight(double[][] a) {
        return a.length;
    }
    
    public static int getWidth(double[][] a) {
        return a.length == 0 ? 0 : a[0].length;
    }
    
    private static void REQUIRE(boolean test) {
        if (!test)
            throw new RuntimeException("Requirement failed");
    }

}
