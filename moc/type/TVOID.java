package moc.type;

public class TVOID implements TTYPE {
    public TVOID() {}

    public int getSize() {
        return 0;
    }

    public boolean compareTo(TTYPE other) {
        return false; /* TODO */
    }

    public String toString() {
        return "void";
    }
}
