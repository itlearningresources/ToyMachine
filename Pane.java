import java.util.Scanner;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.ArrayList;

public class Pane {
    private ArrayList<String> buffer = new ArrayList<String>();
    private int r;
    private int c;
    private int w = 80;
    private int lines;
    private int count;
    private int rpos;
    private int cpos;
    private int refreshpoint;
    // forceeUnicode UTF-8 encoding; otherwise it's system dependent
    private static final String CHARSET_NAME = "UTF-8";
    // assume language = English, country = US for consistency with StdIn
    private static final Locale LOCALE = Locale.US;
    // send output here
    private static java.io.PrintStream out;

    public Pane(int lines, int r, int c, int w) {
        int rr;
        int cc;

    // this is called before invoking any methods
            try {
                out = System.out;
                //out = new PrintWriter(new OutputStreamWriter(System.out, CHARSET_NAME), true);
            }
            catch (Exception e) {
                System.out.println(e);
            }

        this.w = w;

        this.r = r;
        this.c = c;
        String dashes = new String(new char[w]).replace("\0", "-");
        this.lines = lines;
        this.count = 1;
        this.out.print("\033[" + (r-1) + ";" + c + "H" + "+ " + dashes + " +");
        clear();
    }
    public void commandline() {
            this.pos(44,1);
            Scanner input = new Scanner(System.in);
            Finder f   = new Finder("^([/0-9A-Za-z]{1})[ \t]*([0-9A-Za-z]*)");
            while (true) {
                this.pos(44,1);
                System.out.print(">> ");
                String sz = input.nextLine();
                if (f.matches(sz)) {
                    String name = f.get1();
                    if (name.toUpperCase().equals("T")) {
                        this.top();
                    }
                    if (name.toUpperCase().equals("B")) {
                        this.bottom();
                    }
                    if (name.toUpperCase().equals("U")) {
                        this.up();
                    }
                    if (name.toUpperCase().equals("D")) {
                        this.down();
                    }
                    if (name.toUpperCase().equals("F")) {
                        this.find(f.get2());
                    }
                    if (name.toUpperCase().equals("/")) {
                        this.find(f.get2());
                    }
                    if (name.toUpperCase().equals("Q")) {
                        System.exit(1);
                    }
                }
            }
    }
    public void clear() {
        String dashes = new String(new char[w]).replace("\0", "-");
        String blanks = new String(new char[w]).replace("\0", " ");
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

    public void refresh(int n) {
        if (n <0) n = 0;
        if (n >= buffer.size()) n = buffer.size()-1;
        if (n < lines) n = Math.min(lines-1, buffer.size()-1);
        refreshpoint = n;
        clear();
        count =1;
        rpos = r;
        cpos = c;
        int j = 0;
        int s = 0;
        this.pos(46,1);
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
            assertion(  (i>=0),              "i >= 0"  );
            assertion(  (i<buffer.size()),   "i < buffer.size()", n + ""  );

            this.out.print("\033[K");
            this.out.print("\033[" + rpos + ";" + (cpos) + "H" + "| ");
            this.out.print(buffer.get(i));
            //this.out.print(buffer.get(i).substring(0, Math.min(buffer.get(i).length(), w)));
            this.out.print("\033[" + rpos + ";" + (cpos+w+3) + "H" + "|");
            rpos++;
            this.pos(44,1);
//if ( i == 1) System.exit(1);
        }
    }
    public static void assertion(boolean b, String sz) {
            final String c = "\n\033[K";
        if (!b) {
            System.out.print(c + "ASSERTION FAILED");
            System.out.print(c + sz);
            System.out.print(c + "ASSERTION FAILED\n");
            System.exit(1);
        }
    }
    public static void assertion(boolean b, String sz, String szValue) {
            final String c = "\n\033[K";
        if (!b) {
            System.out.print(c + "ASSERTION FAILED");
            System.out.print(c + sz);
            System.out.print(c + "VALUE IS:    " + szValue);
            System.out.print(c + "ASSERTION FAILED\n");
            System.exit(1);
        }
    }

    public void put(String sz) {
        //buffer.add(sz.substring(0, Math.min(sz.length(), w)));
        buffer.add(sz);
        refresh(buffer.size()-1);
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

}

class Test {
     static int r = 5;
     static int c = 2;
     static int ct = 1;
}

// http://www.cse.psu.edu/~kxc104/class/cse472/09f/hw/hw7/vt100ansi.htm
