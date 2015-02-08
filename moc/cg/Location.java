package moc.cg;

/**
 * This class describes a memory address (offset from a register)
 */
public class Location {
    public enum LocationType {
        REGISTER, STACKFRAME, ABSOLUTE
    }

    private long memoryOffset;
    private LocationType memoryType;

    public long getOffset() {
        return memoryOffset;
    }

    public LocationType getType() {
        return memoryType;
    }

    @Override
    public String toString() {
        return memoryOffset + "@" + memoryType;
    }

    public Location(LocationType memoryType, long memoryOffset) {
        this.memoryOffset = memoryOffset;
        this.memoryType = memoryType;
    }

    @Override
    public boolean equals(Object other) {
        if(other == null || !(other instanceof Location))
            return false;

        Location o = (Location) other;
        return this.memoryType == o.memoryType && this.memoryOffset == o.memoryOffset;
    }

    public boolean isRegister() {
        return memoryType == LocationType.REGISTER;
    }

    public boolean isStackFrame() {
        return memoryType == LocationType.STACKFRAME;
    }

    public boolean isAbsolute() {
        return memoryType == LocationType.ABSOLUTE;
    }
}
