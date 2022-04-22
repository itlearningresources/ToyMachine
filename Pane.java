import java.util.Scanner;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.ArrayList;

class MyOut {
    private java.io.PrintStream out;
    public MyOut (java.io.PrintStream out) {
        super();
        this.out = out;
    }
    public void print(String ... a) {
        StringBuffer sb = new StringBuffer();
        for (int i=0;i<a.length;i++) sb.append(a[i]);
        this.out.print(sb.toString());
    }
}

public class Pane {
    private ArrayList<String> buffer = null;
    private ArrayList<String> b0 = new ArrayList<String>();  // decoded memory
    private ArrayList<String> b1 = new ArrayList<String>();
    private ArrayList<String> b2 = new ArrayList<String>();
    private ArrayList<String> b3 = new ArrayList<String>();
    private ArrayList<String> b4 = new ArrayList<String>();
    private ArrayList<String> b5 = new ArrayList<String>();
    private ArrayList<String> temp = new ArrayList<String>();
    private ArrayList<String> help = new ArrayList<String>();
    private String title;
    private int r;
    private int c;
    private int paneRow;
    private int paneCol;
    private int w = 80;
    private int lines;
    private int count;
    private int rpos;
    private int cpos;
    private String bordercolor = ANSI.WHITE;
    private final String initalbordercolor;
    private int refreshpoint;
    private static int COMMAND_ROW = 44;
    private static int COMMAND_COLUMN = 1;
    // forceeUnicode UTF-8 encoding; otherwise it's system dependent
    private static final String CHARSET_NAME = "UTF-8";
    // assume language = English, country = US for consistency with StdIn
    private static final Locale LOCALE = Locale.US;

    public static int setCOMMAND_ROW(int n)    { Pane.COMMAND_ROW    = n; return n;}
    public static int setCOMMAND_COLUMN(int n) { Pane.COMMAND_COLUMN = n; return n;}
    public static int getCOMMAND_ROW() { return Pane.COMMAND_ROW;}
    public static int getCOMMAND_COLUMN() { return Pane.COMMAND_COLUMN;}
 
    public void bufferHelp(int n) { this.buffer = help; if (n>-1) this.refresh(n);}


    public void bufferHelp() { this.buffer = help;}
    public void buffertemp() { this.buffer = temp;}
    public ArrayList<String>  getCurrentBuffer() { return  buffer;}
    public ArrayList<String>  getBuffer0() { return  b0;}
    public ArrayList<String>  getBuffer1() { return  b1;}
    public ArrayList<String>  getBuffer2() { return  b2;}
    public ArrayList<String>  getBuffer3() { return  b3;}
    public ArrayList<String>  getBuffer4() { return  b4;}
    public ArrayList<String>  getBuffer5() { return  b5;}
    public ArrayList<String>  getBufferHelp() { return help;}

    public Pane selectBuffer0() { this.buffer = b0; return this; }
    public Pane selectBuffer1() { this.buffer = b1; return this; }
    public Pane selectBuffer2() { this.buffer = b2; return this; }
    public Pane selectBuffer3() { this.buffer = b3; return this; }
    public Pane selectBuffer4() { this.buffer = b4; return this; }
    public Pane selectBuffer5() { this.buffer = b5; return this; }

    public Pane clearPaneAndBuffer()  { this.clear(); buffer.clear(); return this; }
    public Pane clearPaneAndBuffer0() { this.buffer=b0; this.clear(); this.buffer.clear(); return this; }
    public Pane clearPaneAndBuffer1() { this.buffer=b1; this.clear(); this.buffer.clear(); return this; }
    public Pane clearPaneAndBuffer2() { this.buffer=b2; this.clear(); this.buffer.clear(); return this; }
    public Pane clearPaneAndBuffer3() { this.buffer=b3; this.clear(); this.buffer.clear(); return this; }
    public Pane clearPaneAndBuffer4() { this.buffer=b4; this.clear(); this.buffer.clear(); return this; }
    public Pane clearPaneAndBuffer5() { this.buffer=b5; this.clear(); this.buffer.clear(); return this; }

