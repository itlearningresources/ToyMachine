import java.util.HashMap;


public final class InstructionSet {
    private boolean[] b = new boolean[256];
    private HashMap<Integer, Instruction_Details> details = new HashMap<Integer, Instruction_Details>();
    public InstructionSet() {
        for (int i=0;i<256;i++) b[i]=false;
    }
    public void add(int o, String n, String d) {
        if (!b[o]) {
            b[o]=true; 
            details.put(o, new Instruction_Details(o,n,d));
        }
    }
    public Instruction_Details get(int o) {
        return details.get(o);
    }

private static String[] initDecodeArray(String sz1, String sz2) {
    String[] arr = new String[2];
    arr[0]=sz1;
    arr[1]=sz2;
    return arr;
}
    public final static String DF(String format, String ... args) {
    //String.format("load register %s with %s", H.toHexShort(register), H.toHex(address)) ;
      return new java.util.Formatter().format(format, args).toString();
    }
    public final static String DF(String sz) {
      return sz;
    }

public static String assemblyHex(String mnemonic, String operand) {
    return H.toHex(assembly(mnemonic, operand));
}
public static int assembly(String mnemonic, String operand) {
    int n = 0;
    int o = H.fromHex(operand) & 0x00FF;
    switch (mnemonic) {
        case "HALT": n=0x00; break;
        case "HLT":  n=0x00; break;
        case "ADD":  n=0x01; break;
        case "SUB":  n=0x02; break;
        case "INC":  n=0x03; break;
        case "DEC":  n=0x04; break;
        case "ACM":  n=0x05; break;
        case "DCM":  n=0x06; break;
        case "LRA":  n=0x10; break;
        case "LRM":  n=0x11; break;

        case "SRM":  n=0x14; break;  // Store register to memory
        case "IRM":  n=0x15; break;  // Indirect register to memory
        case "ILD":  n=0x16; break;  // Indirect Load
        case "LIR":  n=0x17; break;  // Load index register

        case "JMP":  n=0x20; break;  // Jump to address in word
        default:     System.out.println("\n\nABEND: Bad Assembly Mnemonic\n\n"); System.exit(1); break;
    }
    n = n << 8;
    return n | o;
}
public static Decode decodeInstruction(int lowword, int highword) {
    Decode decode= null;
    DecodedInstruction d = DecodedInstruction.decode(lowword, highword);
    switch ( d.opCode ) {
        case 0x00: decode=new Decode(DF("halt"),                                       "halt",      "haltflag = true");break;
        case 0x01: decode=new Decode(DF("reg[%s] = reg[%s] + reg[%s]", d.D, d.S, d.T), "add",       "reg[d] = reg[s] + reg[t]");break;
        case 0x02: decode=new Decode(DF("reg[%s] = reg[%s] - reg[%s]", d.D, d.S, d.T), "subtract", "reg[d] = reg[s] - reg[t]");break;
        case 0x03: decode=new Decode(DF("reg[%s]++", d.D),                             "increment register", "reg[d]++"); break;
        case 0x04: decode=new Decode(DF("reg[%s]--", d.D),                             "decrement register", "reg[d]--"); break;
        case 0x05: decode=new Decode(DF("reg[%s] = reg[%s] + reg[%s]",d.D,d.D,d.S),    "accumulate",    "reg[d] = reg[d] + reg[s]"); break;
        case 0x06: decode=new Decode(DF("reg[%s] = reg[%s] - reg[%s]",d.D,d.D,d.S),    "de-accumulate", "reg[d] = reg[d] - reg[s]"); break;
        case 0x10: decode=new Decode(DF("load register %s with %s", d.D, d.W) ,        "load register with addr", "reg[d] = word");break;
        case 0x11: decode=new Decode(DF("reg[%s] = mem[%s]", d.D, d.W),                "load register with memory", "reg[d] = mem[word]");   break;
        case 0x12: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break; // reserved
        case 0x13: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break; // reserved
        case 0x14: decode=new Decode(DF("mem[%s] = reg[%s]", d.W, d.D),                "store reg to mem", "mem[addr] = reg[d]");break;
        case 0x15: decode=new Decode(DF("mem[reg[%s] & 0x0FFFF] = reg[%s]", d.D, d.S), "store reg to mem indirect", "mem[reg[d] & 0x0FFFF] = reg[s]"); break;
        case 0x16: decode=new Decode(DF("reg[%s] = mem[reg[%s] & 0xFFFF]", d.D, d.S),  "load indirect", "reg[d] = mem[reg[s] & 0xFFFF]");break;
        case 0x17: decode=new Decode(DF("load the index register with %s", d.W) ,      "load the index register", "indexregister = word");break;
        case 0x18: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x19: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x1A: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x1B: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x1C: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x1D: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x1E: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x1F: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x20: decode=new Decode(DF("pc = %s", d.W),                               "jump", "pc = addr");break;
        case 0x21: decode=new Decode(DF("if (reg[%s] == 0) pc = %s", d.D, d.W), "branch if zero", "if (reg[d] == 0) pc = addr");  break;
        case 0x22: decode=new Decode(DF("if (reg[%s] != 0) pc = %s",d.D, d.W), "branch if not zero", "if (reg[d] != 0) pc = addr");  break;
        case 0x23: decode=new Decode(DF("if (reg[%s] == 0) pc = popstk",d.D), "pop and link if zero", "if (reg[d] == 0) pc = popstk");  break;
        case 0x24: decode=new Decode(DF("if (reg[%s] != 0) pc = popstk", d.D), "pop and link if not zero", "if (reg[d] != 0) pc = popstk");  break;
        case 0x25: decode=new Decode(DF("if (reg[%s] >  0) pc = %s",d.D, d.W), "branch if pos", "if (reg[d] >  0) pc = addr");break;
        case 0x26: decode=new Decode(DF("pc = reg[%s]", d.D), "jump indirect", "pc = reg[d]");break;
        case 0x27: decode=new Decode(DF("reg[%s] = pc; pc = %s", d.D, d.W), "jump and link", "reg[d] = pc; pc = addr");break;
        case 0x28: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x29: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x2A: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x2B: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x2C: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x2D: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x2E: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x2F: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x30: decode=new Decode(DF("push %s", d.W),                               "push address", "push addr");break;
        case 0x31: decode=new Decode(DF("push reg[%s]", d.D),                          "push register","push reg[d]");break;
        case 0x32: decode=new Decode(DF("pop to reg[d]", d.D),                         "pop to register", "pop to reg[d]");break;
        case 0x33: decode=new Decode(DF("pop and link"),                               "pop and link", "return");break;
        case 0x34: decode=new Decode(DF("push this addr"),                             "Push This", "push this addr");break;
        case 0x35: decode=new Decode(DF("push pc and pc = addr"),                      "push pc and link", "push pc and pc = addr");break;
        case 0x36: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x37: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x38: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x39: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x3A: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x3B: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x3C: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x3D: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x3E: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x3F: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x40: decode=new Decode(DF("reg[%s] = reg[%s] & reg[%s]",d.D, d.S, d.T),  "bitwise and", "reg[d] = reg[s] & reg[t]");break;
        case 0x41: decode=new Decode(DF("reg[%s] = reg[%s] ^ reg[%s]",d.D, d.S, d.T),  "bitwise or",  "reg[d] = reg[s] ^ reg[t]");break;
        case 0x42: decode=new Decode(DF("reg[%s] = reg[%s] << reg[%s]",d.D, d.S, d.T), "shift left", "reg[d] = reg[s] << reg[t]");break;
        case 0x43: decode=new Decode(DF("reg[%s] = reg[%s] >> reg[%s]",d.D, d.S, d.T), "shift right", "reg[d] = reg[s] >> reg[t]"); break;
        case 0x44: decode=new Decode(DF("reg[%s] = reg[%s] << 1",d.D, d.D),            "shift reg left", "reg[d] = reg[d] << 1");break;
        case 0x45: decode=new Decode(DF("reg[%s] = reg[%s] >> 1",d.D, d.D),            "shift reg right", "reg[d] = reg[d] >> 1");break;
        case 0x46: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x47: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x48: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x49: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x4A: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x4B: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x4C: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x4D: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x4E: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x0F: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x50: decode=new Decode(DF("NOP"),                                        "NOP", "NOP");break;
        case 0x61: decode=new Decode(DF("reg[%s] char out", d.D),                      "reg char out", "reg[d] char out");break;
        case 0x62: decode=new Decode(DF("mem[%s] char out", d.W),                      "mem char out", "mem[addr] char out");break;
        case 0x63: decode=new Decode(DF("string out 16b"),                             "string 10 16b", "string out 16b");break;
        case 0x64: decode=new Decode(DF("string out 8b"),                              "string out 8b", "string out 8b");break;
        case 0x65: decode=new Decode(DF("int to ascii"),                               "int to ascii", "int to ascii");break;
        case 0x66: decode=new Decode(DF("mem int to ascii"),                           "mem int to ascii", "mem int to ascii");break;
        case 0x67: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x68: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x69: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x6A: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x6B: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x6C: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x6D: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x6E: decode=new Decode(DF("reserved"),                                   "reserved", "reserved"); break;                            // reserved
        case 0x70: decode=new Decode(DF("system call %s @ 0x%s", d.D, d.W),            "reserved", "reserved"); break;                            // reserved
        default:   decode=new Decode(DF("undefined instruction"),                      "undefined instruction", "undefined instruction"); break; // reserved
    }
    decode.setOpCode(d.opCode);
    return decode;
}
}

final class Instruction_Details {
    private int opcode;
    private String name;
    private String description;
    public Instruction_Details(int o, String n, String d) {
        this.opcode = 0;
        this.name = n;
        this.description = d;
    }
    public String getName() { return name;}
    public String getDescription() { return description; }
    public String getDesc() { return description; }

}

