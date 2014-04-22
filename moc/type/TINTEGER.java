package moc.type;

public class TINTEGER implements TTYPE {
    private int size;

    public TINTEGER(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public boolean compareTo(TTYPE other) {
        return false; /* TODO */
    }

    public String toString() {
        return "int";
    }
}
