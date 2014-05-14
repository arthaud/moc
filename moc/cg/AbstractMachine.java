package moc.cg;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import moc.compiler.MOCException;

/**
 * This class describes a target machine
 */
public abstract class AbstractMachine implements IMachine {

    protected int labelNum = 0;

    protected int getLabelNum() {
        labelNum++;
        return labelNum -1;
    }

    /**
     * Writes the code in a file from the name of the source file and the suffix
     */
    @Override
    public void writeCode(String fname, String code) throws MOCException {
        try {
            // pre-checked at startup
            int pt = fname.lastIndexOf('.');
            String name = fname.substring(0, pt);
            String asmName = name + "." + getSuffix();
            System.err.println("Writing code in " + asmName);
            PrintWriter pw = new PrintWriter(new FileOutputStream(asmName));
            pw.print("; Generated code for " + fname
                    + ".\n; Do not modify by hand\n" + code);
            pw.close();
        } catch (FileNotFoundException e) {
            throw new MOCException(e.getMessage());
        }
    }

    public String genComment(String comm) {
        return "; " + comm + "\n";
    }

    public Code includeAsm(String asmCode) {
        return new Code(asmCode.substring(1, asmCode.length() - 1)); // remove the ""
    }
}
