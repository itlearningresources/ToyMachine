/*************************************************************************
 *  Attribution:  https://introcs.cs.princeton.edu/java/home/
 *
 *  Started with the source code from the princeton site and expanded it
 *  extensively.
 *
 *  Execution:    java TOY filename.toy 
 *  Dependencies: StdIn.java In.java Finder.java HW.java ANSI.java
 *                H.java (Helpers)
 *
 *************************************************************************/

import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;
public class TOY { 
    static int PAGESIZE=32;
    static HashMap<String, Integer> label = new HashMap<String, Integer>();
    static HashMap<String, Integer> pages = new HashMap<String, Integer>();
    static  StringBuffer programAsRead = new StringBuffer(1024);


    static  StringBuffer sb = new StringBuffer(120);
    static  Instruction lastInstruction = null;
    InstructionSet II = new InstructionSet();
    private final int STACKSIZE = 32;          // stack size in memory locations
    private int pc;                            // program counter
    private int original_pc;                   // program counter
    private int stkptr;                        // stack pointer

    private int[] xreg   = new int[16];           // 16 registers
    final int ADRR = 0x01;
    private Registers R = null;
    //private int[] stk   = new int[STACKSIZE];    // stack memory locations
    private HW hw = null;

    // return a 4-digit hex string corresponding to 16-bit integer n
    // return a 2-digit hex string corresponding to 8-bit integer n
    // return a 16-bit integer corresponding to the 4-digit hex string s
    public static final boolean HALT = true;
    public static final boolean DUMP = true;
    public static final boolean NOHALT = false;
    public static final boolean NODUMP = false;
    public TOY coreDump(boolean dump, boolean halt) { 
        if (dump) {
            showhex(hw.getMem(),0,256);
            for (String i : pages.keySet()) {
                StdOut.printf("%s\n", "" + i + " @ " + H.toHex(pages.get(i)));
                showhex(hw.getMem(), pages.get(i), PAGESIZE);
            }

            StdOut.println();
            StdOut.println(TOY.programAsRead.toString());
            if (halt) System.exit(1);
        }
        return this;
    }

    // create a new TOY VM and load with program from specified file
    public TOY(String filename) {
        this(filename, 0x10);
    }

    public TOY(String filename, int pc) {
        final int PROGRAMMEMORY=16;
        int loadptr=PROGRAMMEMORY;

        this.pc = pc;
        this.stkptr = 0;
        In in = new In(filename);


       /****************************************************************
        *  Create a Hardware Object
        ****************************************************************/
        hw = new HW();
        R = new Registers(hw.getReg());

        int[] mem = hw.getMem();
        for (int i=0;i<mem.length;i++) mem[i]=0;

       /****************************************************************
        *  Read Program File
        *  Read in memory location and instruction.         
        ****************************************************************/
        coreDump(NODUMP,HALT);
        Finder empty_line      = new Finder("^[ \t]*$");
        Finder comment_line    = new Finder("^([#]|[/][/])");
        Finder label_line      = new Finder("^(LAB)[ \t]*([0-9A-Za-z]{4})");
        Finder memory_line     = new Finder("^(MEM)[ \t]*([0-9A-Fa-f]{4})[ \t]*([#$][0-9A-Za-z]*)");
        Finder dword_line      = new Finder("^([0-9A-Fa-f]{4})[ \t]*([0-9A-Fa-f]{4})");
        Finder wordandpage_line= new Finder("^([0-9A-Fa-f]{4})[ \t]*(P[0-9]{1})");
        Finder word_line       = new Finder("^([0-9A-Fa-f]{4}$)");

        Finder string_line     = new Finder("^(TEXT|STRING)[ \t]*([0-9A-Za-z]*)");
        Finder page_line       = new Finder("^(PAGE)[ \t]*([0-9A-Fa-f]{2})[ \t]*([#$][0-9A-Za-z]*)");
        Finder pword_line      = new Finder("^(P[0-9]{2})");
        Finder pagesize_line   = new Finder("^(PAGESIZE)[ \t]*([0-9]*)");

        while (in.hasNextLine()) {
            String line = in.readLine();

            if (empty_line.matches(line)) continue;
            if (comment_line.matches(line)) continue;

            programAsRead.append(line + "\n");

            if (pagesize_line.matches(line)) {
                PAGESIZE = H.fromHex(pagesize_line.get(2));
                continue;
            }

            if (string_line.matches(line)) {
                for (int i=0;i<string_line.get2().length();i++)
                    mem[loadptr++] = string_line.get2().charAt(i);
                continue;
            }

            if (pword_line.matches(line)) {
                mem[loadptr] = H.fromHex(pword_line.get(1).substring(1)) * PAGESIZE;
                loadptr++;
                continue;
            }
            if (wordandpage_line.matches(line)) {
                mem[loadptr] = H.fromHex(wordandpage_line.get(1));
                loadptr++;
                mem[loadptr] = H.fromHex(wordandpage_line.get(2).substring(1)) * PAGESIZE;
                loadptr++;
                continue;
            }
            if (dword_line.matches(line)) {
                mem[loadptr] = H.fromHex(dword_line.get(1));
                loadptr++;
                mem[loadptr] = H.fromHex(dword_line.get(2));
                loadptr++;
                continue;
            }

            if (label_line.matches(line)) {
                label.put(label_line.get(2), loadptr);
                continue;
            }
            if (page_line.matches(line)) {
                loadptr = H.fromHex(page_line.get(2)) * PAGESIZE;
                label.put(page_line.get(3), loadptr);
                pages.put(page_line.get(3), loadptr);
                continue;
            }
            if (memory_line.matches(line)) {
                loadptr = H.fromHex(memory_line.get(2));
                //label.put(memory_line.get(3), loadptr);
                pages.put(memory_line.get(3), loadptr);
                continue;
            }
            if (dword_line.matches(line)) {
                mem[loadptr] = H.fromHex(dword_line.get(1));
                loadptr++;
                mem[loadptr] = H.fromHex(dword_line.get(2));
                loadptr++;
                continue;
            }
            if (word_line.matches(line)) {
                mem[loadptr] = H.fromHex(word_line.get(1));
                loadptr++;
                continue;
            }

        }

        coreDump(NODUMP,HALT);
    }

