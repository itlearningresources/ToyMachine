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

import java.io.*;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;
public class TOY { 

    static HashMap<String, Integer> label = new HashMap<String, Integer>();
    static  StringBuffer programAsRead = new StringBuffer(1024);
    static  StringBuffer sb = new StringBuffer(120);
    static  String currentFilename = "";
    static  Instruction lastInstruction = null;
    static Pane MainPane;
    static Pane StatePane;
    static Pane ScreenPane;
    static Pane InteractivePane;
    static Pane StatusAndMessagesPane;
    static HW   HW;
    static TOY singleton;
    InstructionSet II = new InstructionSet();
    private int pc;                            // program counter
    private int memory_monitor;                // Memory monitor address

    final int ADRR = 0x01;
    private Registers R = null;
    private HW hw = null;

    // return a 4-digit hex string corresponding to 16-bit integer n
    // return a 2-digit hex string corresponding to 8-bit integer n
    // return a 16-bit integer corresponding to the 4-digit hex string s
    public static final boolean HALT = true;
    public static final boolean DUMP = true;
    public static final boolean NOHALT = false;
    public static final boolean NODUMP = false;
    private static int dataRows = 16;
    public static void setDataRows(int n) { dataRows = (n>0) ? n : 16; }
    public static int getDataRows() { return dataRows; }
    public final static TOY getSingleton() {
        return singleton;
    }
    // create a new TOY VM and load with program from specified file
    public TOY(String filename) {
        this(filename, 0x0100);
    }

    public TOY(String filename, int pc) {


       /****************************************************************
        *  Create a Hardware Object
        ****************************************************************/
        currentFilename = filename;
        hw = new HW();
        HW = hw;
        R = new Registers(hw.getReg());
        int[] mem = hw.getMem();
        for (int i=0;i<mem.length;i++) mem[i]=0;
        String[] programlines = hw.getProgramLines();
        for (int i=0;i<programlines.length;i++) programlines[i]= new String("Not part of program");
        int loadptr=0x0100;         // default load program start point
        mem[0x0000]=loadptr;        // typically this is changed in the 
                                    // program.

        this.pc = pc;
        this.memory_monitor = 0x1000;
        this.singleton=this;

        label.clear();
        TOY.HW.forwardReferences(new In(TOY.currentFilename), loadptr, TOY.label);
        TOY.HW.loadMemoryWithProgram(new In(TOY.currentFilename), loadptr, TOY.programAsRead, TOY.label);

    }

