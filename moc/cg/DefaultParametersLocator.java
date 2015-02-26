package moc.cg;

import moc.type.TTYPE;

public class DefaultParametersLocator implements ParametersLocator {
    private int offset;

    public DefaultParametersLocator(int initOffset) {
        this.offset = initOffset;
    }

    public Location generate(TTYPE param) {
        int res = offset;
        offset += param.getSize();
        return new Location(Location.LocationType.STACKFRAME, res);
    }
}
