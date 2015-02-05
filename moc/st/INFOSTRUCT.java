package moc.st;

import moc.type.TSTRUCT;

public class INFOSTRUCT implements INFO {
    protected TSTRUCT type;

    public TSTRUCT getType() {
        return type;
    }

    public INFOSTRUCT(TSTRUCT type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "INFOSTRUCT [" + type.toString() + "]";
    }
}
