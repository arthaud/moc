package moc.type;

public class TNULL implements TTYPE {
    private int size;

    public TNULL(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public boolean constructFrom(TTYPE other) {
        return false;
    }

    public boolean comparableWith(TTYPE other, String op) {
        return other instanceof TNULL || other instanceof TPOINTER || other instanceof TINSTANCE || other instanceof TID;
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
