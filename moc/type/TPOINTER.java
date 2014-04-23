package moc.type;

public class TPOINTER implements TTYPE {
    private TTYPE type;
    private int size;

    public TPOINTER(TTYPE type, int size) {
        this.type = type;
        this.size = size;
    }

    public TTYPE getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public boolean compareTo(TTYPE other) {
        return false; /* TODO */
    }

    public String toString() {
        return "pointer on " + type.toString();
    }
}
