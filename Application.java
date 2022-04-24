public class Application {
    static Logger out = new DefaultLogger("out");
    static Logger err = new DefaultLogger("err");
    static Logger dbg = new DefaultLogger("dbg");
    static Logger inf = new DefaultLogger("inf");
    static Logger crash = new CrashLogger("crash");


    public static final void CRASH(Exception e) {
        Application.crash.log(e);
    }

}
