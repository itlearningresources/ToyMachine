public class Decode {
    public String[] decodeArray = new String[2];
    public int opCode;
    public  Decode(String sz0, String sz1) {
        this.decodeArray[0] = sz0;
        this.decodeArray[1] = sz1;
    }
    public void setOpCode(int op) {
        opCode = op;
    }    
    public int getOpCode() {
        return this.opCode;
    }    
    public String toString() {
        return this.decodeArray[0];
    }
}



