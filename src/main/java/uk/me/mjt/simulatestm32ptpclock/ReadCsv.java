
package uk.me.mjt.simulatestm32ptpclock;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ReadCsv {
    public static class FileLine {
        public long trueTimeMicros;
        public int measuredError;
        public int smoothedError;
    }
    
    
    public static List<FileLine> readCsv() throws IOException {
        String file = "error-summary.csv";
        ArrayList<FileLine> result = new ArrayList();
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] s = line.split(",");
                FileLine fl = new FileLine();
                fl.trueTimeMicros = Long.parseLong(s[0]);
                fl.measuredError = Integer.parseInt(s[1]);
                fl.smoothedError = Integer.parseInt(s[2]);
                result.add(fl);
            }
        }
        
        return Collections.unmodifiableList(result);
    }

}
