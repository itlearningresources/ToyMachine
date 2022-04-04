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
public static Decode decodeInstruction(int lowword, int highword) {
//          setPc(pc);
//          setHighword(mem[pc++]);                   // fetch next word
//          setInst(highword); 
//          setOp( (highword >> 8)  & 0x00FF);        // get opcode
//          setD(  (highword >>  4) & 0x000F);        // get dest  
//          setLowword(mem[pc++]);                    // fetch next word
//          this.s    = (highword >>  0) & 0x000F;    // get s    
//          this.t    = lowword          & 0x00FF;    // get t   
//          this.addr = lowword          & 0xFFFF;    // get addr
    int lowbyte = 0;
    int highbyte = 0;
    Decode decode= null;
    int opCode = ((lowword >> 8) & 0x00FF);        // high byte of the lowword is the opcode
    int register = ((lowword) >> 4  & 0x000F);     // high nibble of low byte of the lowword is a register d
    int address = ((highword)   & 0xFFFF);         // high word can be an address
    int wordValue = ((highword)   & 0xFFFF);       // high word can be a 16 bit value

    String rD = H.toHexNibble(((lowword >>    4) & 0x000F));   // get register d (dest)
    String rS = H.toHexNibble(((lowword >>    0) & 0x000F));   // get register s (source)
    String rT = H.toHexNibble(((highword >>  12) & 0x000F));   // get register t (other)
    String rW = H.toHex(((highword)   & 0xFFFF));              // high word

//  int[] reg = hw.getReg();
//  int[] mem = hw.getMem();
    String sz = "";
    switch ( opCode ) {
        case 0x00: decode=new Decode(DF("halt"),                                 "halt",        "haltflag = true");break;
        case 0x01: decode=new Decode(DF("reg[%s] = reg[%s] + reg[%s]",rD,rS,rW), "add",         "reg[d] = reg[s] + reg[t]");break;
        case 0x02: decode=new Decode(DF("reg[%s] = reg[%s] - reg[%s]",rD,rS,rW), "subtract",    "reg[d] = reg[s] - reg[t]");break;
        case 0x03: decode=new Decode(DF("reg[%s]++", rD),                                       "increment register", "reg[d]++"); break;
        case 0x04: decode=new Decode(DF("reg[%s]--", rD),                                       "decrement register", "reg[d]--"); break;
        case 0x05: decode=new Decode(DF("reg[%s] = reg[%s] + reg[%s]",rD,rD,rS),"accumulate",   "reg[d] = reg[d] + reg[s]"); break;
        case 0x06: decode=new Decode(DF("reg[%s] = reg[%s] - reg[%s]",rD,rD,rS),"de-accumulate","reg[d] = reg[d] - reg[s]"); break;
        case 0x10: 
                   sz = String.format("load register %s with %s", H.toHexShort(register), H.toHex(address)) ;
                   decode=new Decode(sz, "load register with addr", "reg[d] = word");break;
        case 0x11: decode=new Decode("load register with memory", "reg[d] = mem[word]");   break;
        case 0x12: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x13: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x14: decode=new Decode("store reg to mem", "mem[addr] = reg[d]");break;
        case 0x15: decode=new Decode("store reg to mem indirect", "mem[reg[d] & 0x0FFFF] = reg[s]"); break;
        case 0x16: decode=new Decode("load indirect", "reg[d] = mem[reg[s] & 0xFFFF]");break;
        case 0x17: 
                   sz = String.format("load the index register with %s", H.toHex(address)) ;
                   decode=new Decode(sz,"load the index register", "indexregister = word");break;
        case 0x18: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x19: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x1A: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x1B: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x1C: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x1D: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x1E: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x1F: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x20: decode=new Decode("jump", "pc = addr");break;
        case 0x21: decode=new Decode("branch if zero", "if (reg[d] == 0) pc = addr");  break;
        case 0x22: decode=new Decode("branch if not zero", "if (reg[d] != 0) pc = addr");  break;
        case 0x23: decode=new Decode("pop and link if zero", "if (reg[d] == 0) pc = popstk");  break;
        case 0x24: decode=new Decode("pop and link if not zero", "if (reg[d] != 0) pc = popstk");  break;
        case 0x25: decode=new Decode("branch if pos", "if (reg[d] >  0) pc = addr");break;
        case 0x26: decode=new Decode("jump indirect", "pc = reg[d]");break;
        case 0x27: decode=new Decode("jump and link", "reg[d] = pc; pc = addr");break;
        case 0x28: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x29: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x2A: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x2B: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x2C: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x2D: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x2E: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x2F: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x30: decode=new Decode("push address", "push addr");break;
        case 0x31: decode=new Decode("push register", "push reg[d]");break;
        case 0x32: decode=new Decode("pop to register", "pop to reg[d]");break;
        case 0x33: decode=new Decode("pop and link", "return");break;
        case 0x34: decode=new Decode("Push This", "push this addr");break;
        case 0x35: decode=new Decode("push pc and link", "push pc and pc = addr");break;
        case 0x36: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x37: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x38: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x39: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x3A: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x3B: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x3C: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x3D: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x3E: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x3F: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x40: decode=new Decode("bitwise and", "reg[d] = reg[s] & reg[t]");break;
        case 0x41: decode=new Decode("bitwise or",  "reg[d] = reg[s] ^ reg[t]");break;
        case 0x42: decode=new Decode("shift left", "reg[d] = reg[s] << reg[t]");break;
        case 0x43: decode=new Decode("shift right", "reg[d] = reg[s] >> reg[t]"); break;
        case 0x44: decode=new Decode("shift reg left", "reg[d] = reg[d] << 1");break;
        case 0x45: decode=new Decode("shift reg right", "reg[d] = reg[d] >> 1");break;
        case 0x46: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x47: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x48: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x49: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x4A: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x4B: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x4C: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x4D: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x4E: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x0F: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x50: decode=new Decode("NOP", "NOP");break;
        case 0x61: decode=new Decode("reg char out", "reg[d] char out");break;
        case 0x62: decode=new Decode("mem char out", "mem[addr] char out");break;
        case 0x63: decode=new Decode("string 10 16b", "string out 16b");break;
        case 0x64: decode=new Decode("string out 8b", "string out 8b");break;
        case 0x65: decode=new Decode("int to ascii", "int to ascii");break;
        case 0x66: decode=new Decode("mem int to ascii", "mem int to ascii");break;
        case 0x67: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x68: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x69: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x6A: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x6B: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x6C: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x6D: decode=new Decode("reserved", "reserved"); break;                            // reserved
        case 0x6E: decode=new Decode("reserved", "reserved"); break;                            // reserved
        default:   decode=new Decode("iundefined instruction", "undefined instruction"); break; // reserved
    }
    decode.setOpCode(opCode);
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

