 
 public class HW {
 
    private int pc;                             // program counter
    private int indexregister;                  // index register
    private int stkptr;                         // stack pointer
    final int ADRR = 0x01;
    // private Registers R = new Registers(reg);

    private final int REGSIZE = 0x0010;          // Number of registers
    private int[] reg   = new int[REGSIZE];      //
    public HW initRegs() {
        for(int i=0;i<REGSIZE;i++) reg[i] = 0;
        return this;
    }
    private final int MEMSIZE = 0xFFFF;                     // memory size
    private int[] mem        = new int[MEMSIZE];            // main memory locations
    private int[] memflags   = new int[MEMSIZE];            // main memory flags
    private String[] programlines   = new String[MEMSIZE];  // program lines (text) as read)
    private boolean[] breakpoint   = new boolean[MEMSIZE];  // breakpoint boolean
    
    private final int STACKSIZE = 32;            // stack size
    private int[] stk   = new int[STACKSIZE];    // stack memory locations
    public HW initStk() {
        for (int i=0;i<STACKSIZE;i++) stk[i] = 0;
        stkptr = 0;
        return this;
    }

    public int getRegisterCount() {
        return REGSIZE;
    }
    public int getPC() {
        return this.pc;
    }
    public int setPC(int n) {
        this.pc = n;
        return this.pc;
    }
    public int getIndexRegister() {
        return this.indexregister;
    }
    public int setIndexRegister(int n) {
        this.indexregister = n;
        return this.indexregister;
    }

    public int[] getReg() {
        return this.reg;
    }
    public String[] getProgramLines() {
        return this.programlines;
    }
    public int[] getMem() {
        return this.mem;
    }
    public int[] getMemFlags() {
        return this.memflags;
    }
    public boolean[] getBrk() {
        return this.breakpoint;
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
    public void stkPushHex(String sz) {
        int n = H.fromHex(sz);
        this.stkptr++;
        this.stk[stkptr] = n;
    }
    public void stkPush(int n) {
        this.stkptr++;
        this.stk[stkptr] = n;
    }
    public String stkTopHex() {
        return H.toHex(this.stk[stkptr]);
    }
    public int stkTop() {
        return this.stk[stkptr];
    }
    public int stkPop() {
        int n = this.stk[stkptr];
        this.stk[stkptr] = 0xFFFF;
        this.stkptr--;
        if (this.stkptr<0) this.stkptr=0;
        return n;
    }
    public String stkPopHex() {
        int n = this.stk[stkptr];
        this.stk[stkptr] = 0xFFFF;
        this.stkptr--;
        if (this.stkptr<0) this.stkptr=0;
        return H.toHex(n);
    }
    public int incStkPtr(int n) {
        this.stkptr++;
        return this.stkptr;
    }
    public int decStkPtr(int n) {
        this.stkptr--;
        if (this.stkptr<0) this.stkptr=0;
        return this.stkptr;
    }

    public int stkGet(int index) {
        return this.stk[index];
    }
    public String stkGetHex(int index) {
        return H.toHex(this.stk[index]);
    }
    public boolean setBreakPoint(int n) {
        breakpoint[n] = true;
        return breakpoint[n];
    }
    public boolean clearBreakPoint(int n) {
        breakpoint[n] = false;
        return breakpoint[n];
    }
    public boolean flipBreakPoint(int n) {
        breakpoint[n] = !breakpoint[n];
        return breakpoint[n];
    }
 
     public HW() {
         super();
         stkptr=0;
         for (int i=0;i<MEMSIZE;i++) breakpoint[i] = false;
         for (int i=0;i<MEMSIZE;i++) memflags[i] = 0;
         for (int i=0;i<MEMSIZE;i++) memflags[i] = 0x9999;
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
                                                                      
