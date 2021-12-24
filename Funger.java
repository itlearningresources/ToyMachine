public class Funger {
    private int n;
    private StringBuffer sb = new StringBuffer(32);
    private MyStringBuffer mysb = new MyStringBuffer(32);
    private final String sp = " ";
    private final String pad2 = "  ";
    private final String pad6 = "      ";
    private final String zero= "0";

    public Funger(int n) {
        this.n = n;
    }
    public static Funger build(int n) {
        return new Funger(n);
    }
    public static Funger build() {
        return new Funger(0);
    }
    public Funger set(int n) {
        this.n = n;
        return this;
    }

    // return the low order nibble of the low order byte of a 16-bit integer as a 16-bit integer
    public int toLON() { return n & 0xFFF0; }

    // return a 4-digit binary string corresponding to the low order nibble of a 16-bit integer n
    public String toLONBinary() {
        return String.format("%8s", Integer.toBinaryString( (n & 0xFF00) >> 8 ) ).replace(sp, zero);
    }

    // return a 8-digit binary string corresponding to the high order byte of a 16-bit integer n
    public String toHOBinary() {
        return String.format("%8s", Integer.toBinaryString( (n & 0xFF00) >> 8 ) ).replace(sp, zero);
    }
    // return a 8-digit binary string corresponding to the low order byte of a 16-bit integer n
    public String toLOBinary() {
        return String.format("%8s", Integer.toBinaryString( (n & 0x00FF) ) ).replace(sp, zero);
    }
    // return two 8-digit binary strings corresponding to 16-bit integer n
    public String toByteBinary() {
        return mysb.clear()
                   .appendz(String.format("%8s", Integer.toBinaryString( (n & 0xFF00) >> 8 ) ))
                   .space()
                   .appendz(String.format("%8s", Integer.toBinaryString( (n & 0x00FF) ) ))
                   .toString();
    }
    // return four 4-digit binary strings corresponding to 16-bit integer n
    public String toNibbleBinary() {
        return 
            String.format("%4s", Integer.toBinaryString( (n & 0xF000) >> 12 ) ).replace(sp, zero) + sp +
            String.format("%4s", Integer.toBinaryString( (n & 0x0F00) >> 8 ) ).replace(sp, zero) + sp +
            String.format("%4s", Integer.toBinaryString( (n & 0x00F0) >> 4 ) ).replace(sp, zero) + sp +
            String.format("%4s", Integer.toBinaryString( (n & 0x000F) >> 0 ) ).replace(sp, zero);
    }
    // return four 1-digit hex strings corresponding to 16-bit integer n
    public String toNibbleHex() {
        return 
            String.format("%X", (n & 0xF000) >> 12) + sp +
            String.format("%X", (n & 0x0F00) >> 8)  + sp +
            String.format("%X", (n & 0x00F0) >> 4)  + sp +
            String.format("%X", (n & 0x000F) >> 0);
    }
    // return two 8-digit hex strings corresponding to 16-bit integer n
    public String toByteHex() {
        sb.delete(0, sb.length());
        return sb.append(String.format("%02X", (n & 0xFF00) >> 8).replace(sp, zero)).append(sp)
                 .append(String.format("%02X", (n & 0x00FF) >> 0).replace(sp, zero)).toString();
    }
    // return two 8-digit hex strings corresponding to 16-bit integer n
    public String toByteHex4() {
        sb.delete(0, sb.length());
        return sb.append(pad2).append(String.format("%02X", (n & 0xFF00) >> 8).replace(sp, zero)).append(sp)
                 .append(pad2).append(String.format("%02X", (n & 0x00FF) >> 0).replace(sp, zero)).toString();
    }
    public String toByteHex8() {
        sb.delete(0, sb.length());
        return sb.append(pad6).append(String.format("%02X", (n & 0xFF00) >> 8).replace(sp, zero)).append(sp)
                 .append(pad6).append(String.format("%02X", (n & 0x00FF) >> 0).replace(sp, zero)).toString();
    }


// Statics
//

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


        public static void main(String[] args) {
            double[][] fees={{3.00, 3.50}, {6.35, 7.35}, {9.00, 5.00}};

            System.out.println(fees.length);
            System.out.println(fees[0].length);
            int n = 65012;
            Funger f = new Funger(n);
            System.out.println( toByteBinary(n) );
            System.out.println( f.toByteBinary() );
            System.out.println( toByteHex(n,8) );
            System.out.println( toNibbleBinary(n) );
            System.out.println( toByteHex(n,4) );
            System.out.println( toNibbleHex(n) );
        }


}
class MyStringBuffer {
    private StringBuffer sb;
    private final String sp = " ";
    private final String pad2 = "  ";
    private final String pad6 = "      ";
    private final String zero= "0";
    public MyStringBuffer() {
        sb = new StringBuffer(32);
    }
    public MyStringBuffer(int n) {
        sb = new StringBuffer(n);
    }
    public MyStringBuffer space() {
        this.sb.append(sp);
        return this;
    }
    public MyStringBuffer append(String sz) {
        this.sb.append(sz);
        return this;
    }
    public MyStringBuffer appendz(String sz) {
        this.sb.append(sz.replace(sp,zero));
        return this;
    }
    public MyStringBuffer clear() {
        this.sb.delete(0, sb.length());
        return this;
    }
    public MyStringBuffer set(String sz) {
        this.sb.delete(0, sb.length());
        this.sb.append(sz);
        return this;
    }
    public String toString() {
        return this.sb.toString();
    }
}