    public Pane selectAndClearBuffer()  { this.buffer.clear(); return this; }
    public Pane selectAndClearBuffer0() { this.buffer=b0; this.buffer.clear(); return this; }
    public Pane selectAndClearBuffer1() { this.buffer=b1; this.buffer.clear(); return this; }
    public Pane selectAndClearBuffer2() { this.buffer=b2; this.buffer.clear(); return this; }
    public Pane selectAndClearBuffer3() { this.buffer=b3; this.buffer.clear(); return this; }
    public Pane selectAndClearBuffer4() { this.buffer=b4; this.buffer.clear(); return this; }
    public Pane selectAndClearBuffer5() { this.buffer=b5; this.buffer.clear(); return this; }

    public ArrayList<String> getBuffer() { return buffer; }
    public ArrayList<String> setBuffer(int n, ArrayList<String> b) { 
        switch (n) {
            case 1: this.b1 = b;
            case 2: this.b2 = b;
            case 3: this.b3 = b;
            case 4: this.b4 = b;
         }
         return b;
    }
    public ArrayList<String> getBuffer(int n) { 
        ArrayList<String> Ret = null;
        switch (n) {
            case 1: Ret = this.b1;
            case 2: Ret = this.b2;
            case 3: Ret = this.b3;
            case 4: Ret = this.b4;
         }
         return Ret;
    }

    private static Pane[] panes = {null};
    public static Pane[] getPanes() { return panes; }

    private static Pane msgPane = null;
    public  static Pane setMsgPane(Pane p) { msgPane = p; return p;}
    public  static Pane getMsgPane() { return msgPane; }

    // send output here
    private static java.io.PrintStream out;
    private static MyOut myout;

    // Constructors and Factories
    public static Pane paneFactory(String title, int lines, int r, int c, int w, String ansicolor) {
        return new Pane(title, lines, r, c, w, ansicolor);
    }
    public static Pane paneFactory(String title, int lines, int r, int c, int w) {
        return new Pane(title, lines, r, c, w, ANSI.WHITE);
    }

    public Pane(String title, int lines, int r, int c, int w, String ansicolor) {
        this.title = title;
        this.buffer = b1;
        this.bordercolor = ansicolor;
        this.initalbordercolor = ansicolor;
        int rr;
        int cc;
 
    // this is called before invoking any methods
        try {
            out = System.out;
            myout = new MyOut(System.out);

            //out = new PrintWriter(new OutputStreamWriter(System.out, CHARSET_NAME), true);
        }
        catch (Exception e) {
            System.out.println(e);
        }

        Pane[] ps = new Pane[panes.length+1];
        for (int i=0;i<panes.length;i++) ps[i] = panes[i];
        ps[ps.length-1] = this; 
        panes = ps;

        this.w = w;
        this.r = r;
        this.c = c;
        this.paneRow = this.r;
        this.paneCol = this.c+1;
        //String dashes = (new String(new char[w]).replace("\0", "-"));
        this.lines = lines;
        this.count = 1;
        // this next line seems like it is old and should be deleted
        // this.out.print("\033[" + (r-1) + ";" + c + "H" + "+ " + dashes + " +");
        clear();



    }
    public int getWritableWidth() {
        int f = 4;
        return ((this.w-f)>0) ? this.w-f : 0;
    }
    public Pane decodeMemoryIntoBuffer(int pc) {
            ArrayList<String> temp = this.buffer;
            this.buffer = b0;
            this.putquiet("MOX");
            this.buffer = temp;
            return this;
    };
    public void loadPaneBuffer(String filename, ArrayList<String> b) {
            ArrayList<String> temp = this.buffer;
            this.buffer = b;
            In in = new In(filename);
            while (in.hasNextLine()) {
                this.putquiet(in.readLine());
            }
            this.buffer = temp;
    };
    public Pane setBorderColor(String szColor) {
        this.bordercolor = szColor;
        return this;
    }
    public String colorText(String sz, String color) {
        this.bordercolor = color;
        return color + sz + ANSI.WHITE;
    }
    public Pane clear() {
        this.clear(this.bordercolor);
        return this;
    }
    public Pane clear(String ansiColor) {
        String dashes = new String(new char[w]).replace("\0", "-");
        String blanks = new String(new char[w]).replace("\0", " ");
        this.out.print(ANSI.RESET);

        // H.Log(dashes.replaceFirst( (new String(new char[title.length()])).replace("\0","-"), title));
        String mdashes = dashes.replaceFirst( (new String(new char[title.length()])).replace("\0","-"), title);
        this.out.print("\033[" + (r-1) + ";" + c + "H" + colorText("+ " + mdashes + " +", ansiColor));
        int rr = r;
        int cc = c;
        for (int i=0;i<this.lines;i++) {
            this.out.print("\033[" + (rr) + ";" + (cc) + "H" + colorText("| ", ansiColor));
            //this.out.print("\033[K");
            this.out.print(blanks);
            this.out.print("\033[" + (rr++) + ";" + (cc+w+3) + "H" + colorText("|", ansiColor));
        }
        this.out.print("\033[" + (r+lines) + ";" + c + "H" + colorText("+ " + dashes + " + ", ansiColor));
        return this;
    }
    public static void printf(String format, Object... args) {

        out.printf(LOCALE, format, args);
        out.flush();
    }
    public static void print(String sz) {
        out.print(sz);
        out.flush();
    }