    public void memoryPane(Pane p) {
        memoryPane(p, 0x0000);
    } 

    public void memoryPane(Pane p, int offset) {
        p.buffer3();
        final int C = 16;

        int[] a = hw.getMem();
        int i = offset;
        StringBuffer sb = new StringBuffer();
        int count = (PAGESIZE < a.length) ? PAGESIZE : a.length;
        count =1024;

        sb.append(H.toHex(0+offset) + ": ");
        while (i < (count+offset) ) {
            if ( a[i] == 0 )
                sb.append(H.toHex(a[i]) + " ");
            else
                sb.append(H.toHex(a[i]) + " ");

             if ( (i+1) % 16 == 0 ) {
                 sb.append("   ||   ");
                 for (int j=(i-15);j<=i;j++) sb.append( (a[j] < 127 && a[j] > 31) ? Character.toString((char) a[j]) : ".");
                 p.putquiet(sb.toString());
                 sb.delete(0, sb.length());
                 sb.append(H.toHex(i+1) + ": ");
             }
            i++;
        }
                p.putquiet(sb.toString());
                sb.delete(0, sb.length());
                p.buffer1();
   }
    public void memorydump(int[] a, int offset, Pane p) {
        int count = (256 < a.length) ? 256 : a.length;
        int i = offset;
        p.reset();
        while (i < (count+offset) ) {
            p.putquiet(H.toHex(i) + ": " + H.toHex(a[i]));
            i++;
        }
        p.refresh(0);
   }
    public void showhexp(int[] a, int offset, int override,Pane p) {
        final int C = 16;
        int i = offset;
        StringBuffer sb = new StringBuffer();
        int count = (PAGESIZE < a.length) ? PAGESIZE : a.length;
        count =0;
        if ( override > 0) count = (override < a.length) ? override : a.length;

        sb.append(ANSI.PURPLE + H.toHex(0+offset) + ": " + ANSI.RESET);
        while (i < (count+offset) ) {
            if ( a[i] == 0 )
                sb.append(H.toHex(a[i]) + " ");
            else
                sb.append(ANSI.DATA + H.toHex(a[i]) + ANSI.RESET + " ");

             if ( (i+1) % 16 == 0 ) {
                 sb.append("   ||   ");
                 for (int j=(i-15);j<=i;j++) sb.append( (a[j] < 127 && a[j] > 31) ? Character.toString((char) a[j]) : ".");
                 p.put(sb.toString());
                 sb.delete(0, sb.length());
                 sb.append(ANSI.PURPLE + H.toHex(i+1) + ": " + ANSI.RESET);
             }
            i++;
        }
                p.put(sb.toString());
                sb.delete(0, sb.length());
   }
    // write to an array of hex integers
    public static void showhex(int[] a, int offset, int override) {
        final int C = 16;
        int i = offset;
        int count = (PAGESIZE < a.length) ? PAGESIZE : a.length;
        if ( override > 0) count = (override < a.length) ? override : a.length;

        StdOut.print(ANSI.PURPLE + H.toHex(0+offset) + ": " + ANSI.RESET);
        while (i < (count+offset) ) {
            if ( a[i] == 0 )
                StdOut.print(H.toHex(a[i]) + " ");
            else
                StdOut.print(ANSI.DATA + H.toHex(a[i]) + ANSI.RESET + " ");

             if ( (i+1) % 16 == 0 ) {
                 StdOut.print("  ");
                 for (int j=(i-15);j<=i;j++) StdOut.print( (a[j] < 127 && a[j] > 31) ? Character.toString((char) a[j]) : ".");
                 StdOut.println("");
                 if ( i+1 < (count+offset) ) StdOut.print(ANSI.PURPLE + H.toHex(i+1) + ": " + ANSI.RESET);
             }
            i++;
        }
        StdOut.println();
    }

