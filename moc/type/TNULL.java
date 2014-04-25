package moc.type;

public class TNULL implements TTYPE {
    public TNULL() {}

    public int getSize() {
        return 0;
    }

    public boolean constructFrom(TTYPE other) {
        return false;
    }

    public boolean comparableWith(TTYPE other, String op) {
        return (op.equals("==") || op.equals("!=")) && (other instanceof TNULL || other instanceof TPOINTER);
    }

    public boolean binaryUsable(TTYPE other, String op) {
        return false;
    }

    public boolean unaryUsable(String op) {
        return false;
    }

    public boolean isCastableTo(TTYPE other) {
        return false; // we never need to cast null
    }

    public boolean testable() {
        return false;
    }

    public boolean equals(TTYPE other) {
        return other instanceof TNULL;
    }

    public String toString() {
        return "null";
    }
}
