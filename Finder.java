import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Finder {
    private String  r = "";
    private Pattern p = null;
    private Matcher m = null;

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
            bRet = true;
        }
        return bRet;
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