    public static void showreg(int[] a) {
        String sz = "";
        int count = a.length;
        count = 8;
        for (int i = 0; i < count; i++) {
            sz = (a[i] == 0) ? ANSI.RESET + H.toHex(a[i]) : ANSI.DATA + H.toHex(a[i]) + ANSI.RESET;
            StdOut.print(ANSI.PURPLE + "R" + H.toHexShort(i) + ": " + sz  + " ");
            if (i % 8 == 7) StdOut.println();
        }
    }
    public  void showstatev(Pane p2) {
        String sz = "";
        p2.reset();
        int[] a = hw.getReg();
        int count = 8;
        p2.put("PC : " + ANSI.RESET + H.toHex(pc));
        for (int i = 0; i < count; i++) {
            sz = (a[i] == 0) ? ANSI.RESET + H.toHex(a[i]) : ANSI.DATA + H.toHex(a[i]) + ANSI.RESET;
            p2.put("R" + H.toHexShort(i) + ": " + sz  + "");
        }
        p2.put("");
            p2.put("SP : " + hw.getStkPtrHex()  + "");
        for (int i = 0; i < count; i++) {
            sz = (hw.stkGet(i) == 0) ? ANSI.RESET + hw.stkGetHex(i) : ANSI.DATA + hw.stkGetHex(i) + ANSI.RESET;
            p2.put(H.toHex2D(i) + " : " + sz  + "");
        }
    }
    public  static void showstate(int[] a, int pc) {
        String sz = "";
        int count = a.length;
        count = 8;
        StdOut.printf("%s %s ", ANSI.PURPLE + "PC:" + ANSI.RESET, H.toHex(pc));
        for (int i = 0; i < count; i++) {
            sz = (a[i] == 0) ? ANSI.RESET + H.toHex(a[i]) : ANSI.DATA + H.toHex(a[i]) + ANSI.RESET;
            StdOut.print(ANSI.PURPLE + "R" + H.toHexShort(i) + ": " + sz  + " ");
            if (i % 8 == 7) StdOut.println();
        }
    }

    // print core dump of TOY to standard output
    public void dump(String sz) {
        StdOut.println("Machine State:");
//        showstate(hw.getReg(), pc);
      StdOut.printf("%s  PC: %s\n", sz, H.toHex(pc) );
      StdOut.println("Registers:");
      showreg(hw.getReg());
      StdOut.println("Main:");
//       showhex(hw.getMem(), 0x0010 * 0x0010);
        StdOut.print("\n\nStack:");
        StdOut.printf("  SP: %s\n", H.toHex(stkptr));
        showhex(hw.getStk(), 0, 0x0020);

        // Print keys and values
        StdOut.println("Pages:");
        for (String i : pages.keySet()) {
            StdOut.printf("%s\n", "" + i + " @ " + H.toHex(pages.get(i)));
            showhex(hw.getMem(), pages.get(i), PAGESIZE);
        }
        // Print keys and values
        StdOut.println("Labels:");
        for (String i : label.keySet()) {
            StdOut.printf("%s\n", "key: " + i + " value: " + H.toHex(label.get(i)));
        }
    }
    static public void trace(String sz, int i) {
        System.out.println(sz + " " + H.toHex(i));
    }

