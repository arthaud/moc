package moc.cg;

public class Code {
    /**
     * The assembler instructions
     */
    protected String asm;

    /**
     * The location, when it is known at compilation time
     * Otherwise, null
     */
    protected Location location;

    /**
     * The value, when it is known at compilation time
     * Otherwise, null
     */
    protected Long value;

    /**
     * True if the code generates an address
     */
    protected boolean address;

    public Code() {
        this(null);
    }

    public Code(String asm) {
        this(asm, false);
    }

    public Code(String asm, boolean isAddress) {
        this.asm = asm;
        location = null;
        value = null;
        address = isAddress;
    }

    /**
     * Assembler
     */
    public String getAsm() {
        return asm;
    }

    public boolean hasAsm() {
        return asm != null;
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
    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean hasLocation() {
        return location != null;
    }

    public Location getLocation() {
        return location;
    }

    public void setAddress(boolean isAddress) {
        address = isAddress;
    }

    public boolean isAddress() {
        return address;
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
    public static Code fromLocation(Location l, boolean isAddress) {
        Code c = new Code();
        c.setLocation(l);
        c.setAddress(isAddress);
        return c;
    }

    public static Code fromLocation(Location l) {
        return fromLocation(l, false);
    }

    public static Code fromValue(long value, boolean isAddress) {
        Code c = new Code();
        c.setValue(value);
        c.setAddress(isAddress);
        return c;
    }

    public static Code fromValue(long value) {
        return fromValue(value, false);
    }
}