    public int nextcolumn() {
        return w+5;
    }
    public int gapcolumn() {
        return w+6;
    }
    public int gaplap() {
        return r+lines+3;
    }
    public int underlap() {
        return r+lines+2;
    }
    public int overlap() {
        return r+lines+1;
    }
    public void top() {
        refresh(0);
    }
    public void bottom() {
        refresh(buffer.size()-1);
    }
    public void up() {
        refresh(refreshpoint-1);
    }
    public void down() {
        refresh(refreshpoint+1);
    }
    public int find(String sz) {
        Finder f   = new Finder("^([0-9A-Fa-f]{4})[ \t]*(" + sz + ")");
        int nRet = -1;
        for (int i =0;i<buffer.size();i++)  
            if (f.matches(buffer.get(i))) {
                refresh(i+ ((int) lines /2));
                nRet = i;
            }
        return nRet;
    }
    public Pane paint() {
        this.refresh(buffer.size()-1);
        return this;
    }
    public Pane refresh(int n) {
        if (n <0) n = 0;
        if (n >= buffer.size()) n = buffer.size()-1;
        if (n < lines) n = Math.min(lines-1, buffer.size()-1);
        refreshpoint = n;
        // Clear is no long required clear();
        count =1;
        rpos = r;
        cpos = c;
        int j = 0;
        int s = 0;
        this.pos(1,1);
        if (n <= 0) {
            j = 0;
            s = Math.min(buffer.size()-1, lines-1);
//        new Out().print("\n1  s set to          "  +  s);
//          if (buffer.size() < lines)
//              s = buffer.size()-1;
//          else{
//              s = lines-1;
//          }
        }
        else {
            if (buffer.size() <= lines) {
                j = 0;
                s = buffer.size() - 1;
            }
            if (buffer.size() > lines) {
                j = n - (lines-1);
                s = Math.max(n, j + (lines-1));
            }
        }
        if (j <0) j = 0;

//      Out o = new Out();
//      o.print("\nlines         "  +  lines);
//      o.print("\nrefreshpoint  "  +  refreshpoint);
//      o.print("\nn             "  +  n);
//      o.print("\nBuffer Size   "  +  buffer.size());
//      o.print("\nj             "  +  j);
//      o.print("\ns             "  +  s);

        for (int i = j; i <= s; i++) {
//          H.assertion(  (i>=0),              "i >= 0"  );
//          H.assertion(  (i<buffer.size()),   "i < buffer.size()", n + ""  );
            this.out.print("\033[K\033[" +rpos+ ";" +cpos+ "H" + colorText("| ",bordercolor) +buffer.get(i)+ "\033[" +rpos+ ";" +(cpos+w+3)+ "H" + colorText("|",bordercolor));
            rpos++;
            this.paneRow=rpos;
            this.paneCol=cpos;
            this.pos(COMMAND_ROW,COMMAND_COLUMN);
        }
        return this;
    }