    public void run(Pane p1, int programCounter, String mode) throws Exception {
        int idx = 0;
        int ict = 0;
        boolean bRun = true;
        int n = 0;
        Instruction I = null;
        boolean haltflag = false;

        int[] reg = hw.getReg();
        int[] mem = hw.getMem();
        pc = programCounter;
        original_pc = pc;
        if ( pc == -1 ) {
            pc = mem[0x0000];
            original_pc = pc;
        }

        coreDump(NODUMP,HALT);

        sb.append(String.format("%26s %6s %2s %2s  %4s\n","Instruction", "D", "S", "T", "ADDR"));
        //p1.put(String.format("%91s%s\n", "", "      PC   STK  0    1    2    3    4    5    6    7"));
        if (mode.equals("READY")) bRun = false;
        while (bRun) {
           if (pc == 0) return;
           if (mode.equals("STEP")) bRun = false;
            // Fetch and parse
               try {
                   I = new Instruction(hw.getMem(),pc); 
                   coreDump(NODUMP,HALT);
               } catch (Exception e) {
                    System.out.println("Caught Exception: "+ e.getMessage());
                    System.out.println(TOY.programAsRead.toString());
                    e.printStackTrace();
                    System.exit(1);
               } 

            lastInstruction = I;
            pc = pc + 2;

            int inst = I.getInst();
            int op   = I.getOp();
            int d    = I.getD();
            int s    = I.getS();
            int t    = I.getT();
            int addr = I.getAddr();

       // stdin 
       //     if ((addr == 255 && op == 8) || (reg[t] == 255 && op == 10))
       //         mem[255] = H.fromHex(StdIn.readString());
            ict++;
            // Execute
            switch (op) {
                case 0x00: II.add(op, "halt", "haltflag = true");
                           haltflag=true;    
                           break;                                                                // halt
                case 0x01: II.add(op, "add", "reg[d] = reg[s] + reg[t]");
                           reg[d] = reg[s] +  reg[t];         
                           break;                                                                // add
                case 0x02: II.add(op, "subtract", "reg[d] = reg[s] - reg[t]");
                           reg[d] = reg[s] -  reg[t];
                           break;                                                                // subtract
                case 0x03: II.add(op, "bitwise and", "reg[d] = reg[s] & reg[t]");
                           reg[d] = reg[s] &  reg[t];
                           break;                                                                // bitwise and
                case 0x04: II.add(op, "bitwise or",  "reg[d] = reg[s] ^ reg[t]");
                           reg[d] = reg[s] ^  reg[t];
                           break;                                                                // bitwise xor
                case 0x05: II.add(op, "shift left", "reg[d] = reg[s] << reg[t]");
                           reg[d] = reg[s] << reg[t];
                           break;                                                                // shift left
                case 0x06: II.add(op, "shift right", "reg[d] = reg[s] >> reg[t]"); 
                           reg[d] = reg[s] >> reg[t];
                           break;                                                                // shift right
                case 0x07: II.add(op, "load address", "reg[d] = addr");
                           reg[d] = addr;
                           break;                                                                // load address
                case 0x08: II.add(op, "load", "reg[d] = mem[addr]");   
                           reg[d] = mem[addr];
                           break;                                                                // load
                case 0x09: II.add(op, "store", "mem[addr] = reg[d]");
                           mem[addr] = reg[d];
                           break;                                                                // store
                case 0x0A: II.add(op, "load indirect", "reg[d] = mem[reg[t] & 0xFFFF]");
                           reg[d] = mem[reg[t] & 0xFFFF];
                           break;                                                                // load indirect
                case 0x0B: II.add(op, "store indirect", "mem[reg[t] & 0x0FFFF] = reg[d]"); 
                           mem[reg[t] & 0xFFFF] = reg[d];
                           break;                                                                // store indirect
                case 0x0C: II.add(op, "branch if zero", "if ((short) reg[d] == 0) pc = addr");  
                           if ((short) reg[d] == 0) pc = addr;
                           break;                                                                // branch if zero
                case 0x0D: II.add(op, "branch if pos", "if ((short) reg[d] >  0) pc = addr");
                           if ((short) reg[d] >  0) pc = addr;
                           break;                                                                // branch if positive
                case 0x0E: II.add(op, "jump indirect", "pc = reg[d]");
                           pc = reg[d];
                           break;                                                                // jump indirect
                case 0x0F: II.add(op, "jump and link", "reg[d] = pc; pc = addr");
                           reg[d] = pc; pc = addr;
                           break;                                                                // jump and link

                // My Instructions
                case 0x10: II.add(op, "push address", "push addr");
                           //XX stkptr++;stk[stkptr] = mem[addr];
                           hw.stkPush(mem[addr]);
                           break;                                                                // push address
                case 0x11: II.add(op, "push register", "push reg[d]");
                           //XX stkptr++;stk[stkptr] = reg[d];
                           hw.stkPush(reg[d]);
                           break;                                                                // push register
                case 0x12: II.add(op, "pop to register", "pop to reg[d]");
                           //XX reg[d] = stk[stkptr];
                           reg[d] = hw.stkPop();
                           //XX stk[stkptr] = 0xFFFF;
                           //XX stkptr--; 
                           //XX if (stkptr<0) stkptr=0;
                           break;                                                                // pop to register
                case 0x13: II.add(op, "increment register", "reg[d]++"); 
                           reg[d] = reg[d] + 1;
                           break;                                                                // increment register
                case 0x14: II.add(op, "decrement register", "reg[d]--"); 
                           reg[d] = reg[d] - 1;
                           break;                                                                // decrement register
                case 0x15: II.add(op, "shift reg left", "reg[d] = reg[d] << 1");
                           reg[d] = reg[d] << 1;
                           break;                                                                // shift reg left
                case 0x16: II.add(op, "shift reg right", "reg[d] = reg[d] >> 1");
                           reg[d] = reg[d] >> 1;
                           break;                                                                // shift reg right
                case 0x17: II.add(op, "push pc and link", "push pc and pc = addr");
                           //XX stk[++stkptr] = pc; pc = addr;
                           hw.stkPush(pc); pc = addr;
                           break;                                                                // push pc and link
                case 0x18: II.add(op, "jump", "pc = addr");
                           pc = addr;
                           break;                                                                // jump
                case 0x19: II.add(op, "pop and link", "return");
                           //XX pc = stk[stkptr];
                           pc = hw.stkPop();
                           //XX stk[stkptr] = 0xFFFF;
                           //XX stkptr--; 
                           //XX if (stkptr<0) stkptr=0;
                           break;                                                                // pop and link
                case 0x20: II.add(op, "NOP", "NOP");
                           pc = pc;
                           break;                                                                // NOP
                case 0x21: II.add(op, "Load Addr Reg", "reg[ADRR] = addr");
                           reg[ADRR] = addr;
                           break;                                                                // Load Address register
                case 0x22: II.add(op, "Inc Addr Reg", "reg[ADRR]++");
                           reg[ADRR] = reg[ADRR] + 1;
                           break;                                                                // Inc Address register
                case 0x23: II.add(op, "Store Reg indirect Addr Reg", "mem[reg[d]]= reg[ADRR]");
                           mem[reg[ADRR]] = reg[d];
                           break;                                                                // sore indirect Address register
                case 0x24: II.add(op, "Push This", "push this addr");
                           //XX stk[++stkptr] = pc-2; 
                           hw.stkPush(pc-2); 
                           break;                                                                // push PC
                case 0x50: II.add(op, "reg char out", "reg[d] char out");
                           StdOut.print(reg[d]);
                           break;                                                                // reg char out
                case 0x51: II.add(op, "mem char out", "mem[addr] char out");
                           StdOut.print(mem[addr]);
                           break;                                                                // mem char out
                case 0x52: II.add(op, "string 10 16b", "string out 16b");
                           idx=addr; 
                           while (mem[idx]!=0) {
                               StdOut.print( String.valueOf((char) mem[idx]) );
                               idx++;
                           }
                           break;                                                                // string out 16 bit
                case 0x53: II.add(op, "string out 8b", "string out 8b");
                           idx=addr; 
                           boolean flip = true;
                           n = (mem[idx] >> 8) & 0x00FF; 
                           while (n != 0) {
                               StdOut.print( String.valueOf((char) n) );
                               flip = !flip; 
                               n = (flip) ? (mem[idx] >> 8) & 0x00FF  : (mem[idx] >> 0) & 0x00FF; 
                               if (!flip) idx++;
                           }
                           break;                                                                // string out 8 bit
                case 0x60: II.add(op, "int to ascii", "int to ascii");
                           char[] ca = H.convertIntegerToCharArray(reg[d]);
                           for (int i = 0;i<ca.length;i++) {
                               mem[reg[t]] = ca[i];
                               reg[t]++;
                           }
                           break;                                                                // int to ascii
            }

            // stdout
       //  if ((addr == 255 && op == 9) || (reg[t] == 255 && op == 11))
       //         StdOut.println(H.toHex(mem[255]));
                                                     // ANSI.PURPLE +H.toHex(I.getPc()) + ":" + ANSI.RESET,
            //sb.append(I.toString() + "\n");
            String result = String.format("%s %s %s %s %-18s %-2s %-2s %-2s %-2s -- %-38s -- %s %s %s %s %s %s %s %s %s %s\n",
                                                     H.toHex(ict),
                                                     H.toHex(I.getPc()) + ":",
                                                     H.toHex(I.getHighword()),
                                                     H.toHex(I.getLowword()),
                                                     II.get(op).getName(),
                                                     H.toHexShort(I.getOp()),
                                                     H.toHexShort(I.getD()),
                                                     H.toHexShort(I.getS()),
                                                     H.toHexShort(I.getT()),
                                                     II.get(op).getDescription(),
                                                     H.toHex(pc),
                                                     H.toHex(hw.stkTop()),
                                                     H.toHex(reg[0]),
                                                     H.toHex(reg[1]),
                                                     H.toHex(reg[2]),
                                                     H.toHex(reg[3]),
                                                     H.toHex(reg[4]),
                                                     H.toHex(reg[5]),
                                                     H.toHex(reg[6]),
                                                     H.toHex(reg[7])
                                                     );
            p1.put(result);

            // halt
            if (haltflag) {
                p1.put("HALT");
                pc = 0;
                break;
            }

            reg[0] = 0;                // ensure reg[0] is always 0
            reg[d] = reg[d] & 0xFFFF;  // don't let reg[d] overflow a 16-bit integer
            pc = pc & 0xFFFF;          // don't let pc overflow an 16-bit integer

        }
    }
    public void commandline(Pane p, Pane[] panes) {
            String szIn = "";
            p.pos(p.getCOMMAND_ROW(),p.getCOMMAND_COLUMN());
            Finder f   = new Finder("^([/0-9A-Za-z]*)[ \t]*([0-9A-Za-z]*)");
            Finder f2   = new Finder("^([/])([0-9A-Za-z]*)");
            while (true) {
                String sz = p.prompt(">> ");
                if (f.matches(sz)) {
                    String name = f.get1().toString();

                    if (name.equals("I")) {
                        p.buffer4();
                        p.refresh(0);
                    }

                    if (name.equals("S")) {  // HELP:: S,Single Step
                        try {
                            p.buffer1();
                            this.run(p, pc, "STEP");
                            this.showstatev(panes[2]);
//                          toy.memoryPane(p1, 0x0000);
//                          toy.showhexp(toy.hw.getMem(), 0x0100, PAGESIZE,p3);
                        } catch (Exception e) {
                             StdOut.printf("%s\n", sb.toString());
                             StdOut.println(lastInstruction.toString() + "\n");
                             System.out.println("Caught Exception: "+ e.getMessage());
                             e.printStackTrace();
                             System.exit(1);
                        }
                    }
                    if (name.equals("G")) {
                        try {
                            this.run(p, H.fromHex(f.get2()), "");
                            this.showstatev(panes[2]);
//                          toy.memoryPane(p1);
//                          toy.showhexp(toy.hw.getMem(), 0x0100, PAGESIZE,p3);
                        } catch (Exception e) {
                             StdOut.printf("%s\n", sb.toString());
                             StdOut.println(lastInstruction.toString() + "\n");
                             System.out.println("Caught Exception: "+ e.getMessage());
                             e.printStackTrace();
                             System.exit(1);
                        }
                    }
                    if (name.equals("R")) {  // HELP:: R,Show Program Trace
                        p.buffer1(0);
                    }
                    if (name.equals("P")) {  // HELP:: P,Show Program as read in
                        p.buffer2(0);
                    }
                    if (name.equals("M")) {  // HELP:: M,Show Memory
                        p.buffer3clear();
                        szIn = p.prompt(">memory address > ");

                        switch (szIn) {
                            case "": this.memoryPane(p, 0x0000); break;
                            case "R0": this.memoryPane(p, hw.getReg()[0]); break;
                            case "R1": this.memoryPane(p, hw.getReg()[1]); break;
                            case "R2": this.memoryPane(p, hw.getReg()[2]); break;
                            case "R3": this.memoryPane(p, hw.getReg()[3]); break;
                            case "R4": this.memoryPane(p, hw.getReg()[4]); break;
                            case "R5": this.memoryPane(p, hw.getReg()[5]); break;
                            case "R6": this.memoryPane(p, hw.getReg()[6]); break;
                            case "R7": this.memoryPane(p, hw.getReg()[7]); break;
                            default: this.memoryPane(p, H.fromHex(szIn));
                         }
                         p.buffer3(0);
                    }
                    if (name.equals("T")) {      // HELP:: T,Move to Top
                        p.top();
                    }
                    if (name.equals("B")) {      // HELP:: B,Move to Bottom
                        p.bottom();
                    }
                    if (name.equals("U")) {      // HELP:: U,Move Up
                        p.up();
                    }
                    if (name.equals("D")) {      // HELP:: D,Move Down
                        p.down();
                    }
                    if (name.equals("F")) {      // HELP:: F,Find
                        p.find(f.get2());
                    }
                    if (name.equals("/")) {      // HELP:: /,Find
                        p.find(f.get2());
                    }
                    if (name.equals("E")) {      // HELP:: E,Edit Memory <Address>
                        int[] x = hw.getMem();
                        x[H.fromHex(f.get2())] = H.fromHex( p.prompt(">edit (" + f.get2() + ")> ") );
                    }
                    if (name.equals("H")) {      // HELP:: H,Help
                        p.bufferHelp(0);
                    }
                    if (name.equals("RESET")) {      // HELP:: RESET,Resets PC
                        panes[1].put("RESET");
                        pc = original_pc;
                        hw.initRegs();
                        this.showstatev(panes[2]);
                    }
                    if (name.equals("IPL")) {      // HELP:: IPL,Initial Program Load
                        panes[1].put("IPL");
                        pc = hw.getMem()[0x0000];
                        original_pc = pc;
                        hw.initRegs();
                        hw.initStk();
                        this.showstatev(panes[2]);
                    }
                    if (name.equals("BOOT")) {      // HELP:: BOOT,IPL from 0x0000
                        panes[1].put("BOOT");
                        pc = hw.getMem()[0x0000];
                        original_pc = pc;
                        hw.initRegs();
                        hw.initStk();
                        this.showstatev(panes[2]);
                        try {
                            this.run(panes[1], pc, "");
                            this.showstatev(panes[2]);
                        } catch (Exception e) {
                             System.exit(1);
                        }
                    }
                    if (name.equals("Q")) {      // HELP:: Q,Quit
                        System.exit(1);
                    }
                }
            }
    }

