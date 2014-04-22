package moc.st;

import moc.cg.Location;
import moc.type.TTYPE;

/**
 * This class describes a local var: address and type
 */
public class INFOVAR implements INFO {
    protected TTYPE type;

    /**
     * Represents a memory location: depends on the machine
     */
    protected Location location;

    public TTYPE getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }

    public int getSize() {
        return this.getType().getSize();
    }

    /**
     * A var has a type and a location for its value
     */
    public INFOVAR(TTYPE t, Location l) {
        type = t;
        location = l;
    }

    @Override
    public String toString() {
        return "INFOVAR [type=" + type + ", location=" + location + "]";
    }
}
