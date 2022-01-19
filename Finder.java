import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Iterator;

public class Finder {
    private String  r = "";
    private Pattern p = null;
    private Matcher m = null;
    private int cursor = 0;

    public Finder(String r) {
        this.r = r;
        this.p = Pattern.compile(r);
    }
    public boolean matches(String sz) {
        return find(sz);
    }
    public boolean isa(String sz) {
        return find(sz);
    }
    public boolean find(String sz) {
        boolean bRet = false;
        this.m = this.p.matcher(sz);
        if (m.find()) {
            this.cursor = 0;
            bRet = true;
        }
        return bRet;
    }

    public boolean hasNext() {
        boolean bRet = false;
        if ((this.cursor + 1) <= this.m.groupCount()) bRet = true;
        return bRet;
    }
    public String next() {
        this.cursor++;
        return new String(this.m.group(this.cursor));
    }

    public int getGroupCount() {
        return this.m.groupCount();
    }
    public String get1() {
        return new String(this.m.group(1));

    }
    public String get2() {
        return new String(this.m.group(2));

    }
    public String get(int n) {
        String szRet = "";
        if ( (n>0) && (n <= this.m.groupCount()) ) szRet = new String(this.m.group(n));
        return szRet;

    }
}
