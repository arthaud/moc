package moc.cg;

/**
 * This class describes a memory address (offset from a register)
 */
public class Location {
    public Enum LocationType {
        REGISTER, STACKFRAME, ABSOLUTE
    }
    
    private int memory_offset;
    private LocationType memory_type;

    public int getOffset() {
        return memory_offset;
    }

    public LocationType getType() {
        return memory_type;
    }

    @Override
    public String toString() {
        return memory_offset + "@" + memory_type;
    }

    public Location(LocationType memory_type, int memory_offset) {
        this.memory_offset = memory_offset;
        this.memory_type = memory_type;
    }
}