    public Pane putquiet(String sz) {
        buffer.add(sz);
        return this;
    }
    public Pane reset() {
        buffer.clear();
        return this;
    }
    public Pane appendf(String format, Object... args) {
        int lastindex=buffer.size()-1;
        if ( lastindex < 0 ) {
            buffer.add(String.format(format, args));
            refresh(buffer.size()-1);
        } else {
            if (buffer.get(lastindex).length() > this.getWritableWidth()) {
                buffer.add(String.format(format, args));
                refresh(buffer.size()-1);
            } else {
                String lastvalue=buffer.get(lastindex);
                buffer.set(lastindex, lastvalue + String.format(format, args));
                refresh(buffer.size()-1);
            }
        }
        return this;
    }
    public Pane putf(String format, Object... args) {
        buffer.add(String.format(format, args));
        refresh(buffer.size()-1);
        return this;
    }
    public Pane putlightf(String format, Object... args) {
        clear();
        buffer.add(String.format(format, args));
        refresh(buffer.size()-1);
        buffer.clear();
        return this;
    }
    public Pane put(String ... a) {
        StringBuffer sb = new StringBuffer();
        for (int i=0;i<a.length;i++) {
            sb.append(a[i]);
        }
        buffer.add(sb.toString());
        refresh(buffer.size()-1);
        return this;
    }
    public Pane putln(String sz) {
        buffer.add(sz);
        buffer.add("");
        refresh(buffer.size()-1);
        return this;
    }
    public Pane putlight(String sz) {
        //buffer.add(sz.substring(0, Math.min(sz.length(), w)));
        clear();
        buffer.add(sz);
        refresh(buffer.size()-1);
        buffer.clear();
        return this;
    }
    public static int nPrompt(String sz) {
        int t = 0;
        Scanner input = new Scanner(System.in);
        Pane.pos(Pane.getCOMMAND_ROW(),Pane.getCOMMAND_COLUMN());
        System.out.print(sz);
        System.out.print("\033[K");
        t = input.nextInt();
        return t;
    }
    public String prompt(String sz, String szDefault) {
        String t = "";
        Scanner input = new Scanner(System.in);
        this.pos(this.getCOMMAND_ROW(),this.getCOMMAND_COLUMN());
        System.out.print(sz);
        System.out.print("\033[K");
        t = input.nextLine();
        return (t.equals("")) ? szDefault : t;
    }
    public String prompt(String sz) {
        Scanner input = new Scanner(System.in);
        this.pos(this.getCOMMAND_ROW(),this.getCOMMAND_COLUMN());
        System.out.print(sz);
        System.out.print("\033[K");
        return input.nextLine();
    }
    public void renderline(String sz) {
         if (count == (lines+1)) {
             count = 1;
         }
         if (count == 1) {
             rpos = r;
             cpos = c;
         }
     }
     public static void pos(int r, int c) {
         Pane.out.print("\033[" + r + ";" + c + "H");
     }

     public String readline() {
            Scanner input = new Scanner(System.in);
            String szIn = "";
            while (szIn.equals("")) {
                this.pos(this.paneRow,this.paneCol);
                System.out.print(">> ");
                String name = input.nextLine();
            }
            return szIn;
     }
     public static void main(String[] args) {
     System.out.print("\033[2J");

            Pane p =  new Pane("THISONE", 16,  3,     1,    66, ANSI.WHITE);
            int i = 0;
            for ( i=0;i<24;i++) {
                         p.put("Hello World " + i + "");
            }

            p.pos(44,1);
            Scanner input = new Scanner(System.in);
            while (true) {
                p.pos(44,1);
                System.out.print(">> ");
                String name = input.nextLine();
                if (name.toUpperCase().equals("T")) {
                    p.top();
                }
                if (name.toUpperCase().equals("B")) {
                    p.bottom();
                }
                if (name.toUpperCase().equals("U")) {
                    p.up();
                }
                if (name.toUpperCase().equals("D")) {
                    p.down();
                }
                if (name.toUpperCase().equals("F")) {
                    p.find("X");
                }
                if (name.toUpperCase().equals("Q")) {
                    System.exit(1);
                }
            }
     }

