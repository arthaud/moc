package moc.cg;

import moc.type.TTYPE;

public interface VariableLocator {
    Location generate(TTYPE param);

    VariableLocator getSon();
}
