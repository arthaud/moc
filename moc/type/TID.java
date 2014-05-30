package moc.type;

public class TID implements TTYPE {
    private int size;

    public TID(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public boolean constructFrom(TTYPE other) {
        return other instanceof TID || other instanceof TNULL || other instanceof TINSTANCE;
    }

    public boolean comparableWith(TTYPE other, String op) {
        return other instanceof TID || other instanceof TNULL || other instanceof TINSTANCE;
    }

    public boolean binaryUsable(TTYPE other, String op) {
        return false;
    }

    public boolean unaryUsable(String op) {
        return false;
    }

    public boolean isCastableTo(TTYPE other) {
        return other instanceof TINTEGER
            || other instanceof TPOINTER
            || other instanceof TINSTANCE
            || other instanceof TID;
    }

    public boolean testable() {
        return false;
    }

    public boolean equals(TTYPE other) {
        return other instanceof TID;
    }

    public String toString() {
        return "id";
    }
}
