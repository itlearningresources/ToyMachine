import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;    
import java.io.FileWriter;   // Import the FileWriter class
import java.io.IOException;  // Import the IOException class to handle errors
public class CrashLogger extends Logger {
    public CrashLogger(String szPrefix) {
        super(szPrefix);
    }
    public CrashLogger(String szPrefix, String fn) {
        super(szPrefix, fn);
    }
    public void log(Exception e) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
        LocalDateTime now = LocalDateTime.now();  
        try {
          String szOut = "";
          FileWriter myWriter = new FileWriter(getFileName(),true);
          szOut = ANSI.EOL + "***********************";myWriter.write(szOut + "\n"); System.out.println(szOut);
          szOut = ANSI.EOL + "*** Crash Logger       ";myWriter.write(szOut + "\n"); System.out.println(szOut);
          szOut = ANSI.EOL + "*** " + dtf.format(now); myWriter.write(szOut + "\n"); System.out.println(szOut);
          szOut = ANSI.EOL + "***********************";myWriter.write(szOut + "\n"); System.out.println(szOut);
          szOut = ANSI.EOL + "Caught Exception: "+ e.getMessage();myWriter.write(szOut + "\n"); System.out.println(szOut);
          StackTraceElement[] arr = e.getStackTrace();
          String spacing = "";
          for ( int i =0;i<arr.length;i++) {
              szOut = ANSI.EOL + spacing + arr[i].toString();myWriter.write(szOut + "\n"); System.out.println(szOut);
              spacing = "  ";
          }
          myWriter.close();
          System.exit(1);
        } catch (IOException fatal) {
          System.out.println(ANSI.EOL + "Fatal Logging Error");
          fatal.printStackTrace();
          System.exit(1);
        }
    }

}


