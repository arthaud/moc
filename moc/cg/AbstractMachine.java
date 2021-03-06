package moc.cg;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import moc.compiler.MOCException;
import moc.st.INFO;
import moc.st.INFOVAR;
import moc.st.ST;
import moc.type.TFUNCTION;

/**
 * This class describes a target machine
 */
public abstract class AbstractMachine implements IMachine {

    protected int labelNum = 0;
    protected ArrayDeque<Integer> loopLabelStack = new ArrayDeque<>();

    protected int getLabelNum() {
        return labelNum++;
    }

    public int currentLoopLabel() {
        return loopLabelStack.peek();
    }

    protected String initCode = "";
    protected String endCode = "";

    private HashMap<String, HashSet<String>> callTree = new HashMap<>();

    /**
     * Writes the code in a file from the name of the source file and the suffix
     */
    @Override
    public void writeCode(String fname, EntityList entities) throws MOCException {
        try {
            // clean-up
            prepareWrite(entities);

            // pre-checked at startup
            int pt = fname.lastIndexOf('.');
            String name = fname.substring(0, pt);
            String asmName = name + "." + getSuffix();
            System.err.println("Writing code in " + asmName);
            PrintWriter pw = new PrintWriter(new FileOutputStream(asmName));
            pw.print(genComment("Generated code for " + fname + "\n"));
            pw.print(genComment("Do not modify by hand\n"));
            pw.print(initCode);

            HashSet<String> usedFunctions = usedFunctions(entities);
            for (EntityCode entity : entities.getList()) {
                if (shouldInclude(entity, usedFunctions)) {
                    pw.print(entity.getAsm());
                }
            }

            pw.print(endCode);
            pw.close();
        }
        catch (FileNotFoundException e) {
            throw new MOCException(e.getMessage());
        }
    }

    /**
     * Called before writeCode
     */
    protected void prepareWrite(EntityList entities) {}

    /**
     * Returns whether the given entity should be included in the final code.
     */
    protected boolean shouldInclude(EntityCode entity, HashSet<String> usedFunctions) {
        if (entity instanceof FunctionCode) {
            FunctionCode fc = (FunctionCode)entity;

            return fc.isExported() || usedFunctions.contains(fc.getName());
        }
        else {
            return true;
        }
    }

    /**
     * Returns the list of functions to include.
     */
    protected HashSet<String> usedFunctions(EntityList entities) {
        HashSet<String> list = new HashSet<>();
        list.add("main");

        for (EntityCode entity : entities.getList()) {
            if (entity instanceof FunctionCode
            && ((FunctionCode)entity).isExported()) {
                list.add(((FunctionCode)entity).getName());
            }
        }

        HashSet<String> newCallers = new HashSet<>();
        do {
            newCallers.clear();

            for (String caller : list) {
                if (callTree.containsKey(caller)) {
                    for (String callee : callTree.get(caller)) {
                        if (!list.contains(callee)) {
                            newCallers.add(callee);
                        }
                    }
                }
            }

            list.addAll(newCallers);
        }
        while (!newCallers.isEmpty());

        return list;
    }

    public Code genFunctionCall(String currentFunc, TFUNCTION f, Code arguments) {
        // remember the function was called and should not be removed
        if (!callTree.containsKey(currentFunc)) {
            callTree.put(currentFunc, new HashSet<String>());
        }
        callTree.get(currentFunc).add(f.getName());

        return genFunctionCallImpl(f, arguments);
    }

    protected abstract Code genFunctionCallImpl(TFUNCTION f, Code arguments);

    public String genComment(String comm) {
        return "; " + comm;
    }

    protected String asmVariablePattern() {
        return "%([a-z][_0-9A-Za-z]*)";
    }

    public void beginLoop() {
        loopLabelStack.add(getLabelNum());
    }

    public void endLoop() {
        loopLabelStack.pop();
    }

    private String genAsmImpl(String asmCode, ST symbolsTable) {
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

        return sb.toString();
    }

    public Code genAsm(String asmCode, ST symbolsTable) {
        return new Code(genAsmImpl(asmCode, symbolsTable));
    }

    public AsmCode genGlobalAsm(String asmCode, ST symbolsTable) {
        return new AsmCode(genAsmImpl(asmCode, symbolsTable));
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
            else if(txt.charAt(i) == '\\') {
                escaped = true;
            }
            else {
                str.add((int) txt.charAt(i));
            }
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
        switch (c) {
            case "'\\0'": return 0;
            case "'\\n'": return 10;
            case "'\\r'": return 13;
            case "'\\t'": return 9;
            default: return (int) c.charAt(1);
        }
    }

    /**
     * Parse a string and returns an integer
     */
    public int getIntFromString(String c) {
        if(c.startsWith("0x")) {
            return Integer.parseInt(c.substring(2), 16);
        }
        else if(c.startsWith("0b")) {
            return Integer.parseInt(c.substring(2), 2);
        }
        else if(c.startsWith("0")) {
            return Integer.parseInt(c.substring(2), 8);
        }
        else {
            return Integer.parseInt(c);
        }
    }

    /**
     * Parse a string and returns a long
     */
    public long getLongFromString(String c) {
        if(c.startsWith("0x"))
            return Long.parseLong(c.substring(2), 16);
        else if(c.startsWith("0b"))
            return Long.parseLong(c.substring(2), 2);
        else
            return Long.parseLong(c);
    }
}
