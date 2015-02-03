package moc.cg;

/**
 * Holds the code for a function.
 */
public class FunctionCode implements EntityCode {
    protected String name;
    protected String asm;

    public FunctionCode(String name, String asm) {
        this.name = name;
        this.asm = asm;
    }

    public String getName() {
        return name;
    }

    public String getAsm() {
        return asm;
    }
}

