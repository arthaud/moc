package moc.cg;

public class Code {
    /**
     * The assembler instructions
     */
    protected String asm;

    /**
     * The address, when it is known at compilation time
     * Otherwise, null
     */
    protected Location address;

    /**
     * True if asm generates an address
     */
    protected boolean isAddress;

    public Code() {
        this(null);
    }

    public Code(String asm) {
        this.asm = asm;
        address = null;
        isAddress = false;
    }

    public String getAsm() {
        return asm;
    }

    public void appendAsm(String asm) {
        this.asm += "\n" + asm;
    }

    public void prependAsm(String asm) {
        this.asm = asm + "\n" + this.asm;
    }

    public void setAsm(String asm) {
        this.asm = asm;
    }

    public void setLocation(Location add) {
        address = add;
    }

    public Location getLocation() {
        return address;
    }

    public void setIsAddress(boolean isa) {
        isAddress = isa;
    }

    public boolean getIsAddress() {
        return isAddress;
    }
}
