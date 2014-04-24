package moc.type;

public class TBOOL implements TTYPE {
    private int size;

    public TBOOL(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public boolean constructFrom(TTYPE other) {
        return other instanceof TBOOL;
    }

    public boolean comparableWith(TTYPE other, String op) {
        return (op.equals("==") || op.equals("!=")) && other instanceof TBOOL;
    }

    public boolean binaryUsable(TTYPE other, String op) {
        return (op.equals("||") || op.equals("&&")) && other instanceof TBOOL;
    }

    public boolean unaryUsable(String op) {
        return op.equals("!");
    }

    public boolean testable() {
        return true;
    }

    public boolean equals(TTYPE other) {
        return other instanceof TBOOL;
    }

    public String toString() {
        return "bool";
    }
}
