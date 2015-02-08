package moc.cg;

/**
 * Holds the code for a global variable.
 */
public class GlobalCode implements EntityCode {
    protected String asm;

    public GlobalCode(String asm) {
        this.asm = asm;
    }

    public String getAsm() {
        return asm;
    }

    public void setAsm(String asm) {
        this.asm = asm;
    }
}

