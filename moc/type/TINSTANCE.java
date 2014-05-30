package moc.type;

public class TINSTANCE implements TTYPE {
    private TCLASS type;
    private int size;

    public TINSTANCE(TCLASS type, int size) {
        this.type = type;
        this.size = size;
    }

    public TCLASS getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public boolean constructFrom(TTYPE other) {
        return equals(other) || other instanceof TNULL || other instanceof TID;
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
        if(other instanceof TINSTANCE)
        {
            TINSTANCE instance = (TINSTANCE) other;
            return type.equals(instance.getType());
        }
        else
            return false;
    }

    public String toString() {
        return "instance of " + type.toString();
    }
}
