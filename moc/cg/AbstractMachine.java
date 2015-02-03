package moc.cg;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;

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
    protected String endCode = "";

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
            pw.print(endCode);
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

    /**
     * Parse a string and returns the corresponding array of ASCII codes,
     * with a trailing zero
     *
     * ex: getStringAsArray("\"toto\"") = {116, 111, 116, 111, 0}
     */
    public List<Integer> getArrayFromString(String txt) {
        txt = txt.substring(1, txt.length() - 1); // remove the ""
        List<Integer> str = new ArrayList<Integer>(txt.length());
        boolean escaped = false;

        for(int i = 0; i < txt.length(); i++) {
            if(escaped) {
                if(txt.charAt(i) == 'n') str.add(10);
                else if(txt.charAt(i) == 'r') str.add(13);
                else if(txt.charAt(i) == 't') str.add(9);
                else str.add((int) txt.charAt(i));

                escaped = false;
            }
            else if(txt.charAt(i) == '\\')
                escaped = true;
            else
                str.add((int) txt.charAt(i));
        }

        str.add(0);
        return str;
    }

    /**
     * Parse a string and returns the char
     *
     * ex: getStringAsChar("'t'") = 116
     */
    public int getCharFromString(String c) {
        if(c.equals("'\\0'")) return 0;
        else if(c.equals("'\\n'")) return 10;
        else if(c.equals("'\\r'")) return 13;
        else if(c.equals("'\\t'")) return 9;
        else return (int) c.charAt(1);
    }

    /**
     * Parse a string and returns an integer
     */
    public int getIntFromString(String c) {
        if(c.startsWith("0x"))
            return Integer.parseInt(c.substring(2), 16);
        else if(c.startsWith("0b"))
            return Integer.parseInt(c.substring(2), 2);
        else
            return Integer.parseInt(c);
    }
}
