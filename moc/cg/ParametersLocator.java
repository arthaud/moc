package moc.cg;

import moc.type.TTYPE;

public interface ParametersLocator {
    /**
     * Generate a new location for a parameter
     *
     * @param param The type of the parameter
     */
    Location generate(TTYPE param);
}
