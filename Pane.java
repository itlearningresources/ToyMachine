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
    private ArrayList<String> b1 = new ArrayList<String>();
    private ArrayList<String> b2 = new ArrayList<String>();
    private ArrayList<String> b3 = new ArrayList<String>();
    private ArrayList<String> b4 = new ArrayList<String>();
    private ArrayList<String> temp = new ArrayList<String>();
    private ArrayList<String> help = new ArrayList<String>();
    private int r;
    private int c;
    private int w = 80;
    private int lines;
    private int count;
    private int rpos;
    private int cpos;
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
    public Pane buffer1(int n) { this.buffer = b1; if (n>-1) this.refresh(n); return this;}
    public Pane buffer2(int n) { this.buffer = b2; if (n>-1) this.refresh(n); return this;}
    public Pane buffer3(int n) { this.buffer = b3; if (n>-1) this.refresh(n); return this;}
    public Pane buffer4(int n) { this.buffer = b4; if (n>-1) this.refresh(n); return this;}
    public void bufferHelp(int n) { this.buffer = help; if (n>-1) this.refresh(n);}
    public Pane buffer1() { this.buffer = b1; return this;}
    public Pane buffer2() { this.buffer = b2; return this;}
    public Pane buffer3() { this.buffer = b3; return this;}
    public Pane buffer4() { this.buffer = b4; return this;}
    public void bufferHelp() { this.buffer = help;}
    public void buffertemp() { this.buffer = temp;}
    public ArrayList<String>  getBuffer1() { return  b1;}
    public ArrayList<String>  getBuffer2() { return  b2;}
    public ArrayList<String>  getBuffer3() { return  b3;}
    public ArrayList<String>  getBuffer4() { return  b4;}
    public ArrayList<String>  getBufferHelp() { return help;}
    public Pane bufferclear() { buffer.clear(); return this;}
    public Pane buffer1clear() { b1.clear(); return this;}
    public Pane buffer2clear() { b2.clear(); return this;}
    public Pane buffer3clear() { b3.clear(); return this;}
    public Pane buffer4clear() { b4.clear(); return this;}

    private static Pane[] panes = {null};
    public static Pane[] getPanes() { return panes; }

    private static Pane msgPane = null;
    public  static Pane setMsgPane(Pane p) { msgPane = p; return p;}
    public  static Pane getMsgPane() { return msgPane; }

    // send output here
    private static java.io.PrintStream out;
    private static MyOut myout;

    public Pane(int lines, int r, int c, int w) {
        this.buffer = b1;
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
        String dashes = new String(new char[w]).replace("\0", "-");
        this.lines = lines;
        this.count = 1;
        this.out.print("\033[" + (r-1) + ";" + c + "H" + "+ " + dashes + " +");
        clearPane();



    }
    public void loadPane(String filename, ArrayList<String> b) {
            ArrayList<String> temp = this.buffer;
            this.buffer = b;
            In in = new In(filename);
            while (in.hasNextLine()) {
                this.putquiet(in.readLine());
            }
            this.buffer = temp;
    };
    private void clearPane() {
        String dashes = new String(new char[w]).replace("\0", "-");
        String blanks = new String(new char[w]).replace("\0", " ");
        this.out.print(ANSI.RESET);
        this.out.print("\033[" + (r-1) + ";" + c + "H" + "+ " + dashes + " +");
        int rr = r;
        int cc = c;
        for (int i=0;i<this.lines;i++) {
            this.out.print("\033[" + (rr) + ";" + (cc) + "H" + "| ");
            //this.out.print("\033[K");
            this.out.print(blanks);
            this.out.print("\033[" + (rr++) + ";" + (cc+w+3) + "H" + "|");
        }
        this.out.print("\033[" + (r+lines) + ";" + c + "H" + "+ " + dashes + " +");
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
    public int gfind(String sz) {
        Finder f   = new Finder(sz);
        int nRet = -1;
        String line = null;
        for (int i =0;i<buffer.size();i++) { 
            line = buffer.get(i);
            if (f.matches(line)) {
                refreshpoint = i;
                refreshzed(i);
                nRet = i;
                break;
            }
        }
        return nRet;
    }

    public void refreshzed(int n) {
//      if (n <0) n = 0;
//      if (n >= buffer.size()) n = buffer.size()-1;
//      if (n < lines) n = Math.min(lines-1, buffer.size()-1);
        refreshpoint = n;
        clearPane();
        count =1;
        rpos = r;
        cpos = c;
        int end = Math.min(n+(lines-1),buffer.size()-1);
        for (int i = n; i <= end; i++) {
            this.out.print("\033[" + rpos + ";" + (cpos) + "H" + "| ");
//            this.out.print("\033[K");
            this.out.print(buffer.get(i));
            this.out.print("\033[" + rpos + ";" + (cpos+w+3) + "H" + "|");
            rpos++;
            this.pos(COMMAND_ROW,COMMAND_COLUMN);
        }
    }
    public void refresh(int n) {
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
            this.out.print("\033[K\033[" +rpos+ ";" +cpos+ "H| " +buffer.get(i)+ "\033[" +rpos+ ";" +(cpos+w+3)+ "H|");
            rpos++;
            this.pos(COMMAND_ROW,COMMAND_COLUMN);
        }
    }

    public Pane putquiet(String sz) {
        buffer.add(sz);
        return this;
    }
    public Pane reset() {
        buffer.clear();
        return this;
    }

    public Pane putf(String format, Object... args) {
        buffer.add(String.format(format, args));
        refresh(buffer.size()-1);
        return this;
    }
    public Pane putlightf(String format, Object... args) {
        clearPane();
        buffer.add(String.format(format, args));
        refresh(buffer.size()-1);
        buffer.clear();
        return this;
    }
    public Pane put(String ... a) {
        StringBuffer sb = new StringBuffer();
        for (int i=0;i<a.length;i++) sb.append(a[i]);
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
        clearPane();
        buffer.add(sz);
        refresh(buffer.size()-1);
        buffer.clear();
        return this;
    }
    public String prompt(String sz, String szDefault) {
        String t = "";
        Scanner input = new Scanner(System.in);
        this.pos(this.getCOMMAND_ROW(),this.getCOMMAND_COLUMN());
        System.out.print(sz);
        System.out.print("\033[K");
        t = input.nextLine().toUpperCase();
        return (t.equals("")) ? szDefault : t.toUpperCase();
    }
    public String prompt(String sz) {
        Scanner input = new Scanner(System.in);
        this.pos(this.getCOMMAND_ROW(),this.getCOMMAND_COLUMN());
        System.out.print(sz);
        System.out.print("\033[K");
        return input.nextLine().toUpperCase();
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
     public void pos(int r, int c) {
         this.out.print("\033[" + r + ";" + c + "H");
     }


     public static void main(String[] args) {
     System.out.print("\033[2J");

            Pane p =  new Pane(16,  3,     1,    66);
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
                 sb.append("   ||   ");
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
