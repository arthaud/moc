package moc.gc;

/**
 * This class describes a memory address (offset from a register)
 */
public class Location {
    private int dep;
    private Register reg;

    public int getDep() {
        return dep;
    }

    public Register getReg() {
        return reg;
    }

    @Override
    public String toString() {
        return "[" + dep + "/" + reg + "]";
    }

    /**
     * Location = address = offset / register.
     */
    public Location(int dep, Register reg) {
        this.dep = dep;
        this.reg = reg;
    }
}
