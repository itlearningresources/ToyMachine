public class Funger {

// Statics
//
    // write to an array of binary integers, 8 per line to standard output
    public static void showbinary(int[] a) {
        final int M = 64;
        final int C = 8;
        int count = (a.length >= M) ? M : a.length;
        StdOut.print(toHex(0) + ": ");
        for (int i = 0; i < count; i++) {
            StdOut.print(toBinary(a[i]) + " ");
            if (i % C == (C-1)) {
                StdOut.println();
                if (i+1 < count) StdOut.print(toHex(i+1) + ": ");
            }
        }
    }
    // return a 4-digit hex string corresponding to 16-bit integer n
    public static String toDec(int n) {
        return String.format("%05d", n & 0xFFFF);
    }
    public static String toDecShort(int n) {
        return String.format("%03d", n & 0xFFFF);
    }
    // return a 16-digit binary string corresponding to 16-bit integer n
    public static String toBinary(int n) {
        return String.format("%16s", Integer.toBinaryString(n & 0xFFFF)).replace(" ", "0");
    }

    // return a 4-digit hex string corresponding to 16-bit integer n
    public static String toHex(int n) {
        return String.format("%04X", n & 0xFFFF);
    }
    // return a 2-digit hex string corresponding to 8-bit integer n
    public static String toHexShort(int n) {
        return String.format("%02X", n & 0xFFFF);
    }

    // return a 16-bit integer corresponding to the 4-digit hex string s
    public static int fromHex(String s) {
        return Integer.parseInt(s, 16) & 0xFFFF;
    }
    // return the low order nibble of the low order byte of a 16-bit integer as a 16-bit integer
    public static int toLON(int n) { return n & 0xFFF0; }


    // return a 4-digit binary string corresponding to the low order nibble of a 16-bit integer n
    public static String toLONBinary(int n) {
        return String.format("%8s", Integer.toBinaryString( (n & 0xFF00) >> 8 ) ).replace(" ", "0");
    }

    // return a 8-digit binary string corresponding to the high order byte of a 16-bit integer n
    public static String toHOBinary(int n) {
        return String.format("%8s", Integer.toBinaryString( (n & 0xFF00) >> 8 ) ).replace(" ", "0");
    }
    // return a 8-digit binary string corresponding to the low order byte of a 16-bit integer n
    public static String toLOBinary(int n) {
        return String.format("%8s", Integer.toBinaryString( (n & 0x00FF) ) ).replace(" ", "0");
    }
    // return two 8-digit binary strings corresponding to 16-bit integer n
    public static String toByteBinary(int n) {
        return 
            String.format("%8s", Integer.toBinaryString( (n & 0xFF00) >> 8 ) ).replace(" ", "0") + " " +
            String.format("%8s", Integer.toBinaryString( (n & 0x00FF) ) ).replace(" ", "0") ;
    }
    // return four 4-digit binary strings corresponding to 16-bit integer n
    public static String toNibbleBinary(int n) {
        return 
            String.format("%4s", Integer.toBinaryString( (n & 0xF000) >> 12 ) ).replace(" ", "0") + " " +
            String.format("%4s", Integer.toBinaryString( (n & 0x0F00) >> 8 ) ).replace(" ", "0") + " " +
            String.format("%4s", Integer.toBinaryString( (n & 0x00F0) >> 4 ) ).replace(" ", "0") + " " +
            String.format("%4s", Integer.toBinaryString( (n & 0x000F) >> 0 ) ).replace(" ", "0");
    }
    // return four 1-digit hex strings corresponding to 16-bit integer n
    public static String toNibbleHex(int n) {
        return 
            String.format("%X", (n & 0xF000) >> 12) + " " +
            String.format("%X", (n & 0x0F00) >> 8)  + " " +
            String.format("%X", (n & 0x00F0) >> 4)  + " " +
            String.format("%X", (n & 0x000F) >> 0);
    }
    // return two 8-digit hex strings corresponding to 16-bit integer n
    public static String toByteHex(int n) {
        return 
            String.format("%02X", (n & 0xFF00) >> 8).replace(" ", "0") + " " +
            String.format("%02X", (n & 0x00FF) >> 0).replace(" ", "0");
    }
    // return two 8-digit hex strings corresponding to 16-bit integer n
    public static String toByteHex(int n, int f) {
        StringBuffer sb = new StringBuffer(32);
        final String sp = " ";
        String pad = "";
        if (f==4) pad = "  ";
        if (f==8) pad = "      ";
        return sb.append(pad).append(String.format("%02X", (n & 0xFF00) >> 8).replace(sp, "0")).append(sp)
                 .append(pad).append(String.format("%02X", (n & 0x00FF) >> 0).replace(sp, "0")).toString();
    }
}
