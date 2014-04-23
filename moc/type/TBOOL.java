package moc.type;

public class TBOOL implements TTYPE {
    private int size;

    public TBOOL(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public boolean compareTo(TTYPE other) {
        return false; /* TODO */
    }

    public String toString() {
        return "bool";
    }
}