     public Pane showHex2Quiet(int[] a, int offset, String ... comments) {
        StringBuffer sbComments = new StringBuffer();
        StringBuffer sb = new StringBuffer();
        final int C = 16;
        final int PAGESIZE = 16;

        for (int i=0;i<comments.length;i++) {
            sbComments.append((i>0) ? "" : "");
            sbComments.append(comments[i]);
        }

        // Put offset on a 16 word boundary
        offset = offset - (offset % 16);

        int i = offset;
        int count = (PAGESIZE < a.length) ? PAGESIZE : a.length;
        sb.append(ANSI.RESET);
        sb.append(H.toHex(0+offset) + ": ");
        while (i < (count+offset) ) {
             sb.append(H.toHexBlank(a[i]) + " ");
             if ( (i+1) % 16 == 0 ) {
                 sb.append(H.BAR);
                 for (int j=(i-15);j<=i;j++) sb.append( (a[j] < 127 && a[j] > 31) ? Character.toString((char) a[j]) : ".");
                 sb.append(H.BAR);
                 this.putquiet(sb.toString() + " " + sbComments.toString());
                 sb.delete(0, sb.length());
                 if  ( (i+1) < (count+offset) ) sb.append(H.toHex(i+1) + ": ");
             }
            i++;
        }
        this.putquiet(sb.toString());
        sb.delete(0, sb.length());
        return this;
    }
     public Pane showHex2(int[] a, int offset, String ... comments) {
        StringBuffer sbComments = new StringBuffer();
        StringBuffer sb = new StringBuffer();
        final int C = 16;
        final int PAGESIZE = 16;

        for (int i=0;i<comments.length;i++) {
            sbComments.append((i>0) ? "" : "");
            sbComments.append(comments[i]);
        }

        // Put offset on a 16 word boundary
        offset = offset - (offset % 16);

        int i = offset;
        int count = (PAGESIZE < a.length) ? PAGESIZE : a.length;
        sb.append(ANSI.RESET);
        sb.append(H.toHex(0+offset) + ": ");
        while (i < (count+offset) ) {
             sb.append(H.toHexBlank(a[i]) + " ");
             if ( (i+1) % 16 == 0 ) {
                 sb.append(H.BAR);
                 for (int j=(i-15);j<=i;j++) sb.append( (a[j] < 127 && a[j] > 31) ? Character.toString((char) a[j]) : ".");
                 sb.append(H.BAR);
                 this.put(sb.toString() + " " + sbComments.toString());
                 sb.delete(0, sb.length());
                 if  ( (i+1) < (count+offset) ) sb.append(H.toHex(i+1) + ": ");
             }
            i++;
        }
        this.put(sb.toString());
        sb.delete(0, sb.length());
        return this;
    }
     public Pane showHexQuiet(int[] a, int offset) {
        StringBuffer sb = new StringBuffer();
        final int C = 16;
        final int PAGESIZE = 32;
        int i = offset;
        int count = (PAGESIZE < a.length) ? PAGESIZE : a.length;
        sb.append(ANSI.RESET);
        sb.append(H.toHex(0+offset) + ": ");
        while (i < (count+offset) ) {
             sb.append(H.toHex(a[i]) + " ");
             if ( (i+1) % 16 == 0 ) {
                 sb.append(H.BAR);
                 for (int j=(i-15);j<=i;j++) sb.append( (a[j] < 127 && a[j] > 31) ? Character.toString((char) a[j]) : ".");
                 this.putquiet(sb.toString());
                 sb.delete(0, sb.length());
                 sb.append(H.toHex(i+1) + ": ");
             }
            i++;
        }
        this.putquiet(sb.toString());
        sb.delete(0, sb.length());
        return this;
    }


