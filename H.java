
import java.io.FileWriter;   // Import the FileWriter class
import java.io.IOException;  // Import the IOException class to handle errors
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;    

public final class H {
    final static String BAR = " | ";
    private static String loggingPrefix = "";

    public static void Log(int n) {
        Log(String.valueOf(n));
    }

    public static String LPad6(String sz)  { return String.format("%-6s", sz).replace(' ', ' ');}
    public static String LPad8(String sz)  { return String.format("%-8s", sz).replace(' ', ' ');}
    public static String LPad12(String sz) { return String.format("%-12s", sz).replace(' ', ' ');}
    public static String LPad24(String sz) { return String.format("%-24s", sz).replace(' ', ' ');}
    public static String LPad32(String sz) { return String.format("%-32s", sz).replace(' ', ' ');}
    public static String LPad36(String sz) { return String.format("%-36s", sz).replace(' ', ' ');}
    public static String LPad44(String sz) { return String.format("%-44s", sz).replace(' ', ' ');}
    public static String LPad48(String sz) { return String.format("%-48s", sz).replace(' ', ' ');}
    public static String LPad64(String sz) { return String.format("%-64s", sz).replace(' ', ' ');}
    public static String Pad6(String sz)   { return String.format("%6s", sz).replace(' ', ' ');}
    public static String Pad8(String sz)   { return String.format("%8s", sz).replace(' ', ' ');}
    public static String Pad12(String sz)  { return String.format("%12s", sz).replace(' ', ' ');}
    public static String Pad24(String sz)  { return String.format("%24s", sz).replace(' ', ' ');}
    public static String Pad32(String sz)  { return String.format("%32s", sz).replace(' ', ' ');}
    public static String Pad36(String sz)  { return String.format("%36s", sz).replace(' ', ' ');}
    public static String Pad48(String sz)  { return String.format("%48s", sz).replace(' ', ' ');}
    public static String Pad44(String sz)  { return String.format("%44s", sz).replace(' ', ' ');}
    public static String Pad64(String sz)  { return String.format("%64s", sz).replace(' ', ' ');}

    public static void LogFlag(String sz) { Log("*** " + sz); }
    public static void FlagLog(String sz) { Log("*** " + sz); }

    public static void setLoggingPrefix(String sz) {
        loggingPrefix = sz;
    }
    public static void Log(String sz1, String sz2) {
        Log(sz1 + ": " + sz2);
    }
    public static void Log(String sz1, int n) {
        Log(sz1 + ": " + n);
    }

    public static void Log(String sz) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
        LocalDateTime now = LocalDateTime.now();  
        try {
          FileWriter myWriter = new FileWriter("/tmp/applog",true);
          myWriter.write(dtf.format(now) + " " + loggingPrefix + "  " + sz + "\n");
          myWriter.close();
        } catch (IOException e) {
          System.out.println("An error occurred.");
          e.printStackTrace();
        }
    }


    public final static String shorten(String sz, int n) { 
        n = (n<=sz.length()) ? n : sz.length();
        return sz.substring(0,n);
    }
    public final static String concat(String ... a) {
        StringBuffer sb = new StringBuffer();
        for (int i=0;i<a.length;i++) sb.append(a[i]);
        return sb.toString();
    }
    public final static boolean xmatch(String sz, String ... a) {
        boolean bRet = false;
        for (String item : a)
            if (sz.equals(item)) {
                bRet = true;
                break;
            }
        return bRet;
    }
    public final static void halt() {
        System.exit(0);
    }

    public final static String argumentsToString(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<args.length;i++) 
            sb.append((i==0) ? "" : " ").append("arg[").append(i).append("]=").append(args[i]);
        return sb.toString();
    }


    public final static void assertion(boolean b, String sz) {
        if (!b) {
            System.out.println();
            System.out.println(sz);
            System.exit(1);
        }
    }
    public final static void xassertion(boolean b, String sz) {
            final String c = "\n\033[K";
        if (!b) {
            System.out.print(c + "ASSERTION FAILED");
            System.out.print(c + sz);
            System.out.print(c + "ASSERTION FAILED\n");
            System.exit(1);
        }
    }
    public final static void assertion(boolean b, String sz, String szValue) {
            final String c = "\n\033[K";
        if (!b) {
            System.out.print(c + "ASSERTION FAILED");
            System.out.print(c + sz);
            System.out.print(c + "VALUE IS:    " + szValue);
            System.out.print(c + "ASSERTION FAILED\n");
            System.exit(1);
        }
    }

    public final static void STOP(boolean b) { if (b) System.exit(1); }
    public final static void STOP() {
            System.exit(1);
    }


    public final static void ABEND(boolean b, int n) { if(b) ABEND( n + ""); }
    public final static void ABEND(boolean b, String sz) { if(b) ABEND(sz); }
    public final static void ABEND(boolean b) { if(b) ABEND(""); }
    public final static void ABEND(String sz) {
        try {
            throw new HException(sz);
        } catch (HException e) {
             System.out.println("ABEND: "+ e.getMessage());
             e.printStackTrace();
             System.exit(1);
        }
    }
    public final static int fromHex(String s)    { 
        int nRet = 0;
        try {
            nRet = Integer.parseInt(s, 16) & 0xFFFF; 
        } catch (Exception e) {
            nRet = 0;
        } finally {
            return nRet;
        }

    }
    public final static String toHex2D(int n)    { return String.format("%02X", n & 0x00FF); }
    public final static String toHex(int n)      { return String.format("%04X", n & 0xFFFF); }
    public final static String toHexBlank(int n) { return (n>0) ? String.format("%04X", n & 0xFFFF) : "----";  }
    public final static String toHexShort(int n) { return String.format("%02X", n & 0xFFFF); }
    public final static String toHexNibble(int n) { return String.format("%1X", n & 0x000F); }

    public final static char intToHexChar(int N) {
           char rChar = 0;
           final int BASE= 16;
           if ((N %BASE) <10) rChar = (char)(N % BASE + '0');
           if ((N % BASE)==10) rChar = (char)('A');
           if ((N % BASE)==11) rChar = (char)('B');
           if ((N % BASE)==12) rChar = (char)('C');
           if ((N % BASE)==13) rChar = (char)('D');
           if ((N % BASE)==14) rChar = (char)('E');
           if ((N % BASE)==15) rChar = (char)('F');
           return rChar;
    }
    public final static char[] convertIntegerToCharArray(int N) {
        char[] arr = new char[4];
        int m = N;
        arr[0] = intToHexChar(((N >> 12) & 0x000F) % 16 );
        arr[1] = intToHexChar(((N >>  8) & 0x000F) % 16 );
        arr[2] = intToHexChar(((N >>  4) & 0x000F) % 16 );
        arr[3] = intToHexChar(((N >>  0) & 0x000F) % 16 );

        return (char[])arr;
    }

}

class HException extends Exception {
     public HException(String sz) {
         super(sz);
     }

}

