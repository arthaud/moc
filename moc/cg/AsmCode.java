package moc.cg;

/**
 * Holds the code for a global asm{} block.
 */
public class AsmCode implements EntityCode {
    protected String asm;

    public AsmCode(String asm) {
        this.asm = asm;
    }

    public String getAsm() {
        return asm;
    }

    public void setAsm(String asm) {
        this.asm = asm;
    }
}

