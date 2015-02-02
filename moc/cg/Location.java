package moc.cg;

/**
 * This class describes a memory address (offset from a register)
 */
public class Location {
    public enum LocationType {
        REGISTER, STACKFRAME, ABSOLUTE
    }

    private int memoryOffset;
    private LocationType memoryType;

    public int getOffset() {
        return memoryOffset;
    }

    public LocationType getType() {
        return memoryType;
    }

    @Override
    public String toString() {
        return memoryOffset + "@" + memoryType;
    }

    public Location(LocationType memoryType, int memoryOffset) {
        this.memoryOffset = memoryOffset;
        this.memoryType = memoryType;
    }

    @Override
    public boolean equals(Object other)
    {
        if(other == null || ! (other instanceof Location))
            return false;
        Location o = (Location) other;
        return this.memoryType == o.memoryType && this.memoryOffset == o.memoryOffset;
    }
}
