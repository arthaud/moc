package moc.cg;

/**
 * Holds the code for a function.
 */
public class FunctionCode implements EntityCode {
    protected String name;
    protected String asm;
    protected boolean exported;

    public FunctionCode(String name, String asm, boolean exported) {
        this.name = name;
        this.asm = asm;
        this.exported = exported;
    }

    public String getName() {
        return name;
    }

    public String getAsm() {
        return asm;
    }

    public void setAsm(String asm) {
        this.asm = asm;
    }

    public boolean isExported() {
        return exported;
    }
}