    public TOY run(int programCounter, String mode) throws Exception {
        int idx = 0;
        int ict = 0;
        boolean bRun = true;
        int n = 0;
        int a = 0;
        Instruction I = null;
        boolean haltflag = false;

        int[] reg = hw.getReg();
        int[] mem = hw.getMem();
        pc = programCounter;
        if ( pc == -1 ) {
            TOY.ipl();
            pc = TOY.HW.getPC();
        }
        

        sb.append(String.format("%26s %6s %2s %2s  %4s\n","Instruction", "D", "S", "T", "ADDR"));
        //p1.putf("%s", String.format("%91s%s\n", "", "      PC   STK  0    1    2    3    4    5    6    7"));
        if (mode.equals("READY")) bRun = false;
        while (bRun) {
           if (pc == 0) break;
           if (mode.equals("STEP")) bRun = false;
            // Fetch and parse
               try {
                   if (programCounter != pc) 
                       if (hw.getBrk()[pc]) {
                           TOY.StatusAndMessagesPane.putf("%s", "BREAK @ " + H.toHex(pc) );
                           break;
                       }
                   I = new Instruction(hw.getMem(),pc); 
               } catch (Exception e) {
                    Application.CRASH(e);
               } 

            lastInstruction = I;
            pc = pc + 2;
            HW.setPC(pc);

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
                case 0x10: II.add(op, "load register with addr", "reg[d] = word");
                           reg[d] = addr;
                           break;                                                                // load word
                case 0x11: II.add(op, "load register with memory", "reg[d] = mem[word]");   
                           reg[d] = mem[addr];
                           break;                                                                // load
                case 0x12: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x13: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x14: II.add(op, "store reg to mem", "mem[addr] = reg[d]");
                           mem[addr] = reg[d];
                           a = addr;
                           break;                                                                // store

                case 0x15: II.add(op, "store reg to mem indirect", "mem[reg[d] & 0x0FFFF] = reg[s]"); 
                           a = reg[d] & 0xFFFF;
                           mem[reg[d] & 0xFFFF] = reg[s];
                           break;                                                                // store indirect

                case 0x16: II.add(op, "load indirect", "reg[d] = mem[reg[s] & 0xFFFF]");
                           reg[d] = mem[reg[s] & 0xFFFF];
                           break;                                                                // load indirect

                case 0x17: II.add(op, "load the index register", "indexregister = word");
                           hw.setIndexRegister(addr);
                           break;                                                                // load index register

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
                           pc = HW.setPC(addr);
                           break;                                                                // jump
                case 0x21: II.add(op, "branch if zero", "if (reg[d] == 0) pc = addr");  
                           if (reg[d] == 0) pc = HW.setPC(addr);
                           break;                                                                // branch if zero
                case 0x22: II.add(op, "branch if not zero", "if (reg[d] != 0) pc = addr");  
                           if (reg[d] != 0) pc = HW.setPC(addr);
                           break;                                                                // branch if not zero

                case 0x23: II.add(op, "pop and link if zero", "if (reg[d] == 0) pc = popstk");  
                           if (reg[d] == 0) pc = HW.setPC(hw.stkPop());
                           break;                                                                // pop and link if zero
                case 0x24: II.add(op, "pop and link if not zero", "if (reg[d] != 0) pc = popstk");  
                           if (reg[d] != 0) pc = HW.setPC(hw.stkPop());
                           break;                                                                // pop and link if not zero

                case 0x25: II.add(op, "branch if pos", "if (reg[d] >  0) pc = addr");
                           if (reg[d] >  0) pc = HW.setPC(addr);
                           break;                                                                // branch if positive
                case 0x26: II.add(op, "jump indirect", "pc = reg[d]");
                           pc = HW.setPC(reg[d]);
                           break;                                                                // jump indirect
                case 0x27: II.add(op, "jump and link", "reg[d] = pc; pc = addr");
                           reg[d] = HW.getPC(); pc = HW.setPC(addr);
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
                case 0x33: II.add(op, "reserved", "reserved"); break;                            // reserveda

                case 0x34: II.add(op, "Push This", "push this addr");
                           hw.stkPush(pc-2); 
                           break;                                                                // push PC
                case 0x35: II.add(op, "push pc and link", "push pc and pc = addr");
                           hw.stkPush(pc);
                           pc = HW.setPC(addr);
                           break;                                                                // push pc and link
                case 0x36: II.add(op, "pop and link", "return");
                           pc = HW.setPC(hw.stkPop());
                           break;                                                                // pop and link
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
                               a = reg[d];
                               reg[d]++;
                           }
                           break;                                                                // int to ascii
                case 0x66: II.add(op, "mem int to ascii", "mem int to ascii");
                for (int jj=0;jj<1024;jj++) {
                               ca = H.convertIntegerToCharArray(mem[addr]);
                               for (int i = 0;i<ca.length;i++) {
                                   mem[reg[d]] = ca[i];
                                   a = reg[d];
                                   reg[d]++;
                               }
                               addr++;
                }
                           break;                                                                // int to ascii
                case 0x67: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x68: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x69: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x6A: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x6B: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x6C: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x6D: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x6E: II.add(op, "reserved", "reserved"); break;                            // reserved
                case 0x6F: II.add(op, "reserved", "reserved"); break;                            // reserved

                // SUBSET:: Systems Calls
                //
                case 0x70: II.add(op, "system call", "system call");
                     switch (d) {
                         case 0x01:
                           idx=addr; 
                           StringBuilder sb = new StringBuilder();
                           while (mem[idx]!=0) {
                               //iStdOut.print( String.valueOf((char) mem[idx]) );
                               sb.append(String.valueOf((char) mem[idx]));
                               idx++;
                           }
                           TOY.ScreenPane.putf("%s", sb.toString());
                           break;
                         // print register d to screen
                         case 0x02:
                           TOY.ScreenPane.appendf("%s", H.toHex(reg[s]) + ",");
                           break;
                         // print memory word screen
                         case 0x03:
                           TOY.ScreenPane.putf("%s", H.toHex(mem[addr]));
                           break;
                     }
                     break;

                }

