public final class H {

    public final static void assertion(boolean b, String sz) {
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


    public final static void ABEND(boolean b) { if(b) ABEND(); }
    public final static void ABEND() {
        try {
            throw new HException("H Exception from KILLER");
        } catch (HException e) {
             System.out.println("Caught Exception: "+ e.getMessage());
             e.printStackTrace();
             System.exit(1);
        }
    }
    public final static int fromHex(String s)    { return Integer.parseInt(s, 16) & 0xFFFF; }
    public final static String toHex(int n)      { return String.format("%04X", n & 0xFFFF); }
    public final static String toHexShort(int n) { return String.format("%02X", n & 0xFFFF); }

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