    // run the TOY simulator with specified file
    public static void main(String[] args) { 
        Pane[] panes = new Pane[4];

        Pane p1 =  new Pane(24,  5,            1,             148);
        Pane p2 =  new Pane(20,  5,            p1.gapcolumn(), 10);
        Pane p3 =  new Pane(10,  p1.gaplap(),   1,             148);
        Pane msg =  new Pane(1,  48,            1,             148);
        panes[0] = null;
        panes[1] = p1;
        panes[2] = p2;
        panes[3] = p3;
        ObjectRing ring = new ObjectRing();
        ring.add(p1);
        ring.add(p2);
        ring.add(p3);
        ring.add(msg);

        int pc = 0x0010;

        StdOut.print(ANSI.RESET);
        // no command-line arguments
        if (args.length == 0) {
            System.err.println("TOY:   invalid command-line options");
            System.err.println("usage: java TOY filename.toy");
            return;
        }

        String filename = args[0];

        TOY toy = new TOY(filename, pc).coreDump(NODUMP,HALT);
        p1.loadPane(filename, p1.getBuffer2() );
        p1.loadPane("instructionset.txt", p1.getBuffer4() );
        p1.loadPane("help.txt", p1.getBufferHelp() );

        try {
            panes[1].put("READY");
            toy.run(p1, -1, "READY");
            toy.memoryPane(p1);
            toy.showstatev(p2);
            toy.showhexp(toy.hw.getMem(), 0x0000, PAGESIZE,p3);
            p3.put("");
            toy.showhexp(toy.hw.getMem(), 0x0100, PAGESIZE,p3);
            toy.commandline(p1, panes);
        } catch (Exception e) {
             StdOut.printf("%s\n", sb.toString());
             StdOut.println(lastInstruction.toString() + "\n");
             System.out.println("Caught Exception: "+ e.getMessage());
             e.printStackTrace();
             System.exit(1);
        }


    }
}

