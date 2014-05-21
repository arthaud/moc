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
        return false; // TODO
    }

    public boolean comparableWith(TTYPE other, String op) {
        return false; // TODO
    }

    public boolean binaryUsable(TTYPE other, String op) {
        return false; // TODO
    }

    public boolean unaryUsable(String op) {
        return false; // TODO
    }

    public boolean isCastableTo(TTYPE other) {
        return false; // TODO
    }

    public boolean testable() {
        return false; // TODO
    }

    public boolean equals(TTYPE other) {
        return false; // TODO
    }

    public String toString() {
        return "instance of " + type.toString();
    }
}