            // stdout
            //  if ((addr == 255 && op == 9) || (reg[t] == 255 && op == 11))
            //         StdOut.println(H.toHex(mem[255]));
            if (1 == 0)
            TOY.InteractivePane.putf("%s: %s %s %-2s %-1s %-1s %-1s  %-25s %-32s -- %s %s %s %s %s %s %s %s %s %s",
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
                TOY.StatusAndMessagesPane.putf("%s", "HALT");
                pc = 0;
                break;
            }

            reg[0] = 0;                // ensure reg[0] is always 0
            reg[d] = reg[d] & 0xFFFF;  // don't let reg[d] overflow a 16-bit integer
            pc = pc & 0xFFFF;          // don't let pc overflow an 16-bit integer

        }

        return this;

    }

    public void commandline() {
            String szIn = "";
            TOY.MainPane.pos(Pane.getCOMMAND_ROW(),Pane.getCOMMAND_COLUMN());
            Finder f   = new Finder("^([/0-9A-Za-z]*)[ \t]*([0-9A-Za-z]*)");
            Finder f2   = new Finder("^([/])([0-9A-Za-z]*)");
            while (true) {
                String sz = TOY.MainPane.prompt(">> ").toUpperCase();
                TOY.StatusAndMessagesPane.putf("%s", ANSI.EOL+sz);
                if (f.matches(sz)) {
                    String name = f.get1().toString().toUpperCase();
                    Application.dbg.log(name);
                    if (H.xmatch(name, "H","HELP","HEL")) {      // HELP:: H,Help
                        TOY.MainPane.clear().bufferHelp(0);
                    }
                    if (name.equals("IPL")) {               // HELP:: IPL,Initial Program Load
                        TOY.ipl();
                    }

                    if (H.xmatch(name,"CRASH")) {  // HELP:: CRASH,Crash Application
                        try {
                            throw new Exception("Crash 14 Test Exception");
                        } catch (Exception e) {
                            Application.CRASH(e);
                        }
                    }
                    if (H.xmatch(name,"S","SS","STEP","STE")) {  // HELP:: S,Single Step
                        try {
                            // TOY.MainPane.buffer1().clear();
                            this.run(HW.getPC(), "STEP");
                            InteractivePane.putf("%s","[NEXT] " + MainPane.showMemoryDecodedString(HW.getPC()));
                            TOY.StatePane.state();
                        } catch (Exception e) {
                             Application.CRASH(e);
                        }
                    }
                    if (H.xmatch(name,"GO","G")) {          // HELP:: G,Run Program
                        try {
                            TOY.MainPane.selectBuffer1();
                            this.run(HW.getPC(), "");
                            InteractivePane.putf("%s","[NEXT] " + MainPane.showMemoryDecodedString(HW.getPC()));
                            TOY.StatePane.state();
                        } catch (Exception e) {
                             Application.CRASH(e);
                        }
                    }

                    if (H.xmatch(name,"BREAK","BRK")) {      // HELP:: BREAK,Set/Clear Breakpoint
                        int bp = H.fromHex( TOY.MainPane.prompt(">break > ") );
                        hw.getBrk()[bp] = !(hw.getBrk()[bp]);
                        TOY.StatePane.clear();
                        TOY.StatePane.state();
                    }
                    if (name.equals("TEST")) {               // HELP:: TEST, Test Command
                        ScreenPane.readline();
                    }
                    if (name.equals("COLOR")) {               // HELP:: COLOR,Initial Program Load
                        TOY.StatePane.clear(ANSI.GREEN).paint();
                    }
                    if (H.xmatch(name,"CLEAR", "CLR", "C")) {  // HELP:: CLEAR,Clear Trace Window
                        TOY.MainPane.clear().selectAndClearBuffer1();
                        TOY.InteractivePane.clear().selectAndClearBuffer3();
                    }
                    if (H.xmatch(name,"Q","QUI","QUIT","EXIT","EXI")) {      // HELP:: Q,Quit
                        System.exit(1);
                    }
                    if (name.equals("T")) {      // HELP:: T,Move to Top
                        TOY.MainPane.top();
                    }
                    if (name.equals("B")) {      // HELP:: B,Move to Bottom
                        TOY.MainPane.bottom();
                    }
                    if (H.xmatch(name,"U")) {      // HELP:: U,Move Up
                        TOY.MainPane.up();
                    }
                    if (name.equals("D")) {      // HELP:: D,Move Down
                        TOY.MainPane.down();
                    }
                    if (name.equals("F")) {      // HELP:: F,Find
                        TOY.MainPane.find(f.get2());
                    }
                    if (name.equals("/")) {      // HELP:: /,Find
                        TOY.MainPane.find(f.get2());
                    }

                    if (H.xmatch(name,"LOG")) {      // HELP:: LOG,Show Log Buffer
                        TOY.MainPane.selectBuffer5().clear().refresh(0).selectBuffer1();
                    }
                    if (name.equals("MM")) {  // HELP:: MM,Show Memory Monitor
                        TOY.InteractivePane.selectAndClearBuffer1().showHex(this.hw.getMem(), this.memory_monitor);
                        TOY.StatePane.state();
                    }
                    if (name.equals("R")) {  // HELP:: R,Show Program Trace
                        TOY.MainPane.clear().selectBuffer1().refresh(0);
                    }
                    if (name.equals("P")) {  // HELP:: P,Show Program as read in
                        TOY.MainPane.clear().selectBuffer2().refresh(0);
                    }
                    if (H.xmatch(name, "DECODE", "D")) {  // HELP:: DECODE,Decode Memory
                        TOY.MainPane.clear().selectAndClearBuffer1().showMemoryDecoded(TOY.HW.getPC(), dataRows * 2);
                        TOY.MainPane.top();
                    }
                    if (H.xmatch(name, "NEXT", "N")) {  // HELP:: NEXT, Show Next (Decoded) instruction to be executed
                            InteractivePane.putf("%s","[NEXT] " + MainPane.showMemoryDecodedString(HW.getPC()));
                    }
                    if (H.xmatch(name, "RELOAD", "D")) {  // HELP:: RELOAD,Reload Current File
                        try {
                            TOY.MainPane.clear().selectAndClearBuffer1();
                            TOY.InteractivePane.clear().selectAndClearBuffer1();
                            TOY.HW.loadMemoryWithProgram(new In(TOY.currentFilename), 0x0100, TOY.programAsRead, TOY.label);
                            TOY.ipl();
                            TOY.getSingleton().run(-1, "READY");          // IPLs and returns, does not execute instructions
                            InteractivePane.putf("%s","[NEXT] " + MainPane.showMemoryDecodedString(HW.getPC()));
                            TOY.StatePane.state();
                            StatusAndMessagesPane.putf("%s", "Initial Progam Load (IPL) " + TOY.currentFilename + " Loaded.  Ready");
                        } catch (Exception e) {
                            Application.CRASH(e);
                        }
                    }
                    if (H.xmatch(name, "LOAD")) {  // HELP:: LOAD, Load Program From File
                        try {
                            szIn = TOY.MainPane.prompt(">source code filename > ");
                            TOY.MainPane.clear().selectAndClearBuffer1();
                            TOY.InteractivePane.clear().selectAndClearBuffer1();
                            TOY.currentFilename = szIn;
                            TOY.HW.loadMemoryWithProgram(new In(TOY.currentFilename), 0x0100, TOY.programAsRead, TOY.label);
                            TOY.ipl();
                            TOY.getSingleton().run(-1, "READY");          // IPLs and returns, does not execute instructions
                            InteractivePane.putf("%s","[NEXT] " + MainPane.showMemoryDecodedString(HW.getPC()));
                            TOY.StatePane.state();
                            StatusAndMessagesPane.putf("%s", "Initial Progam Load (IPL) " + TOY.currentFilename + " Loaded.  Ready");
                        } catch (Exception e) {
                            Application.CRASH(e);
                        }
                    }
                    if (name.equals("ROWS")) {        // HELP:: ROWS,Set Display Rows
                        int n = Pane.nPrompt(">Enter number of rows to display > ");
                        TOY.setDataRows(n);
                    }
                    if (name.equals("MPC")) {        // HELP:: MRO,Show R0 Indirect Memory
                        TOY.InteractivePane.putf("%s", "MPC: Display memory contents of mem[PC] " + pc);
                        TOY.InteractivePane.selectBuffer1();
                        TOY.InteractivePane.clear().selectAndClearBuffer1();
                        TOY.InteractivePane.memory(0x0000, TOY.dataRows);
                        TOY.InteractivePane.paint();

                    }

                    if (name.equals("M")) {  // HELP:: M,Show Memory
                        szIn = TOY.MainPane.prompt(">memory address > ");
                        TOY.InteractivePane.putf("%s",szIn);
                        switch (szIn) {
                            case "": 
                                TOY.InteractivePane.putf("%s", InteractivePane.memoryString(0x0000));
                                break;
                            default:
                                TOY.InteractivePane.putf("%s",InteractivePane.memoryString(H.fromHex(szIn)));
                                break;
                         }
                    }
                    if (name.equals("STATUS")) {      // HELP:: STATUS,Show Status
                        TOY.InteractivePane.putf("%s",  ""+TOY.HW.getPC());
                        TOY.InteractivePane.putf("%s", label.toString());
                        Map<String, Integer> map = TOY.label;

                        for (String key: map.keySet()) {
                            TOY.InteractivePane.putf("%s", H.LPad32(key)  + " " + "value : " + H.toHex(map.get(key)));
                        }


                    }
                    if (H.xmatch(name,"MONITOR","MON")) {      // HELP:: MONITOR,Track Memory
                        int[] mem = hw.getMem();
                        this.memory_monitor = H.fromHex( TOY.MainPane.prompt(">monitor address (" + f.get2() + ")> ", H.toHex(this.memory_monitor)));
                        TOY.InteractivePane.selectAndClearBuffer3().showHex(mem, this.memory_monitor);
                        TOY.StatePane.state();
                    }
                    if (H.xmatch(name,"E", "EDIT","EDI")) {      // HELP:: E,Edit Memory <Address>
                        int[] x = hw.getMem();
                        x[H.fromHex(f.get2())] = H.fromHex( TOY.MainPane.prompt(">edit (" + f.get2() + ")> ") );
                    }
                }
            }
    }
    private static void ipl() {
        try {
            TOY.HW.setPC(TOY.HW.getMem()[0x0000]);
            TOY.HW.initRegs().initStk();
            TOY.StatePane.state();
            TOY.MainPane.clear().selectAndClearBuffer1().showMemoryDecoded(TOY.HW.getPC()).top();
            TOY.InteractivePane.clear().selectAndClearBuffer1();
            TOY.ScreenPane.clear().selectAndClearBuffer1();
            InteractivePane.putf("%s","[NEXT] " + MainPane.showMemoryDecodedString(HW.getPC()));
        } catch (Exception e) {
            Application.CRASH(e);
        }
    }
    // run the TOY simulator with specified file
    public static void main(String[] args) { 
        H.assertion(args.length == 3, "invalid command-line options\nusage: java TOY filename.toy screen-height screen-width");
        int screenHeight = Integer.parseInt(args[1]);
        int screenWidth  = Integer.parseInt(args[2]);
        H.assertion(screenWidth  > 175, "screen is too narrow");
        H.assertion(screenHeight > 41,  "Screen height is too low");
        StdOut.print(ANSI.RESET);

        String filename = args[0];
        // ***************************************************************************************************************************************
        int rowOrigin = 5;
        int colOrigin = 1;
        //                              title,                    lines,   r,                         c,                        w,   ansicolor
        MainPane             = new Pane("Program and Memory",     24,      rowOrigin,                 colOrigin,                100, ANSI.CYAN);
        ScreenPane           = new Pane("Screen I/O",             24,      rowOrigin,                 MainPane.colRight(),       43, ANSI.YELLOW);
        StatePane            = new Pane("State",                  38,      rowOrigin,                 ScreenPane.colRight(),     32, ANSI.RED);
        InteractivePane      = new Pane("Interactive",            12,      MainPane.rowDown(),        colOrigin,                148, ANSI.BLUE);
        StatusAndMessagesPane= new Pane("Status and Messages",     1,      InteractivePane.rowDown(), colOrigin,                148, ANSI.PURPLE);
        Pane.setCOMMAND_ROW(StatusAndMessagesPane.rowDown());
        Pane.setCOMMAND_COLUMN(1);
        // ***************************************************************************************************************************************

        int pc = 0x0000;

        TOY toy = new TOY(filename, pc);
        MainPane.loadPaneBuffer(filename,             MainPane.getBuffer2() );
        MainPane.loadPaneBuffer("instructionset.txt", MainPane.getBuffer4() );
        MainPane.loadPaneBuffer("help.txt",           MainPane.getBufferHelp() );

        try {
          TOY.ipl();
          StatusAndMessagesPane.putf("%s", "Initial Progam Load (IPL) " + TOY.currentFilename + " Loaded.  Ready");
          toy.run(-1, "READY");     // IPLs and returns, does not execute instructions
          TOY.StatePane.state();
          toy.commandline();
        } catch (Exception e) {
              Application.CRASH(e);
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