final class Instruction {
    
    private int pc;
    private int highword;
    private int lowword;
    private int inst;
    private int op;
    private int s;
    private int t;
    private int d;
    private int addr;
        
    public void setPc(int n) {
        this.pc = n;
    }
    public int getPc() {
        return this.pc;
    }
    public void setHighword(int n) {
        this.highword = n;
    }
    public int getHighword() {
        return this.highword;
    }
    public void setLowword(int n) {
        this.lowword = n;
    }
    public int getLowword() {
        return this.lowword;
    }
    public void setInst(int n) {
        this.inst = n;
    }
    public int getInst() {
        return this.inst;
    }
    public void setOp(int n) {
        this.op = n;
    }
    public int getOp() {
        return this.op;
    }
    public void setS(int n) {
       this.s = n;
    }
    public int getS() {
        return this.s;
    }
    public void setD(int n) {
        this.d = n;
    }
    public int getD() {
        return this.d;
    }
    public void setT(int n) {
        this.t = n;
    }
    public int getT() {
        return this.t;
    }
    public void setAddr(int n) {
        this.addr = n;
    }
    public int getAddr() {
        return this.addr;
    }
    public Instruction(int[] mem, int pc) throws InstructionValueException {
            // Fetch and parse
            setPc(pc);
            setHighword(mem[pc++]);                   // fetch next word
            setInst(highword); 
            setOp( (highword >> 8)  & 0x00FF);        // get opcode (bits 12-15)
            setD(  (highword >>  0) & 0x00FF);        // get dest  
            setLowword(mem[pc++]);                    // fetch next word
            this.s    = (lowword  >>  8) & 0x00FF;    // get s    
            this.t    = lowword          & 0x00FF;    // get t   
            this.addr = lowword          & 0xFFFF;    // get addr
    }
    public void decode() {
           
            StdOut.printf("onk\n");
            StdOut.printf("%s %s %s %s %s %s %s\n",
                                                     ANSI.PURPLE +H.toHex(this.getPc()) + ":" + ANSI.RESET,
                                                     H.toHex(this.getHighword()),
                                                     H.toHex(this.getLowword()),
                                                     H.toHexShort(this.getOp()),
                                                     H.toHexShort(this.getD()),
                                                     H.toHexShort(this.getS()),
                                                     H.toHexShort(this.getT())
                                                     );
    }

    public static String toDec(int n) {
        return String.format("%05d", n & 0xFFFF);
    }
    public static String toDecShort(int n) {
        return String.format("%03d", n & 0xFFFF);
    }

}
class InstructionValueException extends Exception {
    public InstructionValueException(Instruction I, String msg) {
        super(msg + "  " + I.toString());
    }
}
class Registers {
    private int[] reg;
    public Registers(int[] r) {
        this.reg = r;
    }
    public int[] getRegisters() {
        return this.reg;
    }
    public String toStringVars() {
        StringBuffer sb = new StringBuffer();
        sb.append("[[").append(H.toHex(this.reg[0])).append("]] ");
        for (int i = 10; i < this.reg.length; i++) sb.append(H.toHex(this.reg[i])).append(" ");
        return sb.toString();
    }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < this.reg.length; i++) sb.append(H.toHex(this.reg[i])).append(" ");
        return sb.toString();
    }
}