     public Pane showMemoryDecoded(int offset) {
        final int count = 32;
        showMemoryDecoded(offset, count);
        return this;
     }
     public String decodePC() {
        int[] a = TOY.HW.getMem();
        StringBuffer sb = new StringBuffer();
            int i = TOY.HW.getPC();
            Decode d = InstructionSet.decodeInstruction(a[i], a[i+1]);
            sb.append(d.getDecodeString());
        return sb.toString();
    }
     public Pane showMemoryDecoded(int offset, int count) {
        int[] a = TOY.HW.getMem();
        StringBuffer sb = new StringBuffer();
        int i = offset;
        sb.append(ANSI.RESET);
        while (i < (offset+count)) {

            Decode d = InstructionSet.decodeInstruction(a[i], a[i+1]);
            sb.append(H.toHex(i) + ": ");
            sb.append(H.toHex(a[i]) + " ");
            sb.append(H.toHex(a[i+1]));
            sb.append(" ");
            sb.append(H.LPad32(d.toString()));
            sb.append("      ");
            sb.append(H.toHex(i) + ": ");
            sb.append(H.LPad32(TOY.HW.getProgramLines()[i]));
            this.put(sb.toString());
            sb.delete(0, sb.length());
            i++;
            i++;
        }
        this.put(sb.toString());
        sb.delete(0, sb.length());
        return this;
    }
     public String showMemoryDecodedString(int offset) {
        int[] a = TOY.HW.getMem();
        StringBuffer sb = new StringBuffer();
        int i = offset;
        sb.append(ANSI.RESET);
        Decode d = InstructionSet.decodeInstruction(a[i], a[i+1]);
        sb.append(H.toHex(i) + ": ");
        sb.append(H.toHex(a[i]) + " ");
        sb.append(H.toHex(a[i+1]));
        sb.append(" ");
        sb.append(H.LPad32(d.toString()));
        sb.append("      ");
        sb.append(H.toHex(i) + ": ");
        sb.append(H.LPad32(TOY.HW.getProgramLines()[i]));
        return sb.toString();
    }

    public  void state() {
        String sz1 = "";
        String sz2 = "";
        this.reset();
        int[] a = TOY.HW.getReg();
        int pc = TOY.HW.getPC();
        int[] m = TOY.HW.getMem();
        int count = TOY.HW.getRegisterCount();
        this.clear().selectAndClearBuffer1();
        this.putf("%s", "PC: " + ANSI.RESET + H.toHex(pc));
        this.putf("%s", this.decodePC());
        this.putf("%s", "");
        this.putf("%s", "R0: 0000 (ALWAYS)");
        this.putf("%s", "");
        int i = 0; 
        while (i < ((int) count/2)) {
            sz1 = (a[i] == 0) ? ANSI.RESET + H.toHex(a[i]) : ANSI.DATA + H.toHex(a[i]) + ANSI.RESET;
            sz2 = (a[i] == 0) ? ANSI.RESET + H.toHex(a[i]) : ANSI.DATA + H.toHex(a[i]) + ANSI.RESET;
            this.putf("%s%s%s%s  %s%s%s%s","R", H.toHexNibble(i), ": ", sz1,"R", H.toHexNibble(i+8), ": ", sz2);
            i++;
        }
        this.putf("%s", "");
        this.putf("%s", "SP: " + TOY.HW.getStkPtrHex());
        i = 0;
        while (i < ((int) count/2)) {
            sz1 = (TOY.HW.stkGet(i) == 0) ? ANSI.RESET + TOY.HW.stkGetHex(i) : ANSI.DATA + TOY.HW.stkGetHex(i) + ANSI.RESET;
            sz2 = (TOY.HW.stkGet(i+8) == 0) ? ANSI.RESET + TOY.HW.stkGetHex(i+8) : ANSI.DATA + TOY.HW.stkGetHex(i+8) + ANSI.RESET;
            this.putf("%s%s%s   %s%s%s", H.toHex2D(i), ": ", sz1, H.toHex2D(i+8), ": ", sz2);
            i++;
        }
        this.putf("%s", "");
        this.putf("%s%s", "IR: ", H.toHex(TOY.HW.getIndexRegister()));
        for (i =0;i<TOY.HW.getBrk().length;i++) if (TOY.HW.getBrk()[i]) this.putf("%s%s", "BP ",H.toHex(i));
        this.putf("%s", "");
        this.putf("%s", "Memory");
        this.putf("%s", "");
        i = a[1];
        count = 8;
        int j = 0;
        while (j < 8) {
            sz1 = (m[i] == 0) ? ANSI.RESET + H.toHex(m[i]) : ANSI.DATA + H.toHex(m[i]) + ANSI.RESET;
            int n = 1;
            sz2 = (m[i+n] == 0) ? ANSI.RESET + H.toHex(m[i+n]) : ANSI.DATA + H.toHex(m[i+n]) + ANSI.RESET;
            this.putf("%s%s%s   %s%s%s", H.toHex(i), ": ", sz1, H.toHex(i+8), ": ", sz2);
            i++;
            j++;
        }
    }

