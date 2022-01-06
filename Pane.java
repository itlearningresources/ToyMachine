class Pane {
    private int r;
    private int c;
    private int lines;
    private int count;
    private int rpos;
    private int cpos;

    public Pane(int r, int c, int lines) {
        this.r = r;
        this.c = c;
        this.lines = lines;
        this.count = 1;
    }
    public void put(String sz) {
         if (count == lines) {
             count = 1;
         }
         if (count == 1) {
             rpos = r;
             cpos = c;
         }
         System.out.print("\033[K");
         System.out.print("\033[" + rpos + ";" + cpos + "H");
         System.out.print(sz);
         count++;
         rpos++;
     }

}
public class Test {
     static int r = 5;
     static int c = 2;
     static int ct = 1;
     public static void pos(int r, int c) {
         System.out.print("\033[" + r + ";" + c + "H");
     }
     public static void main(String[] args) {
     System.out.print("\033[2J");

            Pane p = new Pane(10,5,8);
            Pane p2 = new Pane(20,37,4);
            int i = 1;
            for ( i=0;i<2000;i++) {
                     p.put("Hello World! " + i);
                     p2.put("Hello World! " + i);
            }


            pos(30,1);
     }
}

// http://www.cse.psu.edu/~kxc104/class/cse472/09f/hw/hw7/vt100ansi.htm
