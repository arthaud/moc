package moc.gc;

import moc.compiler.MOCException;

/**
 * This interface describes a target machine
 */
public interface IMachine {
    /**
     * Target file suffix (.tam for example)
     */
    String getSuffix();

    /**
     * Writes the code in a file from the source file name and the suffix
     */
    void writeCode(String fileName, String code) throws MOCException;
}
