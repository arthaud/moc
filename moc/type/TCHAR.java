package moc.type;

public class TCHAR implements TTYPE {
    private int size;

    public TCHAR(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public boolean constructFrom(TTYPE other) {
        return other instanceof TCHAR;
    }

    public boolean comparableWith(TTYPE other, String op) {
        return other instanceof TCHAR;
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
        return other instanceof TCHAR;
    }

    public String toString() {
        return "char";
    }
}
