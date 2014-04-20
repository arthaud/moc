package moc.st;

import moc.gc.Location;
import moc.type.DTYPE;

/**
 * This class describes a local var: address and type
 */
public class INFOVAR implements INFO {
    protected DTYPE type;

    /**
     * Represents a memory location: depends on the machine
     */
    protected Location location;

    public DTYPE getType() {
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
    public INFOVAR(DTYPE t, Location l) {
        type = t;
        location = l;
    }

    @Override
    public String toString() {
        return "INFOVAR [type=" + type.getName() + ", location=" + location + "]";
    }
}
