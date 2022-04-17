import java.util.Map;
import java.util.HashMap;

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

    public void forwardReferences(In in, int loadptr, HashMap<String, Integer> label) {
        Finder empty_line      = new Finder("^[ \t]*$");
        Finder comment_line    = new Finder("^([#]|[/][/])");
        H.setLoggingPrefix("FORWARD REFERENCE");
        // Potential Forward References
        Finder dwordlabel_line = new Finder("^([0-9A-Fa-f]{4})[ \t]*([0-9A-Fa-f]{4})[ \t]([A-Z]+$)");
        Finder pragma_line     = new Finder("^(PRAGMA)[ \t](STRING|MEMORY|HERE)[ \t]*([0-9A-Za-z]+)[ \t]([A-Z]+$)*");
        while (in.hasNextLine()) {
            String line = in.readLine();
            if (empty_line.matches(line)) continue;
            if (comment_line.matches(line)) continue;
            if (dwordlabel_line.matches(line)) {
                HW.putHM(label,dwordlabel_line.get(3), loadptr);
                loadptr++;
                loadptr++;
                continue;
            }
            if (pragma_line.matches(line)) {
                if (pragma_line.get(2).equals("STRING")) {
                    for (int i=0;i<pragma_line.get(3).length();i++) loadptr++;
                }
                if (pragma_line.get(2).equals("MEMORY")) {
                    loadptr=H.fromHex(pragma_line.get(3));
                    HW.putHM(label,pragma_line.get(4), loadptr);
                }
                if (pragma_line.get(2).equals("HERE")) {
                    HW.putHM(label,pragma_line.get(3), loadptr);
                }
                continue;
            }
                loadptr++;
                loadptr++;
        }
        H.setLoggingPrefix("");
    }


    private static void putHM(HashMap<String, Integer> hm, String name, int value) {
                H.Log("PUTHM",name + " = " + value);
                hm.put(name,value);
    }

    public void loadMemoryWithProgram(In in, int loadptr, StringBuffer programAsRead, HashMap<String, Integer> label) {
        int PAGESIZE = 64;
        programAsRead.delete(0, programAsRead.length());

       /****************************************************************
        *  Read Program File
        *  Read in memory location and instruction.         
        ****************************************************************/
        Finder empty_line      = new Finder("^[ \t]*$");
        Finder comment_line    = new Finder("^([#]|[/][/])");
       
        Finder dword_line      = new Finder("^([0-9A-Fa-f]{4})[ \t]*([0-9A-Fa-f]{4}$)");
        Finder dwordlabel_line = new Finder("^([0-9A-Fa-f]{4})[ \t]*([0-9A-Fa-f]{4})[ \t]*([A-Z]+$)");

        Finder word_line       = new Finder("^([0-9A-Fa-f]{4}$)");
        Finder wordlabel_line  = new Finder("^([0-9A-Fa-f]{4})[ \t]*([A-Z]+)");

        Finder pragma_line     = new Finder("^(PRAGMA)[ \t](STRING|MEMORY|HERE)[ \t]*([0-9A-Za-z]+)[ \t]([A-Z]+$)*");
        Finder pagesize_line   = new Finder("^(PAGESIZE)[ \t]*([0-9]*)");

        Finder assembly_line   = new Finder("^([A-Z]{3})[ \t]+([0-9A-Fa-f]{2})[ \t]+([0-9A-Fa-f]{4})");
        Finder assembly_line2  = new Finder("^([A-Z]{3})([0-9A-Fa-f]{2})[ \t]*([0-9A-Fa-f]{4})");
        Finder assembly_line3  = new Finder("^([A-Z]{3})[ \t]+([0-9A-Fa-f]{4})");
        Finder halt_line       = new Finder("^(HALT)[ \t]+(0000)");

        while (in.hasNextLine()) {
            // String ltrim = src.replaceAll("^\\s+", "");
            // String rtrim = src.replaceAll("\\s+$", "")
            String line = in.readLine().replaceAll("\\s+$", "").replaceAll("^\\s+", "");

            if (empty_line.matches(line)) continue;
            if (comment_line.matches(line)) continue;

            programAsRead.append(line + "\n");

            if (pagesize_line.matches(line)) {
                PAGESIZE = H.fromHex(pagesize_line.get(2));
                continue;
            }
// IN LOAD
            if (pragma_line.matches(line)) {
                H.Log("PRAGMA",line);
                H.Log("PRAGMA LINE1",pragma_line.get(1));
                H.Log("PRAGMA LINE2",pragma_line.get(2));
                H.Log("PRAGMA LINE3",pragma_line.get(3));
                H.Log("PRAGMA LINE4",pragma_line.get(4));
                if (pragma_line.get(2).equals("STRING")) {
                    for (int i=0;i<pragma_line.get(3).length();i++) mem[loadptr++] = pragma_line.get(3).charAt(i);
                }
                if (pragma_line.get(2).equals("MEMORY")) {
                    loadptr = H.fromHex(pragma_line.get(3));
                    programlines[loadptr] = line;
                    HW.putHM(label,pragma_line.get(4), loadptr);
                }
                if (pragma_line.get(2).equals("HERE")) {
                    HW.putHM(label,pragma_line.get(3), loadptr);
                }
                continue;
            }

            if (dwordlabel_line.matches(line)) {
                HW.putHM(label,dwordlabel_line.get(3), loadptr);
                mem[loadptr] = H.fromHex(dwordlabel_line.get(1));
                programlines[loadptr] = line;
                loadptr++;
                mem[loadptr] = H.fromHex(dwordlabel_line.get(2));
                loadptr++;
                continue;
            }
            if (dword_line.matches(line)) {
                mem[loadptr] = H.fromHex(dword_line.get(1));
                programlines[loadptr] = line;
                loadptr++;
                mem[loadptr] = H.fromHex(dword_line.get(2));
                loadptr++;
                continue;
            }

            // READ Regular Two Word Machine Instruction
            if (dword_line.matches(line)) {
                mem[loadptr] = H.fromHex(dword_line.get(1));
                programlines[loadptr] = line;
                loadptr++;
                mem[loadptr] = H.fromHex(dword_line.get(2));
                loadptr++;
                continue;
            }
            // READ Assembly Line version 1
            if (assembly_line.matches(line)) {
                // This re-expresses  (as an int) the lowword of the 2 word instruction pair
                mem[loadptr] = InstructionSet.assembly(assembly_line.get(1), assembly_line.get(2));
                programlines[loadptr] = line;
                loadptr++;
                mem[loadptr] = H.fromHex(assembly_line.get(3));
                programlines[loadptr] = line;
                loadptr++;
                continue;
            }
            // READ Assembly Line version 2
            if (assembly_line2.matches(line)) {
                mem[loadptr] = InstructionSet.assembly(assembly_line2.get(1), assembly_line2.get(2));
                programlines[loadptr] = line;
                loadptr++;
                mem[loadptr] = H.fromHex(assembly_line2.get(3));
                programlines[loadptr] = line;
                loadptr++;
                continue;
            }
            // READ Assembly Line version 3
            if (assembly_line3.matches(line)) {
                mem[loadptr] = InstructionSet.assembly(assembly_line3.get(1), "00");
                programlines[loadptr] = line;
                loadptr++;
                mem[loadptr] = H.fromHex(assembly_line3.get(2));
                programlines[loadptr] = line;
                loadptr++;
                continue;
            }
            // READ HALT
            if (halt_line.matches(line)) {
                mem[loadptr] = 0;
                programlines[loadptr] = line;
                loadptr++;
                mem[loadptr] = 0;
                programlines[loadptr] = line;
                loadptr++;
                continue;
            }
            if (word_line.matches(line)) {
                mem[loadptr] = H.fromHex(word_line.get(1));
                loadptr++;
                continue;
            }

        }
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
                                                                      
