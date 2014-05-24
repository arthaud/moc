package moc.type;

public class TID implements TTYPE {
    public TID() {}

    public int getSize() {
        return 0; // TODO: check
    }

    public boolean constructFrom(TTYPE other) {
        return other instanceof TID || other instanceof TNULL || other instanceof TINSTANCE;
    }

    public boolean comparableWith(TTYPE other, String op) {
        if(!op.equals("==") && !op.equals("!="))
            return false;

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
