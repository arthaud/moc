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

    public void setAsm(String asm) {
        this.asm = asm;
    }
}
