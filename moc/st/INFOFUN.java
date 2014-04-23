package moc.st;

import moc.type.TFUNCTION;

public class INFOFUN implements INFO {
    protected TFUNCTION function;

    public TFUNCTION getFunction() {
        return function;
    }

    public INFOFUN(TFUNCTION fun) {
        function = fun;
    }

    @Override
    public String toString() {
        return "INFOFUN [" + function.toString() + "]";
    }
}
