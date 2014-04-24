package moc.type;

public class TVOID implements TTYPE {
    public TVOID() {}

    public int getSize() {
        return 0;
    }

    public boolean constructFrom(TTYPE other) {
        return false;
    }

    public boolean comparableWith(TTYPE other, String op) {
        return false;
    }

    public boolean binaryUsable(TTYPE other, String op) {
        return false;
    }

    public boolean unaryUsable(String op) {
        return false;
    }

    public boolean testable() {
        return false;
    }

    public boolean equals(TTYPE other) {
        return other instanceof TVOID;
    }

    public String toString() {
        return "void";
    }
}
