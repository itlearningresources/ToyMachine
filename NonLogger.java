public final class NonLogger extends Logger {
    public NonLogger(String szPrefix) {
        super(szPrefix);
    }
    public NonLogger(String szPrefix, String fn) {
        super(szPrefix, fn);
    }
    public void log(String sz) {
    }
}

