package moc.cg;

public class Code {
    protected String asm;
    protected int address;
    protected boolean isAddress;
    protected int typeSize;

    public Code() {
        this(null);
    }

    public Code(String asm) {
        this.asm = asm;
        address = 0;
        isAddress = false;
    }

    public String getAsm() {
        return asm;
    }

    public void appendAsm(String asm) {
        this.asm += "\n";
        this.asm += asm;
    }

    public void prependAsm(String asm) {
        this.asm = asm + "\n" + this.asm;
    }

    public void setAsm(String asm) {
        this.asm = asm;
    }

    public void setTypeSize(int s) {
        typeSize = s;
    }

    public int getTypeSize() {
        return typeSize;
    }

    public void setAddress(int add) {
        address = add;
    }

    public int getAddress() {
        return address;
    }

    public void setIsAddress(boolean isa) {
        isAddress = isa;
    }

    public boolean getIsAddress() {
        return isAddress;
    }
}
