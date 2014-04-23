package moc.type;

public class TNULL implements TTYPE {
    public TNULL() {}

    public int getSize() {
        return 0;
    }

    public boolean compareTo(TTYPE other) {
        return false; /* TODO */
    }

    public String toString() {
        return "null";
    }
}
