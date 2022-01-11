 
 public class HW {
 
    private int pc;                              // program counter
    private int stkptr;                           // stack pointer
    final int ADRR = 0x01;
    // private Registers R = new Registers(reg);

    private final int REGSIZE = 0x0010;          // stack size in memory locations
    private int[] reg   = new int[REGSIZE];      // 16 registers
    public void initRegs() {
        for(int i=0;i<REGSIZE;i++) reg[i] = 0;
    }
    private final int MEMSIZE = 0xFFFF;          // stack size in memory locations
    private int[] mem   = new int[MEMSIZE];      // main memory locations
    
    private final int STACKSIZE = 32;            // stack size in memory locations
    private int[] stk   = new int[STACKSIZE];    // stack memory locations

    public int getPC() {
        return this.pc;
    }
    public int[] getReg() {
        return this.reg;
    }
    public int[] getMem() {
        return this.mem;
    }
    public int[] getStk() {
        return this.stk;
    }
    public String getStkPtrHex() {
        return H.toHex(this.stkptr);
    }
    public int getStkPtr() {
        return this.stkptr;
    }
    public void setStkPtr(int n) {
        this.stkptr = n;
    }
    public void StkPushHex(String sz) {
        int n = H.fromHex(sz);
        this.stkptr++;
        this.stk[stkptr] = n;
    }
    public void StkPush(int n) {
        this.stkptr++;
        this.stk[stkptr] = n;
    }
    public String StkTopHex() {
        return H.toHex(this.stk[stkptr]);
    }
    public int StkTop() {
        return this.stk[stkptr];
    }
    public int StkPop() {
        int n = this.stk[stkptr];
        this.stkptr--;
        return n;
    }
    public String StkPopHex() {
        int n = this.stk[stkptr];
        this.stkptr--;
        return H.toHex(n);
    }
    public int incStkPtr(int n) {
        this.stkptr++;
        return this.stkptr;
    }
    public int decStkPtr(int n) {
        this.stkptr--;
        return this.stkptr;
    }

 
     public HW() {
         super();
         stkptr=0;
     }
 
 
     public String toString() {
         String szRet = "";
 
         return szRet;
     }
 
 
     public static void main (String args[]) {
         int i = 0;
         int j = 0;
         String sz = "";
 
         System.out.println("Hello World!");
 
     }
 
 
 }
                                                                      
