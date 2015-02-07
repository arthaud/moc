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
     * The value, when it is known at compilation time
     * Otherwise, null
     */
    protected Long value;

    /**
     * True if the code generates an address
     */
    protected boolean isAddress;

    public Code() {
        this(null);
    }

    public Code(String asm) {
        this(asm, false);
    }

    public Code(String asm, boolean isa) {
        this.asm = asm;
        address = null;
        value = null;
        isAddress = isa;
    }

    /**
     * Assembler
     */
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

    /**
     * Location
     */
    public void setLocation(Location add) {
        address = add;
    }

    public boolean hasLocation() {
        return address != null;
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

    /**
     * Value
     */
    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public boolean hasValue() {
        return value != null;
    }

    /**
     * Static constructors
     */
    public static Code fromLocation(Location l, boolean isa) {
        Code c = new Code();
        c.setLocation(l);
        c.setIsAddress(isa);
        return c;
    }

    public static Code fromLocation(Location l) {
        return fromLocation(l, false);
    }

    public static Code fromValue(long value, boolean isa) {
        Code c = new Code();
        c.setValue(value);
        c.setIsAddress(isa);
        return c;
    }

    public static Code fromValue(long value) {
        return fromValue(value, false);
    }
}
