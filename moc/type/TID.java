package moc.type;

public class TID implements TTYPE {
    public TID() {}

    public int getSize() {
        return 0; // TODO: check
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
        return "id";
    }
}
