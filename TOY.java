/*************************************************************************
 *  Attribution:  https://introcs.cs.princeton.edu/java/home/
 *
 *  Compilation:  javac-introcs TOY.java
 *  Execution:    java-introcs TOY [--verbose] filename.toy 
 *  Dependencies: StdIn.java In.java
 *
 *  We use variables of type int to store the TOY registers, main
 *  memory, and program counter even though in TOY these quantities
 *  are 16 and 8 bit integers. Java does not have an 8-bit unsigned
 *  type. The type short in Java does represent 16-bit 2's complement
 *  integers, but using it requires alot of casting. Instead, we are
 *  careful to treat all of the variable as if they were the appropriate
 *  type so that the behavior truly models the TOY machine.
 *
 *  Each TOY instruction consists of 4 hex digits (16 bits). The leading (left-most) hex digit
 *  encodes one of the 16 opcodes. The second (from the left) hex digit refers to one of the
 *  16 registers, which we call the destination register and denote by d. The interpretation
 *  of the two rightmost hex digits depends on the opcode. With Format 1 opcodes, the third
 *  and fourth hex digits are each interpreted as the index of a register, which we call the
 *  two source registers and denote by s and t. For example, the instruction 1462 adds the
 *  contents of registers s = 6 and t = 2 and puts the result into register d = 4.
 *
 *  With Format 2 opcodes, the third and fourth hex digits (the rightmost 8 bits) are interpreted
 *  oas a memory address, which we denote by addr.
 *  For example, the instruction 9462 stores the contents of register d = 4 into memory location
 *  addr = 62. Note that there is no ambiguity between Format 1 and Format 2 instruction since
 *  each opcode has a unique format.
 *
 *          // Fetch and parse
 *          int inst = mem[pc++];            // fetch next instruction
 *          trace("INST", inst);
 *          int op   = (inst >> 12) &  15;   // get opcode (bits 12-15)
 *          int d    = (inst >>  8) &  15;   // get dest   (bits  8-11)
 *          int s    = (inst >>  4) &  15;   // get s      (bits  4- 7)
 *          int t    = inst         &  15;   // get t      (bits  0- 3)
 *          int addr = inst         & 255;   // get addr   (bits  0- 7)
 *
 *  % more multiply.toy
 *  10: 8AFF   read R[A]
 *  11: 8BFF   read R[B]
 *  12: 7C00   R[C] <- 0000
 *  13: 7101   R[1] <- 0001
 *  14: CA18   if (R[A] == 0) goto 18
 *  15: 1CCB   R[C] <- R[C] + R[B]
 *  16: 2AA1   R[A] <- R[A] - R[1]
 *  17: C014   goto 14
 *  18: 9CFF   write R[C]
 *  19: 0000   halt                        
 *
 *  % java-introcs TOY multiply.toy
 *  0002
 *  0004
 *  0008
 *
 *  % java-introcs TOY --verbose multiply.toy
 *  [core dump]
 *  0002
 *  0004
 *  0008
 *  [core dump]
 *
 *************************************************************************/

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class TOY { 
    static  StringBuffer sb = new StringBuffer(120);
    static  StringBuffer programAsRead = new StringBuffer(1024);
    static  Instruction lastInstruction = null;
    private final int STACKSIZE = 32;          // stack size in memory locations
    private int pc;                            // program counter
    private int stkptr;                        // stack pointer

    private int[] reg = new int[16];           // 16 registers
    private Registers R = new Registers(reg);
    private int[] mem = new int[0xFFFF];       // main memory locations
    private int[] stk = new int[STACKSIZE];    // stack memory locations

    // create a new TOY VM and load with program from specified file
    public TOY(String filename) {
        this(filename, 0x10);
    }

    public TOY(String filename, int pc) {
        final int PROGRAMMEMORY=16;
        int loadptr=PROGRAMMEMORY;

        for (int i=0;i<256;i++) mem[i]=i;
        this.pc = pc;
        this.stkptr = 0;
        In in = new In(filename);


       /****************************************************************
        *  Read Program File
        *  Read in memory location and instruction.         
        *  A valid input line consists of 2 hex digits followed by a 
        *  colon, followed by any number of spaces, followed by 4
        *  hex digits. The rest of the line is ignored.
        ****************************************************************/
        String regexpm = "^([0-9A-Fa-f]{2}):[ \t]*([0-9A-Fa-f]{4})";
        String regexp = "^[ \t]*([0-9A-Fa-f]{4})[ \t]*([0-9A-Fa-f]{4})";
        String asmregexp = "^([0-9A-Fa-f]{2}):[ \t]*([A-Fa-f]{3})";

        String memregexp = "^(MEM)[ \t]*([0-9A-Fa-f]{4})";
        String wordregexp = "^([0-9A-Fa-f]{4}$)";
        String dwordregexp = "^([0-9A-Fa-f]{4})[ \t]*([0-9A-Fa-f]{4})";
        
        
        Pattern pattern = Pattern.compile(regexp);
        Pattern asmpattern = Pattern.compile(asmregexp);
        Pattern mempattern = Pattern.compile(memregexp);
        Pattern wordpattern = Pattern.compile(wordregexp);
        Pattern dwordpattern = Pattern.compile(dwordregexp);

        while (in.hasNextLine()) {
            String line = in.readLine();
            programAsRead.append(line + "\n");
            Matcher memmatcher = mempattern.matcher(line);
            if (memmatcher.find()) {
                loadptr = fromHex(memmatcher.group(2));
                continue;
            }
            Matcher dwordmatcher = dwordpattern.matcher(line);
            if (dwordmatcher.find()) {
                mem[loadptr] = fromHex(dwordmatcher.group(1));
                loadptr++;
                mem[loadptr] = fromHex(dwordmatcher.group(2));
                loadptr++;
                continue;
            }
            Matcher wordmatcher = wordpattern.matcher(line);
            if (wordmatcher.find()) {
                mem[loadptr] = fromHex(wordmatcher.group(1));
                loadptr++;
                continue;
            }

            Matcher matcher = pattern.matcher(line);
            Matcher asmmatcher = asmpattern.matcher(line);
            if (matcher.find()) {
                //int addr = fromHex(matcher.group(1));
                int addr = loadptr;
                //int inst = fromHex(matcher.group(2));
                int inst = fromHex(matcher.group(1));
                mem[addr] = inst;
                inst = fromHex(matcher.group(1));
                mem[addr++] = inst;
                loadptr++;
                loadptr++;
            }
            else {
                if (asmmatcher.find()) {
                    int addr = fromHex(matcher.group(1));
                    int inst = -1;
                    String sz = matcher.group(2);
                        switch (sz) {
                            case  "HLT": inst = 0;         break;    // halt
                            case  "ADD": inst = 1;         break;    // add
                            case  "SUB": inst = 2;         break;    // sub
                            case  "PSH": inst = 16;        break;    // push
                            case  "POP": inst = 17;        break;    // pop 
                        }
                    mem[addr] = inst;
                }
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
    // write to an array of hex integers, 8 per line to standard output
    public static void showhex(int[] a) {
        final int M = 64;
        final int C = 16;
        int count = (a.length >= M) ? M : a.length;
        StdOut.print(toHex(0) + ": ");
        for (int i = 0; i < count; i++) {
            StdOut.print(toHex(a[i]) + " ");
            if (i % C == (C-1)) {
                StdOut.println();
                if (i+1 < count) StdOut.print(toHex(i+1) + ": ");
            }
        }
    }
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

    // write to an array of hex integers, 8 per line to standard output
    public static void show(int[] a) {
        int count = (a.length >= 16) ? 16 : a.length;
        for (int i = 0; i < count; i++) {
            StdOut.print(toHex(a[i]) + " ");
            if (i % 8 == 7) StdOut.println();
        }
    }
    public static void showreg(int[] a) {
        int count = a.length;
        for (int i = 0; i < count; i++) {
            StdOut.print("[" + toHexShort(i) + "] " + toHex(a[i]) + " ");
            if (i % 8 == 7) StdOut.println();
        }
    }


    // print core dump of TOY to standard output
    public void dump(String sz) {
        StdOut.printf("%s  PC: %s\n", sz, toHex(pc) );
        StdOut.println("Registers:");
        showreg(reg);
        StdOut.println("Main memory in Hex (16 bit words):");
        showhex(mem);
        StdOut.print("Stack memory in Hex (16 bit words):");
        StdOut.printf("  SP: %s\n", toHex(stkptr));
        showhex(stk);
    }
    static public void trace(String sz, int i) {
        System.out.println(sz + " " + toHex(i));
    }

    public void run() throws Exception {
        int idx = 0;
        int n = 0;
        Instruction I = null;
        boolean haltflag = false;

        sb.append(String.format("%26s %6s %2s %2s  %4s\n","Instruction", "D", "S", "T", "ADDR"));
        while (true) {

            // Fetch and parse
               try {
                   I = new Instruction(mem,pc); 
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

            //StdOut.printf("%s  %s x%s d%d\n", I.opString(), toHex(I.getLowword()), toHexShort(I.getOp()), I.getOp());



            // stdin 
       //     if ((addr == 255 && op == 8) || (reg[t] == 255 && op == 10))
       //         mem[255] = fromHex(StdIn.readString());

            // Execute
            switch (op) {
                case 0x00: haltflag=true;                       break;    // halt
                case 0x01: reg[d] = reg[s] +  reg[t];           break;    // add
                case 0x02: reg[d] = reg[s] -  reg[t];           break;    // subtract
                case 0x03: reg[d] = reg[s] &  reg[t];           break;    // bitwise and
                case 0x04: reg[d] = reg[s] ^  reg[t];           break;    // bitwise xor
                case 0x05: reg[d] = reg[s] << reg[t];           break;    // shift left
                case 0x06: reg[d] = (short) reg[s] >> reg[t];   break;    // shift right
                case 0x07: reg[d] = addr;                       break;    // load address
                case 0x08: reg[d] = mem[addr];                  break;    // load
                case 0x09: mem[addr] = reg[d];                  break;    // store
                case 0x0A: reg[d] = mem[reg[t] & 255];          break;    // load indirect
                case 0x0B: mem[reg[t] & 255] = reg[d];          break;    // store indirect
                case 0x0C: if ((short) reg[d] == 0) pc = addr;  break;    // branch if zero
                case 0x0D: if ((short) reg[d] >  0) pc = addr;  break;    // branch if positive
                case 0x0E: pc = reg[d];                         break;    // jump indirect
                case 0x0F: reg[d] = pc; pc = addr;              break;    // jump and link

                // My Instructions
                case 0x10: stkptr++;stk[stkptr] = mem[addr];    break;    // push address
                case 0x11: stkptr++;stk[stkptr] = reg[d];       break;    // push register
                case 0x12: reg[d] = stk[stkptr];
                           stkptr--; 
                           if (stkptr<0) stkptr=0;              break;    // pop to register

                case 0x13: reg[d] = reg[d] + 1;                 break;    // increment register
                case 0x14: reg[d] = reg[d] - 1;                 break;    // decrement register
                case 0x15: reg[d] = reg[d] << 1;                break;    // shift reg left
                case 0x16: reg[d] = reg[d] >> 1;                break;    // shift reg right
                case 0x17: stk[++stkptr] = pc; pc = addr;       break;    // push pc and link
                case 0x18: pc = addr;                           break;    // link
                case 0x20: pc = pc;                             break;    // NOP

                case 0x50: StdOut.print(reg[d]);                break;    // reg char out
                case 0x51: StdOut.print(mem[addr]);             break;    // mem char out
                case 0x52: idx=addr; 
                           while (mem[idx]!=0) {
                               StdOut.print( String.valueOf((char) mem[idx]) );
                               idx++;
                           }
                           break;                                         // string out 16 bit
                case 0x53: idx=addr; 
                           boolean flip = true;
                           n = (mem[idx] >> 8) & 0x00FF; 
                           while (n != 0) {
                               StdOut.print( String.valueOf((char) n) );
                               flip = !flip; 
                               n = (flip) ? (mem[idx] >> 8) & 0x00FF  : (mem[idx] >> 0) & 0x00FF; 
                               if (!flip) idx++;
                           }
                           break;                                         // string out 8 bit
            }

            // stdout
       //  if ((addr == 255 && op == 9) || (reg[t] == 255 && op == 11))
       //         StdOut.println(toHex(mem[255]));
            sb.append(I.toString() + "\n");
            StdOut.printf("%s -- %s -- %s\n", I.toString(), TOY.toHex(pc),R.toStringVars());
            // halt
            if (haltflag) break;

            reg[0] = 0;                // ensure reg[0] is always 0
            reg[d] = reg[d] & 0xFFFF;  // don't let reg[d] overflow a 16-bit integer
            pc = pc & 0xFFFF;          // don't let pc overflow an 16-bit integer

        }
    }


    // run the TOY simulator with specified file
    public static void main(String[] args) { 
        // -v or --verbose is an optional first command-line argument
        boolean isVerbose = false;
        if (args.length > 0 && (args[0].equals("-v") || args[0].equals("--verbose"))) {
            isVerbose = true;
        }

        // the filename is the next command-line argument
        String filename = null;
        if (!isVerbose && args.length > 0) filename = args[0];
        if ( isVerbose && args.length > 1) filename = args[1];

        // the initial value of the PC is an optional last command-line argument
        int pc = 0x10;
        if (!isVerbose && args.length > 1) pc = fromHex(args[1]);
        if ( isVerbose && args.length > 2) pc = fromHex(args[2]);

        // no command-line arguments
        if (args.length == 0) {
            System.err.println("TOY:   invalid command-line options");
            System.err.println("usage: java-introcs TOY [--verbose] filename.toy [pc]");
            return;
        }

        TOY toy = new TOY(filename, pc);
        if (isVerbose) {
            toy.dump("Before Executing");
            StdOut.println("Terminal");
        }

        try {
            toy.run();
        } catch (Exception e) {
             StdOut.printf("%s\n", sb.toString());
             StdOut.println(lastInstruction.toString() + "\n");

             System.out.println("Caught Exception: "+ e.getMessage());
             e.printStackTrace();
             System.exit(1);
        }

        if (isVerbose) {
            toy.dump("After Executing");

        }

    }
}

class Instruction {
    
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
//          if (this.d > 15) throw new InstructionValueException(this, "d too big");
//          if (this.s > 15) throw new InstructionValueException(this, "s too big");
//          if (this.t > 15) throw new InstructionValueException(this, "t too big");
            //System.out.println(toHex(addr));System.exit(1);
    }

    public static String toDec(int n) {
        return String.format("%05d", n & 0xFFFF);
    }
    public static String toDecShort(int n) {
        return String.format("%03d", n & 0xFFFF);
    }
    public static String toHex(int n) {
        return String.format("%04X", n & 0xFFFF);
    }
    // return a 2-digit hex string corresponding to 8-bit integer n
    public static String toHexShort(int n) {
        return String.format("%02X", n & 0xFFFF);
    }
    public String toString() {
        return opString();
    }
    private String opString() {
            String sz = "";
            switch (op) {
                case  0x00: sz = "halt";               break;    // halt
                case  0x01: sz = "add";                break;    // add
                case  0x02: sz = "subtract";           break;    // subtract
                case  0x03: sz = "bitwise and";        break;    // bitwise and
                case  0x04: sz = "bitwise xor";        break;    // bitwise xor
                case  0x05: sz = "shift left";         break;    // shift left
                case  0x06: sz = "shft right";         break;    // shift right
                case  0x07: sz = "load address";       break;    // load address
                case  0x08: sz = "load";               break;    // load
                case  0x09: sz = "store";              break;    // store
                case  0x0A: sz = "load indirect";      break;    // load indirect
                case  0x0B: sz = "store indirect";     break;    // store indirect
                case  0x0C: sz = "branch if zero";     break;    // branch if zero
                case  0x0D: sz = "branch if pos";      break;    // branch if positive
                case  0x0E: sz = "jump indirect";      break;    // jump indirect
                case  0x0F: sz = "jump and link";      break;    // jump and link

                case  0x10: sz = "push address";       break;    // stack push address
                case  0x11: sz = "push reg";           break;    // stack push register
                case  0x12: sz = "pop to reg";         break;    // stack pop to register
                case  0x13: sz = "increment reg";      break;    // increment register
                case  0x14: sz = "decrement reg";      break;    // decrement register
                case  0x15: sz = "shift reg left";     break;    // shift reg left
                case  0x16: sz = "shift reg right";    break;    // shift reg right
                case  0x17: sz = "push pc and link";   break;    // push pc and link
                case  0x18: sz = "link";               break;    // link
                case  0x20: sz = "NOP";                break;    // NOP
                case  0x50: sz = "reg char out";       break;    // reg char out
                case  0x51: sz = "mem char out";       break;    // mem char out
                case  0x52: sz = "string out";         break;    // string out
                case  0x53: sz = "string out 8bit";    break;    // string out 8bit
            }
            return String.format("%s %s %s %-16s %s %s %s %s - %s %s %s %s",
            toHex(pc),toHex(highword), toHex(lowword), sz, 
            toHexShort(this.d),toHexShort(this.s),toHexShort(this.t), toHex(this.addr),
            toDecShort(this.d),toDecShort(this.s),toDecShort(this.t), toDec(this.addr));
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
        for (int i = 10; i < this.reg.length; i++) sb.append(TOY.toHex(this.reg[i])).append(" ");
        return sb.toString();
    }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < this.reg.length; i++) sb.append(TOY.toHex(this.reg[i])).append(" ");
        return sb.toString();
    }
}
