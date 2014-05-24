package moc.type;

public class TINTEGER implements TTYPE {
    private int size;

    public TINTEGER(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public boolean constructFrom(TTYPE other) {
        return other instanceof TINTEGER;
    }

    public boolean comparableWith(TTYPE other, String op) {
        return other instanceof TINTEGER;
    }

    public boolean binaryUsable(TTYPE other, String op) {
        return !op.equals("&&") && !op.equals("||")
            && (other instanceof TINTEGER || other instanceof TPOINTER);
    }

    public boolean unaryUsable(String op) {
        return op.equals("+") || op.equals("-");
    }

    public boolean isCastableTo(TTYPE other) {
        return other instanceof TINTEGER
            || other instanceof TBOOL
            || other instanceof TCHAR
            || other instanceof TPOINTER
            || other instanceof TINSTANCE
            || other instanceof TID;
    }

    public boolean testable() {
        return false;
    }

    public boolean equals(TTYPE other) {
        return other instanceof TINTEGER;
    }

    public String toString() {
        return "int";
    }
}
