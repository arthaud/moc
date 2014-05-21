package moc.st;

import moc.type.TCLASS;

public class INFOCLASS implements INFO {
    protected TCLASS type;

    public INFOCLASS(TCLASS type) {
        this.type = type;
    }

    public TCLASS getType() {
        return type;
    }

    @Override
    public String toString() {
        return "INFOCLASS [" + type.toString() + "]";
    }
}
