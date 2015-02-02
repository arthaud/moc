package moc.cg;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import moc.compiler.MOCException;
import moc.st.ST;
import moc.st.INFO;
import moc.st.INFOVAR;

/**
 * This class describes a target machine
 */
public abstract class AbstractMachine implements IMachine {

    protected int labelNum = 0;

    protected int getLabelNum() {
        labelNum++;
        return labelNum - 1;
    }

    protected int globalNum = 0;

    protected int getGlobalNum() {
        globalNum++;
        return globalNum - 1;
    }

    protected String initCode = "";

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
            pw.print(genComment("Generated code for " + fname + "\n"));
            pw.print(genComment("Do not modify by hand\n"));
            pw.print(initCode);
            pw.print(code);
            pw.close();
        } catch (FileNotFoundException e) {
            throw new MOCException(e.getMessage());
        }
    }

    public Location genGlobalLocation() {
        return new Location(Location.LocationType.ABSOLUTE, getGlobalNum());
    }

    public String genComment(String comm) {
        return "; " + comm;
    }

    public String asmVariablePattern() {
        return "%([a-z][_0-9A-Za-z]*)";
    }

    public Code includeAsm(String asmCode, ST symbolsTable) {
        String asm = asmCode.substring(1, asmCode.length() - 1); // remove the ""
        StringBuffer sb = new StringBuffer();

        Pattern pattern = Pattern.compile(asmVariablePattern());
        Matcher matcher = pattern.matcher(asm);

        while (matcher.find()) {
            String ident = matcher.group(1);
            INFO i = symbolsTable.globalSearch(ident);

            if (i == null) {
                System.err.println("Warning(Semantics): undefined %" + ident +", ignoring");
            }
            else if (!(i instanceof INFOVAR)) {
                System.err.println("Warning(Semantics): %" + ident +" not a variable, ignoring");
            }
            else {
                INFOVAR info = (INFOVAR) i;
                ident = genLocation(info.getLocation());
            }

            matcher.appendReplacement(sb, ident);
        }
        matcher.appendTail(sb);

        return new Code(sb.toString());
    }
}
