import java.util.Scanner;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.ArrayList;

public class ObjectRing {
    private ArrayList<Object> ring = new ArrayList<Object>();
    private int index = -1;
    public void add(Object o) {
        this.index++;
        ring.add(o);
    }
    public Object get() {
        return ring.get(index);
    }
    public Object next() {
        int l = this.ring.size() -1;
        Object oRet = null;
        if ( this.ring.size() > 0 ) {
            if ( index+2 > this.ring.size() ) {
                oRet =  ring.get(0);
                index = 0;
            } else {
                index++;
                oRet =  ring.get(index);
            }
        }
        return oRet;
    }

}
