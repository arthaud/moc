package moc.cg;

public class Code {
    protected String asm;

    public Code() {
        this(null);
    }

    public Code(String asm) {
        this.asm = asm;
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
}
