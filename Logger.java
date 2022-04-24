import java.io.FileWriter;   // Import the FileWriter class
import java.io.IOException;  // Import the IOException class to handle errors
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;    

public class Logger {
    final static String BAR = " | ";
    private String loggingPrefix = "";
    private String fn ="/tmp/applog";

    public String getFileName() {
        return fn;
    }
    public Logger(String szPrefix) {
        loggingPrefix = szPrefix;
    }
    public Logger(String szPrefix, String fn) {
        this.loggingPrefix = szPrefix;
        this.fn = fn;
    }

    public void log(Exception e) {
    }
    public void log(int n) {
        log(String.valueOf(n));
    }

    public void setloggingPrefix(String sz) {
        loggingPrefix = sz;
    }
    public void log(String sz1, String sz2) {
        log(sz1 + ": " + sz2);
    }
    public void log(String sz1, int n) {
        log(sz1 + ": " + n);
    }
    public void logHex(String sz1, int n) {
        log(sz1 + ": 0x" + H.toHex(n));
    }

    public void log(String sz) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
        LocalDateTime now = LocalDateTime.now();  
        try {
          FileWriter myWriter = new FileWriter(fn,true);
          myWriter.write(dtf.format(now) + " " + loggingPrefix + "  " + sz + "\n");
          myWriter.close();
        } catch (IOException e) {
          System.out.println("An error occurred.");
          e.printStackTrace();
        }
    }


}