    public String memoryString(int offset) {
        int[] a = TOY.HW.getMem();
        String delim = "";
        StringBuffer sb = new StringBuffer();

        sb.append(H.toHex(0+offset) + ": ");
        for (int i= offset;i<offset+16;i++) {
                 sb.append(delim).append(H.toHex(a[i]));
                 delim = " ";
        }

        return sb.toString();
    }
    public Pane memory(int offset, int rows) {
        final int PAGESIZE=62;
        final int C = 16;
        int[] a = TOY.HW.getMem();
        int i = offset;
        StringBuffer sb = new StringBuffer();
        int count = (PAGESIZE < a.length) ? PAGESIZE : a.length;
        count = (rows>0 ? rows : 1) * C;

        sb.append(H.toHex(0+offset) + ": ");
        while (i < (count+offset) ) {
            if ( a[i] == 0 )
                sb.append(H.toHex(a[i]) + " ");
            else
                sb.append(H.toHex(a[i]) + " ");

             if ( (i+1) % 16 == 0 ) {
                 sb.append(H.BAR);
                 for (int j=(i-15);j<=i;j++) sb.append( (a[j] < 127 && a[j] > 31) ? Character.toString((char) a[j]) : ".");
                 this.putquiet(sb.toString());
                 sb.delete(0, sb.length());
                 sb.append(H.toHex(i+1) + ": ");
             }
            i++;
        }
                this.putquiet(sb.toString());
                sb.delete(0, sb.length());
                return this;
    }

     public Pane showHex(int[] a, int offset) {
        StringBuffer sb = new StringBuffer();
        final int C = 16;
        final int PAGESIZE = 32;
        int i = offset;
        int count = (PAGESIZE < a.length) ? PAGESIZE : a.length;
        sb.append(ANSI.RESET);
        sb.append(H.toHex(0+offset) + ": ");
        while (i < (count+offset) ) {
             sb.append(H.toHex(a[i]) + " ");
             if ( (i+1) % 16 == 0 ) {
                 sb.append(H.BAR);
                 for (int j=(i-15);j<=i;j++) sb.append( (a[j] < 127 && a[j] > 31) ? Character.toString((char) a[j]) : ".");
                 this.put(sb.toString());
                 sb.delete(0, sb.length());
                 sb.append(H.toHex(i+1) + ": ");
             }
            i++;
        }
        this.put(sb.toString());
        sb.delete(0, sb.length());
        return this;
    }

}
class Test {
     static int r = 5;
     static int c = 2;
     static int ct = 1;
}

// http://www.cse.psu.edu/~kxc104/class/cse472/09f/hw/hw7/vt100ansi.htm
