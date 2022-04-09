public class Decode {
    public String[] decodeArray = new String[3];
    public int opCode;
    public  Decode(String sz0, String sz1) {
        this.decodeArray[0] = sz0;
        this.decodeArray[1] = sz1;
        this.decodeArray[2] = "-";
    }
    public  Decode(String sz0, String sz1,String sz2) {
        this.decodeArray[0] = sz0;
        this.decodeArray[1] = sz1;
        this.decodeArray[2] = sz2;
    }
    public void setOpCode(int op) {
        opCode = op;
    }    
    public int getOpCode() {
        return this.opCode;
    }    
    public String getDecodeString() {
        return this.decodeArray[0];
    }    
    public String toString() {
        return "("+H.toHexShort(this.opCode)+") " + this.decodeArray[0] + "";
    }
}



