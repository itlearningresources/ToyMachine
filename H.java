public final class H {

    public final static void assertion(boolean b, String sz) {
            final String c = "\n\033[K";
        if (!b) {
            System.out.print(c + "ASSERTION FAILED");
            System.out.print(c + sz);
            System.out.print(c + "ASSERTION FAILED\n");
            System.exit(1);
        }
    }
    public final static void assertion(boolean b, String sz, String szValue) {
            final String c = "\n\033[K";
        if (!b) {
            System.out.print(c + "ASSERTION FAILED");
            System.out.print(c + sz);
            System.out.print(c + "VALUE IS:    " + szValue);
            System.out.print(c + "ASSERTION FAILED\n");
            System.exit(1);
        }
    }
    public final static void STOP() {
            System.exit(1);
    }
    public final static void STOP(boolean b) {
            if (b) System.exit(1);
    }

}
