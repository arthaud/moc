package moc.st;

import moc.type.TFUNCTION;

public class INFOFUN implements INFO {
    protected TFUNCTION type;

    public TFUNCTION getType() {
        return type;
    }

    public INFOFUN(TFUNCTION type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "INFOFUN [" + type.toString() + "]";
    }
}
