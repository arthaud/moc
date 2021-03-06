package moc.cg;

import moc.type.TTYPE;

public interface VariableLocator {
    /**
     * Generate a new location for a local variable
     *
     * @param param The type of the new local variable
     */
    Location generate(TTYPE param);

    /**
     * Returns a new VariableLocator, for instance when entering in a new block
     */
    VariableLocator getChild();
}
