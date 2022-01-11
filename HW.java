 
 public class HW {
 
    private int pc;                              // program counter
    private int stkptr;                          // stack pointer
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
    public int getStkPtr() {
        return this.stkptr;
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

 
     public HW() {
         super();
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
                                                                      
