package moc.st;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import moc.type.TFUNCTION;

/**
 * A hierarchical symbols table
 */
public class ST extends HashMap<String, INFO> {
    private static final long serialVersionUID = 1L;

    /**
     * The mother ST
     */
    private ST mother;

    /**
     * The current function
     */
    private TFUNCTION currentFunction;

    /**
     * Constructor for a symbols table without mother
     */
    public ST() {
        this(null);
    }

    /**
     * Constructor for a ST daughter of p
     */
    public ST(ST p) {
        super();
        mother = p;
        currentFunction = null;
    }

    public ST getMother() {
        return mother;
    }

    /**
     * Look for n in the current TDS only
     */
    public INFO localSearch(String n) {
        return get(n);
    }

    /**
     * Look for n in the current TDS and its ancestors
     */
    public INFO globalSearch(String n) {
        INFO i = localSearch(n);

        if (i == null && mother != null) {
            return mother.globalSearch(n);
        }

        return i;
    }

    /**
     * Add n and its info i in the TDS
     */
    public void insert(String n, INFO i) {
        put(n, i);
    }

    public void setCurrentFunction(TFUNCTION function) {
        currentFunction = function;
    }

    public TFUNCTION getCurrentFunction() {
        if (currentFunction == null && mother != null) {
            return mother.getCurrentFunction();
        }

        return currentFunction;
    }

    public String toString() {
        return toString(true);
    }

    public String toString(boolean printMother) {
        StringBuffer sb = new StringBuffer();

        if (printMother && mother != null)
            sb.append(mother.toString(true));

        Set<Map.Entry<String, INFO>> s = entrySet();
        for (Map.Entry<String, INFO> e : s) {
            sb.append("\t" + e.getKey() + " : " + e.getValue() + '\n');
        }

        return sb.toString();
    }
}
