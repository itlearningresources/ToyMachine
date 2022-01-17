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
    static int PAGESIZE=62;
    static HashMap<String, Integer> label = new HashMap<String, Integer>();
    static HashMap<String, Integer> pages = new HashMap<String, Integer>();
    static  StringBuffer programAsRead = new StringBuffer(1024);
    static  StringBuffer sb = new StringBuffer(120);
    static  Instruction lastInstruction = null;
    InstructionSet II = new InstructionSet();
    private int pc;                            // program counter
    private int original_pc;                   // program counter
    private int memory_monitor;                // Memory monitor address

    final int ADRR = 0x01;
    private Registers R = null;
    private HW hw = null;
    private boolean trackmemory = true;

    // return a 4-digit hex string corresponding to 16-bit integer n
    // return a 2-digit hex string corresponding to 8-bit integer n
    // return a 16-bit integer corresponding to the 4-digit hex string s
    public static final boolean HALT = true;
    public static final boolean DUMP = true;
    public static final boolean NOHALT = false;
    public static final boolean NODUMP = false;

    // create a new TOY VM and load with program from specified file
    public TOY(String filename) {
        this(filename, 0x10);
    }

    public TOY(String filename, int pc) {
        final int PROGRAMMEMORY=16;
        int loadptr=PROGRAMMEMORY;

        this.pc = pc;
        this.memory_monitor = 0x1000;
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

    public  void showstate(Pane p2) {
        String sz = "";
        p2.reset();
        int[] a = hw.getReg();
        int count = 8;
        p2.put("PC : ", ANSI.RESET, H.toHex(pc));
        p2.put("");
        for (int i = 0; i < count; i++) {
            sz = (a[i] == 0) ? ANSI.RESET + H.toHex(a[i]) : ANSI.DATA + H.toHex(a[i]) + ANSI.RESET;
            //p2.put("R", H.toHexShort(i), ": ", sz);
            p2.putf("%s%s%s%s","R", H.toHexShort(i), ": ", sz);
        }
        p2.put("");
            p2.put("SP : ", hw.getStkPtrHex());
        for (int i = 0; i < count; i++) {
            sz = (hw.stkGet(i) == 0) ? ANSI.RESET + hw.stkGetHex(i) : ANSI.DATA + hw.stkGetHex(i) + ANSI.RESET;
            p2.put(H.toHex2D(i), " : ", sz);
        }
        p2.put("");
        p2.put("MM : ", H.toHex(memory_monitor));
        p2.put("");
        for (int i =0;i<hw.getBrk().length;i++) if (hw.getBrk()[i]) p2.put("BP ",H.toHex(i));
    }

//      StdOut.println("Pages:");
//      for (String i : pages.keySet()) {
//          StdOut.printf("%s\n", "" + i + " @ " + H.toHex(pages.get(i)));
//          showhex(hw.getMem(), pages.get(i), PAGESIZE);
//      }
//      StdOut.println("Labels:");
//      for (String i : label.keySet()) {
//          StdOut.printf("%s\n", "key: " + i + " value: " + H.toHex(label.get(i)));
//      }

    public TOY run(Pane[] panes, int programCounter, String mode) throws Exception {
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


        sb.append(String.format("%26s %6s %2s %2s  %4s\n","Instruction", "D", "S", "T", "ADDR"));
        //p1.put(String.format("%91s%s\n", "", "      PC   STK  0    1    2    3    4    5    6    7"));
        if (mode.equals("READY")) bRun = false;
        while (bRun) {
           if (pc == 0) break;
           if (mode.equals("STEP")) bRun = false;
            // Fetch and parse
               try {
                   if (programCounter != pc) 
                       if (hw.getBrk()[pc]) {
                           Pane.getMsgPane().putlight("BREAK @ " + H.toHex(pc) );
                           break;
                       }
                   I = new Instruction(hw.getMem(),pc); 
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
                //
                // SUBSET:: Halt
                //
                case 0x00: II.add(op, "halt", "haltflag = true");
                           haltflag=true;    
                           break;                                                                // halt
                //
                // SUBSET:: Math and Accumulator
                //
                case 0x01: II.add(op, "add", "reg[d] = reg[s] + reg[t]");
                           reg[d] = reg[s] +  reg[t];         
                           break;                                                                // add
                case 0x02: II.add(op, "subtract", "reg[d] = reg[s] - reg[t]");
                           reg[d] = reg[s] -  reg[t];
                           break;                                                                // subtract
                case 0x03: II.add(op, "increment register", "reg[d]++"); 
                           reg[d] = reg[d] + 1;
                           break;                                                                // increment register
                case 0x04: II.add(op, "decrement register", "reg[d]--"); 
                           reg[d] = reg[d] - 1;
                           break;                                                                // decrement register
                case 0x05: II.add(op, "accumulate", "reg[d] = reg[d] + reg[s]"); 
                           reg[d] = reg[d] + reg[s];
                           break;                                                                // accumlate register
                case 0x06: II.add(op, "deccumulate", "reg[d] = reg[d] + reg[s]"); 
                           reg[d] = reg[d] - reg[s];
                           break;                                                                // deccumlate register

                //
                // SUBSET:: Load and Store
                //
                case 0x10: II.add(op, "load register with addr", "reg[d] = addr");
                           reg[d] = addr;
                           break;                                                                // load address
                case 0x11: II.add(op, "load register with memory", "reg[d] = mem[addr]");   
                           reg[d] = mem[addr];
                           break;                                                                // load
                case 0x12: II.add(op, "Inc Addr Reg", "reg[addr]++");
                           reg[ADRR] = reg[ADRR] + 1;
                           break;                                                                // Inc Address register
                case 0x13: II.add(op, "Store Reg indirect Addr Reg", "mem[reg[d]]= reg[addr]");
                           mem[reg[ADRR]] = reg[d];
                           break;                                                                // sore indirect Address register
                case 0x14: II.add(op, "store reg to mem", "mem[addr] = reg[d]");
                           mem[addr] = reg[d];
                           if (trackmemory) {
                               panes[3].buffer1().clear().showHex2(this.hw.getMem(), addr);
                           }
                           break;                                                                // store
                case 0x15: II.add(op, "store reg to mem indirect", "mem[reg[d] & 0x0FFFF] = reg[s]"); 
                           mem[reg[d] & 0xFFFF] = reg[s];
                           break;                                                                // store indirect
                case 0x16: II.add(op, "load indirect", "reg[d] = mem[reg[s] & 0xFFFF]");
                           reg[d] = mem[reg[s] & 0xFFFF];
                           break;                                                                // load indirect
                case 0x17: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x18: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x19: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x1A: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x1B: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x1C: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x1D: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x1E: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x1F: II.add(op, "reserved", "reserved"); break;                            // reserved


                //
                //  SUBSET:: Branch and Jump
                //
                case 0x20: II.add(op, "jump", "pc = addr");
                           pc = addr;
                           break;                                                                // jump
                case 0x21: II.add(op, "branch if zero", "if (reg[d] == 0) pc = addr");  
                           if (reg[d] == 0) pc = addr;
                           break;                                                                // branch if zero
                case 0x22: II.add(op, "branch if not zero", "if (reg[d] != 0) pc = addr");  
                           if (reg[d] != 0) pc = addr;
                           break;                                                                // branch if not zero

                case 0x23: II.add(op, "pop and link if zero", "if (reg[d] == 0) pc = popstk");  
                           if (reg[d] == 0) pc = hw.stkPop();
                           break;                                                                // pop and link if zero
                case 0x24: II.add(op, "pop and link if not zero", "if (reg[d] != 0) pc = popstk");  
                           if (reg[d] != 0) pc = hw.stkPop();
                           break;                                                                // pop and link if not zero


                case 0x25: II.add(op, "branch if pos", "if (reg[d] >  0) pc = addr");
                           if (reg[d] >  0) pc = addr;
                           break;                                                                // branch if positive
                case 0x26: II.add(op, "jump indirect", "pc = reg[d]");
                           pc = reg[d];
                           break;                                                                // jump indirect
                case 0x27: II.add(op, "jump and link", "reg[d] = pc; pc = addr");
                           reg[d] = pc; pc = addr;
                           break;                                                                // jump and link
                case 0x28: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x29: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x2A: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x2B: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x2C: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x2D: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x2E: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x2F: II.add(op, "reserved", "reserved"); break;                            // reserved

                // 
                // SUBSET:: Stack
                //
                case 0x30: II.add(op, "push address", "push addr");
                           hw.stkPush(mem[addr]);
                           break;                                                                // push address
                case 0x31: II.add(op, "push register", "push reg[d]");
                           hw.stkPush(reg[d]);
                           break;                                                                // push register
                case 0x32: II.add(op, "pop to register", "pop to reg[d]");
                           reg[d] = hw.stkPop();
                           break;                                                                // pop to register
                case 0x33: II.add(op, "pop and link", "return");
                           pc = hw.stkPop();
                           break;                                                                // pop and link
                case 0x34: II.add(op, "Push This", "push this addr");
                           hw.stkPush(pc-2); 
                           break;                                                                // push PC
                case 0x35: II.add(op, "push pc and link", "push pc and pc = addr");
                           hw.stkPush(pc); pc = addr;
                           break;                                                                // push pc and link
                case 0x36: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x37: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x38: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x39: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x3A: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x3B: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x3C: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x3D: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x3E: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x3F: II.add(op, "reserved", "reserved"); break;                            // reserved
                //
                // SUBSET:: Bitwise and Shift
                //
                case 0x40: II.add(op, "bitwise and", "reg[d] = reg[s] & reg[t]");
                           reg[d] = reg[s] &  reg[t];
                           break;                                                                // bitwise and
                case 0x41: II.add(op, "bitwise or",  "reg[d] = reg[s] ^ reg[t]");
                           reg[d] = reg[s] ^  reg[t];
                           break;                                                                // bitwise xor
                case 0x42: II.add(op, "shift left", "reg[d] = reg[s] << reg[t]");
                           reg[d] = reg[s] << reg[t];
                           break;                                                                // shift left
                case 0x43: II.add(op, "shift right", "reg[d] = reg[s] >> reg[t]"); 
                           reg[d] = reg[s] >> reg[t];
                           break;                                                                // shift right
                case 0x44: II.add(op, "shift reg left", "reg[d] = reg[d] << 1");
                           reg[d] = reg[d] << 1;
                           break;                                                                // shift reg left
                case 0x45: II.add(op, "shift reg right", "reg[d] = reg[d] >> 1");
                           reg[d] = reg[d] >> 1;
                           break;                                                                // shift reg right
                case 0x46: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x47: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x48: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x49: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x4A: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x4B: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x4C: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x4D: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x4E: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x0F: II.add(op, "reserved", "reserved"); break;                            // reserved

                //
                // SUBSET:: NOP
                //
                case 0x50: II.add(op, "NOP", "NOP");
                           pc = pc;
                           break;                                                                // NOP
                //
                // SUBSET:: I/O and String
                //
                case 0x61: II.add(op, "reg char out", "reg[d] char out");
                           StdOut.print(reg[d]);
                           break;                                                                // reg char out
                case 0x62: II.add(op, "mem char out", "mem[addr] char out");
                           StdOut.print(mem[addr]);
                           break;                                                                // mem char out
                case 0x63: II.add(op, "string 10 16b", "string out 16b");
                           idx=addr; 
                           while (mem[idx]!=0) {
                               StdOut.print( String.valueOf((char) mem[idx]) );
                               idx++;
                           }
                           break;                                                                // string out 16 bit
                case 0x64: II.add(op, "string out 8b", "string out 8b");
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
                case 0x65: II.add(op, "int to ascii", "int to ascii");
                           char[] ca = H.convertIntegerToCharArray(reg[s]);
                           for (int i = 0;i<ca.length;i++) {
                               mem[reg[d]] = ca[i];
                               reg[d]++;
                           }
                           break;                                                                // int to ascii
                case 0x66: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x67: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x68: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x69: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x6A: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x6B: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x6C: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x6D: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x6E: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x6F: II.add(op, "reserved", "reserved"); break;                            // reserved
            }

            // stdout
            //  if ((addr == 255 && op == 9) || (reg[t] == 255 && op == 11))
            //         StdOut.println(H.toHex(mem[255]));

            panes[1].putf("%s: %s %s %-2s %-1s %-1s %-1s  %-25s %-32s -- %s %s %s %s %s %s %s %s %s %s",
                                                     H.toHex(I.getPc()),
                                                     H.toHex(I.getHighword()),
                                                     H.toHex(I.getLowword()),
                                                     H.toHexShort(I.getOp()),
                                                     H.toHexNibble(I.getD()),
                                                     H.toHexNibble(I.getS()),
                                                     ((I.getLowword() >> 4) > 0) ? "-" : H.toHexNibble(I.getT()),
                                                     H.shorten(II.get(op).getName(),25),
                                                     H.shorten(II.get(op).getDescription(),30),
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

            // halt
            if (haltflag) {
                panes[1].put("HALT");
                Pane.getMsgPane().putlight("HALT");
                pc = 0;
                break;
            }

            reg[0] = 0;                // ensure reg[0] is always 0
            reg[d] = reg[d] & 0xFFFF;  // don't let reg[d] overflow a 16-bit integer
            pc = pc & 0xFFFF;          // don't let pc overflow an 16-bit integer

        }

        return this;

    }
    public void commandline(Pane[] panes) {
            String szIn = "";
            panes[1].pos(Pane.getCOMMAND_ROW(),Pane.getCOMMAND_COLUMN());
            Finder f   = new Finder("^([/0-9A-Za-z]*)[ \t]*([0-9A-Za-z]*)");
            Finder f2   = new Finder("^([/])([0-9A-Za-z]*)");
            while (true) {
                String sz = panes[1].prompt(">> ");
                Pane.getMsgPane().putlight(sz);
                if (f.matches(sz)) {
                    String name = f.get1().toString().toUpperCase();

                    if (H.xmatch(name,"I","ISET")) {  // HELP:: I,List Instruction Set
                        panes[1].clear().buffer4().refresh(0);
                    }

                    if (name.equals("MM")) {  // HELP:: MM,Show Memory Monitor
                        panes[3].buffer1clear().showHex(this.hw.getMem(), this.memory_monitor);
                        this.showstate(panes[2]);
                    }
                    if (H.xmatch(name,"V")) {  // HELP:: V,View Memory
                        panes[3].buffer1clear().showHex2(this.hw.getMem(), 0x0000);
                    }
                    if (H.xmatch(name,"S","SS","STEP","STE")) {  // HELP:: S,Single Step
                        try {
                            panes[1].buffer1();
                            this.run(panes, pc, "STEP").showstate(panes[2]);
                            // panes[3].clear().buffer1clear().showHex2(this.hw.getMem(), this.memory_monitor);
                        } catch (Exception e) {
                             StdOut.printf("%s\n", sb.toString());
                             StdOut.println(lastInstruction.toString() + "\n");
                             System.out.println("Caught Exception: "+ e.getMessage());
                             e.printStackTrace();
                             System.exit(1);
                        }
                    }
                    if (H.xmatch(name,"GO","G")) {          // HELP:: G,Run Program
                        try {
                            panes[1].buffer1();
                            this.run(panes, pc, "").showstate(panes[2]);
                            panes[3].buffer1clear().showHex(this.hw.getMem(), this.memory_monitor);
                        } catch (Exception e) {
                             StdOut.printf("%s\n", sb.toString());
                             StdOut.println(lastInstruction.toString() + "\n");
                             System.out.println("Caught Exception: "+ e.getMessage());
                             e.printStackTrace();
                             System.exit(1);
                        }
                    }
                    if (name.equals("R")) {  // HELP:: R,Show Program Trace
                        panes[1].buffer1(0);
                    }
                    if (name.equals("P")) {  // HELP:: P,Show Program as read in
                        panes[1].buffer2(0);
                    }
                    if (name.equals("M")) {  // HELP:: M,Show Memory
                        panes[1].buffer3clear();
                        szIn = panes[1].prompt(">memory address > ");

                        switch (szIn) {
                            case "": this.memoryPane(panes[1], 0x0000); break;
                            case "R0": this.memoryPane(panes[1], hw.getReg()[0]); break;
                            case "R1": this.memoryPane(panes[1], hw.getReg()[1]); break;
                            case "R2": this.memoryPane(panes[1], hw.getReg()[2]); break;
                            case "R3": this.memoryPane(panes[1], hw.getReg()[3]); break;
                            case "R4": this.memoryPane(panes[1], hw.getReg()[4]); break;
                            case "R5": this.memoryPane(panes[1], hw.getReg()[5]); break;
                            case "R6": this.memoryPane(panes[1], hw.getReg()[6]); break;
                            case "R7": this.memoryPane(panes[1], hw.getReg()[7]); break;
                            default: this.memoryPane(panes[1], H.fromHex(szIn));
                         }
                         panes[1].buffer3(0);
                    }
                    if (name.equals("T")) {      // HELP:: T,Move to Top
                        panes[1].top();
                    }
                    if (name.equals("B")) {      // HELP:: B,Move to Bottom
                        panes[1].bottom();
                    }
                    if (H.xmatch(name,"U")) {      // HELP:: U,Move Up
                        panes[1].up();
                    }
                    if (name.equals("D")) {      // HELP:: D,Move Down
                        panes[1].down();
                    }
                    if (name.equals("F")) {      // HELP:: F,Find
                        panes[1].find(f.get2());
                    }
                    if (name.equals("/")) {      // HELP:: /,Find
                        panes[1].find(f.get2());
                    }
                    if (H.xmatch(name,"MONITOR","MON")) {      // HELP:: E,Edit Memory <Address>
                        int[] mem = hw.getMem();
                        this.memory_monitor = H.fromHex( panes[1].prompt(">monitor address (" + f.get2() + ")> ", H.toHex(this.memory_monitor)));
                        panes[3].buffer3clear().showHex(mem, this.memory_monitor);
                        this.showstate(panes[2]);
                    }
                    if (H.xmatch(name,"E", "EDIT","EDI")) {      // HELP:: E,Edit Memory <Address>
                        int[] x = hw.getMem();
                        x[H.fromHex(f.get2())] = H.fromHex( panes[1].prompt(">edit (" + f.get2() + ")> ") );
                    }
                    if (H.xmatch(name, "H","HELP","HEL")) {      // HELP:: H,Help
                        panes[1].bufferHelp(0);
                    }
                    if (H.xmatch(name,"RESET","RES")) {      // HELP:: RESET,Resets PC
                        panes[1].put("RESET");
                        hw.initRegs();
                        pc = original_pc;
                        this.showstate(panes[2]);
                    }
                    if (H.xmatch(name,"BREAK","BRK")) {      // HELP:: BREAK, Set/Clear Breakpoint
                        panes[1].put("BREAK");
                        int bp = H.fromHex( panes[1].prompt(">break > ") );
                        hw.getBrk()[bp] = !(hw.getBrk()[bp]);
                        panes[2].clear();
                        this.showstate(panes[2]);
                    }
                    if (name.equals("IPL")) {               // HELP:: IPL,Initial Program Load
                        panes[1].put("IPL");
                        pc = hw.getMem()[0x0000];
                        original_pc = pc;
                        hw.initRegs().initStk();
                        this.showstate(panes[2]);
                    }
                    if (H.xmatch(name,"BOOT","BOO")) {      // HELP:: BOOT,IPL from 0x0000
                        panes[1].put("BOOT");
                        pc = hw.getMem()[0x0000];
                        original_pc = pc;
                        hw.initRegs().initStk();
                        this.showstate(panes[2]);
                        try {
                            this.run(panes, pc, "").showstate(panes[2]);
                        } catch (Exception e) {
                             System.exit(1);
                        }
                    }
                    if (H.xmatch(name,"Q","QUI","QUIT","EXIT","EXI")) {      // HELP:: Q,Quit
                        System.exit(1);
                    }
                }
            }
    }

    // run the TOY simulator with specified file
    public static void main(String[] args) { 
        //Pane[] panes = new Pane[4];
        Pane[] panes = {};
        Pane[] ui = {};
        new Pane(24,  5,                   1,                    148);
        panes = Pane.getPanes();
        new Pane(39,  5,                   panes[1].gapcolumn(),  10);
        new Pane(12,  panes[1].gaplap(),   1,                    148);

        Pane.setMsgPane(new Pane(1,50,1,148));

        Pane.setCOMMAND_ROW(46);
        Pane.setCOMMAND_COLUMN(1);
        panes = Pane.getPanes();
        ui = Pane.getPanes();

        int pc = 0x0010;

        StdOut.print(ANSI.RESET);
        // no command-line arguments
        if (args.length == 0) {
            System.err.println("TOY:   invalid command-line options");
            System.err.println("usage: java TOY filename.toy");
            return;
        }

        String filename = args[0];

        TOY toy = new TOY(filename, pc);
        ui[1].loadPane(filename,             ui[1].getBuffer2() );
        ui[1].loadPane("instructionset.txt", ui[1].getBuffer4() );
        ui[1].loadPane("help.txt",           ui[1].getBufferHelp() );

        try {
            int[] mem = toy.hw.getMem();
            ui[1].put("READY");
            Pane.getMsgPane().putlight("READY!!!");
            toy.run(ui, -1, "READY");
            toy.memoryPane(ui[1]);
            toy.showstate(ui[2]);
            // ui[3].showHex(mem, toy.memory_monitor).put("").showHex(mem, 0x0000).put("").showHex(mem, 0x0100);
            toy.commandline(ui);
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
    public String toString() {
        String szRet = String.format("\n%d %d %d %d\n",op,d,s,t);
        return  szRet;
    }
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
            setOp( (highword >> 8)  & 0x00FF);        // get opcode
            setD(  (highword >>  4) & 0x000F);        // get dest  
            setLowword(mem[pc++]);                    // fetch next word
            this.s    = (highword >>  0) & 0x000F;    // get s    
            this.t    = lowword          & 0x00FF;    // get t   
            this.addr = lowword          & 0xFFFF;    // get addr
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
